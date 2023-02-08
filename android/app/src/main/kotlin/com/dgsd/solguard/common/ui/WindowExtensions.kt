package com.dgsd.solguard.common.ui

import android.os.Build
import android.view.Window
import android.view.WindowManager

/**
 * On devices that support it, enables the blurring of the window background
 */
fun Window.enableBackgroundBlur() {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
    attributes = attributes?.apply { blurBehindRadius = 30 }
  }
}