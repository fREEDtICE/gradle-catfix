package com.github.freedtice.catfix.task

import com.github.freedtice.catfix.utils.LoggerUtil
import com.github.freedtice.catfix.utils.SdkHelper
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * @author yuantong
 */
class DexTask extends BaseTask {
  @InputFile
  File toDexJar

  @OutputFile
  File dex

  @TaskAction
  public void buildDex() {
    def dx = project.file("${SdkHelper.getBuildToolFolder(project)}/dx")
    LoggerUtil.d("${dx} --dex --output=${dex.absolutePath} --input-list=${toDexJar.absolutePath}")
    project.exec {
      commandLine dx, '--dex', "--output=${dex.absolutePath}", toDexJar.absolutePath
    }
  }
}
