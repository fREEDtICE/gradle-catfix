package com.github.freedtice.catfix

public class PluginExtension {
  def File releaseRecordDir

  def File patchDir

  def int patchVersion

  @Override
  public String toString() {
    return "PluginExtension{" +
        "patchCode=" + patchCode +
        ", releaseRecordDir=" + releaseRecordDir +
        ", patchDir=" + patchDir +
        ", patchVersion='" + patchVersion + '\'' +
        '}';
  }
}