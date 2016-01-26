package com.github.freedtice.catfix.task

import com.github.freedtice.catfix.utils.CryptoUtil
import com.github.freedtice.catfix.utils.FileUtils
import com.github.freedtice.catfix.utils.MappingReader
import com.github.freedtice.catfix.utils.SdkHelper
import com.google.common.collect.Lists
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import javassist.*
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.*

import java.security.InvalidParameterException

class PreparePatchTask extends BaseTask {
  @OutputDirectory
  File diffClassesDir
  @OutputFile
  File diffMD5
  @OutputDirectory
  File originalClassesDir
  @OutputDirectory
  File patchJarDir

  @Input
  String lazyName
  @InputFile
  File cacheMD5
  @InputDirectory
  File variantClassDir

  @TaskAction
  def prepare() {
    if (!project.catfix.patchVersion) {
      throw new InvalidParameterException("patch version is not specified")
    }

    FileUtils.cleanAndMKDirs(configuration.patchVersionDir)
    FileUtils.mkDirsIfNotExists(originalClassesDir)
    FileUtils.mkDirsIfNotExists(diffClassesDir)
    FileUtils.mkDirsIfNotExists(patchJarDir)

    def md5Cache = new JsonSlurper().parseText(cacheMD5.text)

    def changedFileMap = new LinkedHashMap()

    def md5Map = new LinkedHashMap()

    FileTree variantClasses = project.fileTree(dir: variantClassDir.absolutePath)
    variantClasses.include '**/*.class'
    variantClasses.exclude "${configuration.preverifyPreventorClassName.replace('.', '/')}.class"

    variantClasses.each { File file ->
      def key = variantClassDir.toURI().relativize(file.toURI()).toString()
      def md5 = CryptoUtil.generateMD5(file)
      if (!file.name.equals('R.class') && !file.name.startsWith('R$') &&
          (!md5Cache.containsKey(key) || !md5.equals(md5Cache.get(key)))) {
        changedFileMap.put(key, file)
        md5Map.put(key, md5)
      } else {
        def parentDir = new File(originalClassesDir, new File(key.toString()).getParent())
        FileUtils.mkDirsIfNotExists(parentDir)
        project.copy {
          from file
          into parentDir
        }
      }
    }

    if (changedFileMap.isEmpty()) {
      println 'no changed classes found. no patch need!'
      return
    }

    ClassPool pool = new ClassPool()
    pool.appendSystemPath()
    pool.appendClassPath(SdkHelper.getAndroidRuntime(project).absolutePath)
    pool.appendClassPath(variantClassDir.absolutePath)
    project.configurations.compile.each { File file ->
      if (file.name.endsWith(".jar")) {
        pool.appendClassPath(file.absolutePath)
      }
    }
    SdkHelper.getProjectAARLibrary(project).each { File jar ->
      pool.appendClassPath(jar.absolutePath)
    }

    MappingReader reader = new MappingReader()
    if (configuration.variant.buildType.minifyEnabled) {
      reader.parse(project.file("${configuration.proguardCopyDir}/mapping.txt"))
    }

    searchAllRelatedClass(md5Cache, reader, pool, changedFileMap, md5Map)

    CtClass LazyAndroid = pool.get(lazyName)

    removePatchMethod(LazyAndroid)

    CtMethod method = createPatchMethod(LazyAndroid)

    CtConstructor constructor = LazyAndroid.getConstructors()[0]

    constructor.setBody("{try{loadPatchClasses();}catch(Throwable ignore){}}")

    changedFileMap.entrySet().each { entry ->
      transferClassFile(entry.key, entry.value)
      def className = entry.getKey().substring(0, entry.getKey().indexOf('.')).replace('/', '.')
      try {
        method.insertAfter("System.out.println(${className}.class);")
      } catch (Throwable throwable) {
        project.logger.error("error when parse class ${className}", throwable)
      }
    }

    LazyAndroid.writeFile(diffClassesDir.path)
    LazyAndroid.detach()

    def json = JsonOutput.toJson(md5Map)

    diffMD5.withWriter {
      it.write(json.toString())
    }

    FileUtils.mkDirsIfNotExists(patchJarDir)
  }

  private static void removePatchMethod(CtClass LazyAndroid) {
    try {
      CtMethod old = LazyAndroid.getDeclaredMethod("loadPatchClasses")
      LazyAndroid.removeMethod(old)
    } catch (Throwable notFound) {
    }
  }

  private static CtMethod createPatchMethod(CtClass LazyAndroid) {
    CtMethod patchMethod = CtNewMethod.make("private void loadPatchClasses(){ }", LazyAndroid)
    CtClass throwable = ClassPool.getDefault().get("java.lang.Throwable")
    LazyAndroid.addMethod(patchMethod)
    return patchMethod
  }

  def transferClassFile(String key, File file) {
    def parentDir = new File(diffClassesDir, new File(key).getParent())
    FileUtils.mkDirsIfNotExists(parentDir)
    project.copy {
      from file
      into parentDir
    }
  }

  def collectionAllRefClass(Object md5, MappingReader reader, ClassPool pool, CtClass clazz, List collection) {
    clazz.getRefClasses().each { String name ->
      if (md5.containsKey(toPathKey(name)) && !collection.contains(name) && reader.isShrink(name)) {
        project.logger.debug("add ref class ${name}")
        collection.add(name)
        try {
          CtClass newClazz = pool.get(name)
          collectionAllRefClass(md5, reader, pool, newClazz, collection)
        } catch (Throwable throwable) {
          project.logger.error("error when collectionAllRefClass ${name}", throwable)
        }
      }
    }
  }

  def static String toPathKey(String className) {
    return "${className.replace('.', '/')}.class"
  }

  def searchAllRelatedClass(Object md5s, MappingReader reader, ClassPool pool, LinkedHashMap changedFileMap, LinkedHashMap md5Map) {
    ArrayList allClazz = Lists.newArrayList()
    changedFileMap.entrySet().each { Map.Entry<String, File> entry ->
      String key = entry.key
      def className = key.substring(0, key.indexOf('.')).replace('/', '.')
      try {
        CtClass clazz = pool.get(className)
        collectionAllRefClass(md5s, reader, pool, clazz, allClazz)
      } catch (Throwable throwable) {
        project.logger.error("error when load class ${className}", throwable)
      }
    }

    allClazz.each { String className ->
      String key = toPathKey(className)
      if (!changedFileMap.containsKey(key)) {
        project.logger.debug("found changed class ${key}")
        File file = new File(variantClassDir.absolutePath, key)
        if (file.exists()) {
          String md5 = CryptoUtil.generateMD5(file)
          changedFileMap.put(key, file)
          md5Map.put(key, md5)
          project.logger.debug("add class ${key} - ${file} to changed map")
        }
      }
    }
  }
}