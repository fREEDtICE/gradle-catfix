package com.github.freedtice.catfix

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import com.github.freedtice.catfix.model.PatchConfiguration
import com.github.freedtice.catfix.model.internal.DefaultPatchConfigurationImpl
import com.github.freedtice.catfix.model.internal.FlavorPatchConfigurationImpl
import com.github.freedtice.catfix.task.*
import com.github.freedtice.catfix.utils.GradleVariantHelper
import com.github.freedtice.catfix.utils.SdkHelper
import javassist.*
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import proguard.gradle.ProGuardTask

public class CatFixPlugin implements Plugin<Project> {

  protected Project project

  protected PluginExtension pluginExtension

  private boolean hasCreatedTasks = false

  @Override
  void apply(Project project) {
    this.project = project

    pluginExtension = project.extensions.create('catfix', PluginExtension)

    def isLibrary = project.plugins.hasPlugin(LibraryPlugin);

    if (isLibrary) {
      throw new GradleException("library plugin is not supported! only support for application")
    }

    def android = project.extensions.getByType(AppExtension)

    android.applicationVariants.all { BaseVariant variant ->
      configureJavaCompileTask(project, variant, variant.javaCompile)
      configureAssembleTask(project, variant, variant.assemble)
    }

    project.afterEvaluate {
      createPatchTasks()
    }
//    project.apply plugin: CatFixPlugin
  }

  private void createPatchTasks() {
    if (hasCreatedTasks) {
      return
    }

    hasCreatedTasks = true
    doCreatePatchTasks()
  }

  private void doCreatePatchTasks() {
    def android = project.extensions.getByType(AppExtension)
    android.applicationVariants.all { BaseVariant variant ->
      def PATCH_GROUP = "Patch"

      def configuration = createPatchConfiguration(variant, project, pluginExtension)

      def prepareTask = project.tasks.create("preparePatch${variant.name.capitalize()}", PreparePatchTask)
      prepareTask.configuration = configuration
      prepareTask.variantClassDir = configuration.variantClassDir
      prepareTask.cacheMD5 = configuration.md5
      prepareTask.diffClassesDir = configuration.diffClassesDir
      prepareTask.originalClassesDir = configuration.originalClassesDir
      prepareTask.diffMD5 = configuration.patchDiffMD5
      prepareTask.lazyName = configuration.preverifyPreventorClassName
      prepareTask.patchJarDir = configuration.patchJarDir

      prepareTask.onlyIf {
        configuration.md5.exists() && configuration.variantClassDir.exists()
      }

      prepareTask.dependsOn variant.javaCompile

      def originJarTask = project.tasks.create("jarOriginal${variant.name.capitalize()}", JarTask)
      originJarTask.configuration = configuration
      originJarTask.jarFile = configuration.originalJar
      originJarTask.patchClasses = configuration.originalClassesDir
      originJarTask.dependsOn prepareTask
      originJarTask.onlyIf {
        configuration.originalClassesDir.exists() && null != configuration.originalClassesDir.listFiles() && configuration.originalClassesDir.listFiles().length > 0
      }

      def jarTask = project.tasks.create("jarPatch${variant.name.capitalize()}", JarTask)
      jarTask.configuration = configuration
      jarTask.jarFile = configuration.diffJar
      jarTask.patchClasses = configuration.diffClassesDir
      jarTask.dependsOn originJarTask
      jarTask.onlyIf {
        configuration.diffClassesDir.exists() && null != configuration.diffClassesDir.listFiles() && configuration.diffClassesDir.listFiles().length > 0
      }

      def dexTask = project.tasks.create("buildPatch${variant.name.capitalize()}", DexTask)
      dexTask.configuration = configuration
      dexTask.dex = configuration.diffDexFile

      if (variant.buildType.minifyEnabled) {
        ProGuardTask proGuardTask = project.tasks.create("proguardPatch${variant.name.capitalize()}", ProGuardTask)
        variant.buildType.proguardFiles.each { File f ->
          proGuardTask.configuration(f)
        }

        proGuardTask.applymapping("${configuration.proguardCopyDir.absolutePath}/mapping.txt")

        proGuardTask.dontshrink()

        proGuardTask.libraryjars(SdkHelper.getAndroidRuntime(project))

        SdkHelper.getProjectAARLibrary(project).each { File jar ->
          proGuardTask.libraryjars(jar)
        }

        project.configurations.compile.each { File file ->
          if (file.name.endsWith(".jar")) {
            proGuardTask.libraryjars(file)
          }
        }

        proGuardTask.libraryjars(configuration.originalJar)
        proGuardTask.injars("${configuration.diffJar}")

        File proguardJar = project.file("${configuration.patchVersionDir}/proguard/${configuration.diffJar.name}")

        proGuardTask.outjars(proguardJar)

        proGuardTask.dependsOn jarTask

        dexTask.toDexJar = proguardJar

        proGuardTask.onlyIf {
          configuration.diffJar.exists() && configuration.originalJar.exists()
        }

        dexTask.onlyIf {
          proguardJar.exists()
        }

        dexTask.dependsOn proGuardTask
      } else {
        dexTask.toDexJar = configuration.diffJar
        dexTask.dependsOn jarTask
        dexTask.onlyIf {
          configuration.diffJar.exists()
        }
      }
    }

    def patchTask = project.tasks.create("patch")
    patchTask.dependsOn {
      project.tasks.findAll { task ->
        task.name.startsWith('buildPatch')
      }
    }

    def patchDebug = project.tasks.create("patchDebug")
    patchDebug.dependsOn {
      project.tasks.findAll { task ->
        task.name.startsWith('buildPatch') && task.name.endsWith('Debug')
      }
    }

    def patchRelease = project.tasks.create("patchRelease")
    patchRelease.dependsOn {
      project.tasks.findAll { task ->
        task.name.startsWith('buildPatch') && task.name.endsWith('Release')
      }
    }
  }

  private
  static PatchConfiguration createPatchConfiguration(BaseVariant variant, Project project, PluginExtension extension) {
    def android = project.extensions.getByType(AppExtension)
    def configuration
    if (GradleVariantHelper.isFlavor(variant)) {
      configuration = new FlavorPatchConfigurationImpl(variant, project, extension, android.productFlavors[variant.flavorName])
    } else {
      configuration = new DefaultPatchConfigurationImpl(variant, project, extension)
    }
    return configuration
  }

  private void configureJavaCompileTask(Project project, BaseVariant variant, Task javaCompile) {
    def configuration = createPatchConfiguration(variant, project, pluginExtension)
    javaCompile.doLast {
      ClassPool pool = ClassPool.getDefault()
      CtClass LazyAndroid = pool.makeClass(configuration.preverifyPreventorClassName)
      pool.appendClassPath(configuration.variantClassDir.absolutePath)
      LazyAndroid.writeFile(configuration.variantClassDir.absolutePath)
      LazyAndroid.defrost()
    }

    def destDir = project.file("${project.buildDir}/catfix/${variant.name}")

    ClassPreverifiedProcessTask preverifiedProcessTask = project.tasks.create("preventPreverifyClass${variant.name.capitalize()}", ClassPreverifiedProcessTask)
    preverifiedProcessTask.configuration = configuration
    preverifiedProcessTask.classesDir = configuration.variantClassDir
    preverifiedProcessTask.preverifyClassName = configuration.preverifyPreventorClassName
    preverifiedProcessTask.destDir = destDir

    preverifiedProcessTask.outputs.upToDateWhen { false }
    javaCompile.finalizedBy(preverifiedProcessTask)
  }

  private void configureAssembleTask(Project project, BaseVariant variant, Task assembleTask) {
//    def variantType = variant.name
//    if (variantType.equals("release")) {
    assembleTask.doLast(new AssemblePatch(createPatchConfiguration(variant, project, pluginExtension)))
//    }
  }
}
