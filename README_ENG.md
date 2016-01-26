## gradle-catfix
This project is gradle plugin for [Catfix]. with this tool, you can build your patch dex file with one command.

**NOTE:The minimum android gradle plugin is 1.0.0.

#### Installation
---
add the following to your build.gradle:
```groovy
buildscript {
  repositories {
    jcenter()
    mavenCentral()
    mavenLocal()
  }
  dependencies {
    classpath 'com.github.freedtice:gradle-catfix:0.1.0'
}

apply plugin: 'com.android.application'
apply plugin: 'com.github.freedtice.catfix'
```

#### Configuration
---
```groovy
android {
    ......
}
catfix {
    patchVersion 1
}
```
- patchVersion: a must-give parameter, integer, presents the version of your patch
- releaseRecordDir: optional, file, default is project.projectDir
- patchDir: optional, file, default is project.file("${project.orijectDir.absolutePath}/${appVersion}/${patchVersion}/patches")

if you are using Proguard, add the following to your proguard-rules.pro:
```java
-keep class me.soandky.catfix.android.ClassPreverifyPreventor
```

### How to use it
1. When you execute any assemble gradle task, will generate the following files under the ${releaseRecordDir}:
```sh
  + releaseRecordDir
    + ${app version} # your app version name, defined in AndroidManifest.xml or build.gradle
      + ${build type| product flavor name} # build type, for example:debug, release. If you are using product flavor, it would be the name of flavor
        - ${app name}-${build type}.apk # the apk assembled by task
        - md5.json # the md5 values for compiled class files(build/intermediates/classes)
        + proguard # files copy for proguard
```
2. When you need build a patch, set your patch version in your build.gradle(mentioned above), then execute '../gradlew patchRelease' or '../gradlew patchDebug'. After task finished, it should created following files under your ${patchDir}. :
```sh
  + releaseRecordDir
    + ${app version} #  your app version name, defined in AndroidManifest.xml or build.gradle
      + ${build type| product flavor name} # build type, for example:debug, release. If you are using product flavor, it would be the name of flavor
        - ${app name}-${build type}.apk # the apk assembled by task
        - md5.json #  md5 for compiled class files(build/intermediates/classes)
        + proguard #  files copy for proguard
        + patches # patch dir
          - patch.dex  # the final path dex file
          - diff.json # md5 for modified classes compare to assemble
          + ${patch version}
            + classes
              + diff # modified classes compare to assemble
              + original # unmodified classes
          + jar
            - origin.jar # jar of modified classes
            - patch.jar # jar of unmodified classes
          + proguard
            - patch.jar # patch file after proguard if you are using it

```

### About Catfix
[Catfix] is an Android hotfix system

[Catfix]:<https://github.com/fREEDtICE/catfix/blob/master/README.md>