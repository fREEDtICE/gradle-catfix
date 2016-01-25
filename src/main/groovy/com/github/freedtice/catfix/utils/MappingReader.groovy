package com.github.freedtice.catfix.utils

import com.google.common.collect.Lists

class MappingReader {
  private final List classNames;

  MappingReader() {
    this.classNames = Lists.newArrayList()
  }

  void parse(File mappingTxt) {
    if (!mappingTxt.exists()) {
      return
    }
    mappingTxt.eachLine { String line ->
      line = line.trim();
      if (line.endsWith(":")) {
        String className = processClassMapping(line);
        if (null != className && !classNames.contains(className)) {
          classNames.add(className)
        }
      }
    }
  }

  def boolean isShrink(String className) {
    return !classNames.isEmpty() && !classNames.contains(className)
  }

  private static String processClassMapping(String line) {
    int arrowIndex = line.indexOf("->")
    if (arrowIndex < 0) {
      return null
    }
    int colonIndex = line.indexOf(':', arrowIndex + 2)
    if (colonIndex < 0) {
      return null
    }
    return line.substring(0, arrowIndex).trim()
  }

}
