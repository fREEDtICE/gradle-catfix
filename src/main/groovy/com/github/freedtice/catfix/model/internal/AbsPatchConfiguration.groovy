package com.github.freedtice.catfix

import com.android.build.gradle.api.BaseVariant
import com.github.freedtice.catfix.model.PatchConfiguration
import org.gradle.api.Project
import org.gradle.api.file.FileTree

abstract class AbsPatchConfiguration implements PatchConfiguration {
  protected PluginExtension extension

  protected Project project

  protected BaseVariant variant

  protected File baseDir, releaseDir, verDir, md5Local, classDir, proguardDir, diffClassesDir, patchBase,
                 patchVersion, patchClassDir, patchJar, originalJar, originalClassesDir, diffDex, patchDirForJar

  protected FileTree proguardTree

  AbsPatchConfiguration(BaseVariant releaseVariant, Project project, PluginExtension extension) {
    this.extension = extension
    this.project = project
    this.variant = releaseVariant
  }

  @Override
  PluginExtension getExtension() {
    return extension
  }

  @Override
  BaseVariant getVariant() {
    return variant
  }

  @Override
  Project getProject() {
    return project
  }

  @Override
  File getOutputDir() {
    if (null == baseDir) {
      baseDir = extension.releaseRecordDir ?: project.projectDir;
    }
    return baseDir
  }

  @Override
  File getReleaseRecDir() {
    if (null == releaseDir) {
      releaseDir = new File(getOutputDir(), "releaseRecords")
    }
    return releaseDir
  }


  @Override
  String getPreverifyPreventorClassName() {
    return "com.github.freedtice.catfix.android.ClassPreverifyPreventor"
  }

  @Override
  File getPatchDiffMD5() {
    return new File(getPatchVersionDir(), "diff.json")
  }

  @Override
  File getPatchVersionDir() {
    if (null == patchVersion) {
      patchVersion = new File(getPatchBaseDir(), String.valueOf(extension.patchVersion))
    }
    return patchVersion

  }

  @Override
  File getPatchClassesDir() {
    if (null == patchClassDir) {
      patchClassDir = new File(getPatchVersionDir(), "classes")
    }
    return patchClassDir
  }

  @Override
  File getDiffJar() {
    if (null == patchJar) {
      patchJar = new File(getPatchJarDir(), "patch.jar")
    }
    return patchJar
  }

  @Override
  File getDiffClassesDir() {
    if (null == diffClassesDir) {
      diffClassesDir = new File(getPatchClassesDir(), "diff")
    }
    return diffClassesDir
  }

  @Override
  File getOriginalJar() {
    if (null == originalJar) {
      originalJar = new File(getPatchJarDir(), "origin.jar")
    }
    return originalJar
  }

  @Override
  File getOriginalClassesDir() {
    if (null == originalClassesDir) {
      originalClassesDir = new File(getPatchClassesDir(), "original")
    }

    return originalClassesDir

  }

  @Override
  File getProguardCopyDir() {
    if (null == proguardDir) {
      proguardDir = new File(getVersionDir(), "proguard")
    }
    return proguardDir
  }

  @Override
  File getDiffDexFile() {
    if (null == diffDex) {
      diffDex = new File(getPatchVersionDir(), "patch.dex")
    }
    return diffDex
  }

  @Override
  File getPatchJarDir() {
    if (null == patchDirForJar) {
      patchDirForJar = new File(getPatchVersionDir(), "jar")
    }

    return patchDirForJar
  }

  @Override
  File getMd5() {
    if (null == md5Local) {
      md5Local = new File(getVersionDir(), "md5.json")
    }
    return md5Local
  }

  @Override
  File getPatchBaseDir() {
    if (null == patchBase) {
      patchBase = extension.patchDir ?: new File(getVersionDir(), "patches")
    }
    return patchBase
  }
}