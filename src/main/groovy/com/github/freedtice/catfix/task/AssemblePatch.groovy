package com.github.freedtice.catfix.task

import com.github.freedtice.catfix.model.PatchConfiguration
import com.github.freedtice.catfix.utils.CryptoUtil
import com.github.freedtice.catfix.utils.FileUtils
import groovy.json.JsonOutput
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree

class AssemblePatch implements Action<DefaultTask> {

  protected PatchConfiguration configuration

  AssemblePatch(PatchConfiguration configuration) {
    this.configuration = configuration
  }

  @Override
  void execute(DefaultTask task) {
    def md5map = new LinkedHashMap()

    FileTree variantClasses = configuration.project.fileTree(dir: configuration.variantClassDir.absolutePath)
    variantClasses.include '**/*.class'
    variantClasses.exclude '**/R.class'
    variantClasses.exclude '**/R$*.class'

    variantClasses.each { File file ->
      def key = configuration.variantClassDir.toURI().relativize(file.toURI()).toString()
      def md5 = CryptoUtil.generateMD5(file)
      md5map.put(key, md5)
    }

    FileUtils.cleanAndMKDirs(configuration.versionDir)
    FileUtils.cleanAndMKDirs(configuration.proguardCopyDir)

    configuration.project.copy {
      from configuration.APK
      into configuration.versionDir
    }

    def json = JsonOutput.toJson(md5map)

    configuration.md5.withWriter {
      it.write(json.toString())
    }

    if (!configuration.variant.buildType.minifyEnabled) {
      println "proguard ${configuration.variant.name} is  ${configuration.variant.buildType.minifyEnabled} not activated. set minifyEnabled value to true to enable it"
      return
    }

    configuration.proguardFiles.each { file ->
      configuration.project.copy {
        from file
        into configuration.proguardCopyDir
      }
    }
  }
}