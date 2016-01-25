package com.github.freedtice.catfix.task

import com.github.freedtice.catfix.model.PatchConfiguration
import org.gradle.api.DefaultTask

abstract class BaseTask extends DefaultTask {
  PatchConfiguration configuration
}


