package com.github.freedtice.catfix.model

import com.android.build.gradle.api.BaseVariant
import com.github.freedtice.catfix.PluginExtension
import org.gradle.api.Project
import org.gradle.api.file.FileTree

interface PatchConfiguration {

  PluginExtension getExtension()

  BaseVariant getVariant()

  Project getProject()

  /**
   * get basic output directory
   * @return
   */
  File getOutputDir()
  /**x
   *
   * @return
   */
  File getReleaseRecDir()

  File getVersionDir()

  File getPatchBaseDir()

  File getPatchDiffMD5()

  File getPatchVersionDir()

  File getPatchClassesDir()

  File getDiffJar()

  File getDiffClassesDir()

  File getOriginalJar()

  File getOriginalClassesDir()

  File getDiffDexFile()

  File getMd5()

  File getPatchJarDir()

  FileTree getProguardFiles()

  File getVariantClassDir()

  File getProguardCopyDir()

  File getAPK()

  String getPreverifyPreventorClassName()
}



