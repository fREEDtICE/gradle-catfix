package com.github.freedtice.catfix.task

import com.github.freedtice.catfix.utils.FileUtils
import com.github.freedtice.catfix.utils.SdkHelper
import javassist.*
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class ClassPreverifiedProcessTask extends BaseTask {

  @InputDirectory
  File classesDir
  @Input
  String preverifyClassName
  @OutputDirectory
  File destDir

  @TaskAction
  void processClass() {
    ClassPool pool = new ClassPool()
    pool.appendSystemPath()
    pool.appendClassPath(classesDir.absolutePath)
    pool.appendClassPath(SdkHelper.getAndroidRuntime(project).absolutePath)
    FileTree classTree = project.fileTree(dir: classesDir.absolutePath)
    classTree.include '**/*.class'

    CtClass applicationClass = pool.get('android.app.Application')

    FileUtils.cleanAndMKDirs(destDir)
    classTree.each { File file ->
      def key = classesDir.toURI().relativize(file.toURI()).toString()
      // com/foo/class.class => com.foo.class
      def className = key.substring(0, key.indexOf('.')).replace('/', '.')
      // skip inner class, retrolamda class and R
      if (key.indexOf('$') != -1 || className.equals(preverifyClassName) || "R.class".equals(file.name)) {
        project.logger.debug("do not process internal or lamda class:${key}")
      } else {
        project.logger.debug("process class ${className}, add class preverify preventor to constructors")
        try {
          CtClass toProcess = pool.get(className)
          // application class is start class, can not be patched
          if (!toProcess.isFrozen() && !toProcess.subclassOf(applicationClass)) {
            CtConstructor[] constructors = toProcess.getConstructors()
            final int size = constructors.length
            for (int i = 0; i < size; i++) {
              constructors[i].insertAfter("System.out.println(${preverifyClassName}.class);")
            }
            toProcess.writeFile(destDir.absolutePath)
          }
        } catch (Throwable throwable) {
          project.logger.error("error when process class ${className}", throwable)
        }
      }
    }

    project.copy {
      from destDir
      into classesDir
    }
  }
}