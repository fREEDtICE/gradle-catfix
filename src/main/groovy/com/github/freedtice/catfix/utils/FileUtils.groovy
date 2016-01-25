package com.github.freedtice.catfix.utils

class FileUtils {
  public static void mkDirsIfNotExists(File f) {
    if (!f.exists()) {
      f.mkdirs()
    }
  }

  public static void cleanAndMKDirs(File f) {
    deleteFolder(f)
    f.mkdirs()
  }

  public static void deleteFolder(File folder) {
    File[] files = folder.listFiles()
    if (files != null && files.length > 0) {
      for (File file : files) {
        if (file.isDirectory()) {
          deleteFolder(file)
        } else {
          file.delete()
        }
      }
    }

    folder.delete()
  }
}