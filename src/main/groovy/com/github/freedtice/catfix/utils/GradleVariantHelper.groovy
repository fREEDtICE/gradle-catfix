package com.github.freedtice.catfix.utils

import com.android.build.gradle.api.BaseVariant

class GradleVariantHelper {
  public static boolean isFlavor(BaseVariant variant) {
    return !TextUtils.isEmpty(variant.flavorName)
  }
}