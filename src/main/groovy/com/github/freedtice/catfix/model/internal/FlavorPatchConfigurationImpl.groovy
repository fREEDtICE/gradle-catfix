package com.github.freedtice.catfix.model.internal

import com.android.build.gradle.api.BaseVariant
import com.github.freedtice.catfix.AbsPatchConfiguration
import com.github.freedtice.catfix.PluginExtension
import com.github.freedtice.catfix.utils.FileUtils
import org.gradle.api.Project
import org.gradle.api.file.FileTree

class FlavorPatchConfigurationImpl extends AbsPatchConfiguration {

  protected Object flavor

  FlavorPatchConfigurationImpl(BaseVariant releaseVariant, Project project, PluginExtension extension, Object flavor) {
    super(releaseVariant, project, extension)
    this.flavor = flavor
  }

  @Override
  File getVersionDir() {
    if (null == verDir) {
      verDir = new File(getReleaseRecDir(), "${flavor.versionName ?: project.android.defaultConfig.versionName}/${flavor.name}/${variant.buildType.name}")
    }
    return verDir
  }

  @Override
  FileTree getProguardFiles() {
    if (null == proguardTree) {
      proguardTree = project.fileTree(dir: "${project.buildDir.absolutePath}/outputs/mapping/${flavor.name}/${variant.buildType.name}")
      proguardTree.include '**/*.txt'
    }
    return proguardTree
  }

  @Override
  File getVariantClassDir() {
    return new File("${project.buildDir.absolutePath}/intermediates/classes/${flavor.name}/${variant.buildType.name}")
  }

  @Override
  File getAPK() {
    return new File("${project.buildDir.absolutePath}/outputs/apk/${project.name}-${flavor.name}-${variant.buildType.name}.apk")
  }
}