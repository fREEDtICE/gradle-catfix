package com.github.freedtice.catfix.task

import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * @author yuantong
 */
public class JarTask extends BaseTask {

  @OutputFile
  File jarFile

  @InputDirectory
  File patchClasses

  @TaskAction
  def jar() {
    project.logger.debug("start jar files. input is ${patchClasses.absolutePath}, output is ${jarFile.absolutePath}")
    project.exec {
      commandLine 'jar', 'cf', jarFile.absolutePath, '-C', patchClasses.absolutePath, "."
    }
  }
}
