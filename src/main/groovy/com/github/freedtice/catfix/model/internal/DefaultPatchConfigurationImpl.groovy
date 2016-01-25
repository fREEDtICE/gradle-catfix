package com.github.freedtice.catfix.model.internal

import com.android.build.gradle.api.BaseVariant
import com.github.freedtice.catfix.AbsPatchConfiguration
import com.github.freedtice.catfix.PluginExtension
import com.github.freedtice.catfix.utils.FileUtils
import org.gradle.api.Project
import org.gradle.api.file.FileTree

class DefaultPatchConfigurationImpl extends AbsPatchConfiguration {

  DefaultPatchConfigurationImpl(BaseVariant releaseVariant, Project project, PluginExtension extension) {
    super(releaseVariant, project, extension)
  }

  @Override
  File getVersionDir() {
    if (null == verDir) {
      verDir = new File(getReleaseRecDir(), "${project.android.defaultConfig.versionName}/${variant.name}")
    }
    return verDir
  }

  @Override
  FileTree getProguardFiles() {
    if (null == proguardTree) {
      proguardTree = project.fileTree(dir: "${project.buildDir.absolutePath}/outputs/mapping")
      proguardTree.include '**/*.txt'
    }
    return proguardTree
  }

  @Override
  File getVariantClassDir() {
    if (null == classDir) {
      classDir = new File("${project.buildDir.absolutePath}/intermediates/classes/${variant.name}")
    }
    return classDir
  }

  @Override
  File getAPK() {
    return new File("${project.buildDir.absolutePath}/outputs/apk/${project.name}-${variant.buildType.name}.apk")
  }
}