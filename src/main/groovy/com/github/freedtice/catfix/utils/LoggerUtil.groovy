package com.github.freedtice.catfix.utils

import org.gradle.api.Project


class LoggerUtil {
  private static Project project;

  public static void init(Project p) {
    project = p;
  }

  public static void t(String msg, Object... params) {
    project.logger.trace("gi${msg}", params)
  }

  public static void d(String msg, Object... params) {
    project.logger.debug("******* ${msg}", params)
  }

  public static void i(String msg, Object... params) {
    project.logger.info("******* ${msg}", params)
  }

  public static void w(String msg, Object... params) {
    project.logger.warn("******* ${msg}", params)
  }

  public static void e(String msg, Object... params) {
    project.logger.error("******* ${msg}", params)
  }

  public static void e(String msg, Throwable error){
    project.logger.error("******* ${msg}", error)
  }
}