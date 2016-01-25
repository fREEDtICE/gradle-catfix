package com.github.freedtice.catfix.utils

class TextUtils {
  public static boolean isEmpty(String text) {
    return null == text || text.trim().length() == 0
  }
}