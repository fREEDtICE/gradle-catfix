### [English Version]

## gradle-catfix
本工程是[Catfix]的gradle插件。通过该插件可轻松完成补丁包的制作。

**注意:android gradle plugin的最低版本为1.0.0**
#### 安装
---
在build.gradle中添加以下代码:
```groovy
buildscript {
  repositories {
    jcenter()
    mavenCentral()
    mavenLocal()
  }
  dependencies {
    classpath 'com.github.freedtice:gradle-catfix:0.1.0-SNAPSHOT'
}

apply plugin: 'com.android.application'
apply plugin: 'com.github.freedtice.catfix'
```

#### 配置
---
```groovy
android {
    ......
}
catfix {
    patchVersion 1
}
```
- patchVersion: 必填参数, 整型, patch的版本号
- releaseRecordDir: 选填, file类型, 默认为project.file("${project.projectDir.absolutePath}/releaseRecords")
- patchDir: 选填, file 类型, 默认为project.file("${project.projectDir.absolutePath}/${appVersion}/${buildType|flavorName}${patchVersion}/patches")

如果使用了Proguard, 在proguard-rules.pro中添加:
```java
-keep class me.soandky.catfix.android.ClassPreverifyPreventor
```

### 使用方法
1. 当执行assemble任务时, 会在${releaseRecordDir}下生成以下文件:
```sh
  + releaseRecordDir
    + ${app version} # 即应用当前的版本号
      + ${build type| product flavor name} # build type, 如debug, release, 使用product flavor时, 会product flavor的名字
        - ${app name}-${build type}.apk # assemble生成的apk
        - md5.json # 编译生成的class文件(build/intermediates/classes)对应的md5值
        + proguard # proguard产生的文件副本
```
2. 当需要打补丁时, 依照配置先设置patch版本号, 然后执行'../gradlew patchRelease'或'../gradlew patchDebug', 也可执行'../gradlew patch'. 完成后在${patchDir}下生成patch文件. 以默认配置为例:
```sh
  + releaseRecordDir
    + ${app version} #即应用当前的版本号
      + ${build type| product flavor name} # build type, 如debug, release, 使用product flavor时, 会product flavor的名字
        - ${app name}-${build type}.apk #assemble生成的apk
        - md5.json #编译生成的class文件(build/intermediates/classes)对应的md5值
        + proguard #proguard产生的文件副本
        + patches #patch dir
          - patch.dex  # 补丁包
          - diff.json # 差异文件对应的md5值
          + ${patch version} #patch version
            + classes #编译生成的class
              + diff #发生变化的class, 会被打入补丁
              + original #没有变化的class
          + jar
            - origin.jar #没有变化的class生成的jar
            - patch.jar #补丁jar
          + proguard
            - patch.jar #如果有打开proguard, 会执行proguard, 此为proguard后的补丁jar

```

### 关于Catfix
[Catfix]是一套Android热修复系统。

[Catfix]:<https://github.com/fREEDtICE/catfix/blob/master/README.md>
[English Version]:<https://github.com/fREEDtICE/gradle-catfix/blob/master/README_ENG.md>
[QQ空间热修复技术]:<https://mp.weixin.qq.com/s?__biz=MzI1MTA1MzM2Nw==&mid=400118620&idx=1&sn=b4fdd5055731290eef12ad0d17f39d4a&scene=1&srcid=1106Imu9ZgwybID13e7y2nEi#wechat_redirect>