package com.github.freedtice.catfix.utils

import com.google.common.collect.Lists
import org.gradle.api.Project
import org.gradle.api.file.FileTree

import java.security.InvalidParameterException

public class SdkHelper {

  static String getCompileSdkVersion(Project project) {
    return project.android.compileSdkVersion
  }

  static String getBuildToolsVersion(Project project) {
    return project.android.buildToolsVersion
  }

  static String getSdkHome(Project project) {
    def sdkHome = System.getenv("ANDROID_HOME")
    if (null == sdkHome) {
      println 'ANDROID_HOME is not set in your environment path, use local.properties'
      Properties props = new Properties()
      props.load(new FileInputStream(project.rootProject.file("local.properties")))
      sdkHome = props.get('sdk.dir');
    }
    if (null == sdkHome) {
      throw new InvalidParameterException("CANNOT FIND ANDROID_HOME")
    }
    return sdkHome
  }

  static File getPlatformFolder(Project project) {
    return new File(getSdkHome(project), "platforms/${getCompileSdkVersion(project)}")
  }

  static File getExtrasFolder(Project project) {
    return new File(getSdkHome(project), "extras")
  }

  static File getBuildToolFolder(Project project) {
    return new File(getSdkHome(project), "build-tools/${getBuildToolsVersion(project)}")
  }

  static File getAndroidRuntime(Project project) {
    return new File(getPlatformFolder(project), "android.jar")
  }

  static ArrayList<File> getProjectAARLibrary(Project project) {
    FileTree tree = project.fileTree(dir: "${project.buildDir.path}/intermediates/exploded-aar")
    tree.include "**/*.jar"

    ArrayList<File> resultList = Lists.newArrayList()

    tree.each { File jar ->
      resultList.add(jar)
    }
    return resultList
  }
}


