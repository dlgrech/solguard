package com.dgsd.solguard.common.ui

import android.graphics.*
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.Px

private val GREYSCALE_COLOR_FILTER = ColorMatrixColorFilter(
  ColorMatrix(
    floatArrayOf(
      0.33f, 0.33f, 0.33f, 0f, 0f,
      0.33f, 0.33f, 0.33f, 0f, 0f,
      0.33f, 0.33f, 0.33f, 0f, 0f,
      0f, 0f, 0f, 1f, 0f
    )
  )
)

fun View.roundedCorners(
  @Px radius: Float,
  cornerRoundingMode: CornerRoundingMode = CornerRoundingMode.ALL_CORNERS,
) {
  clipToOutline = cornerRoundingMode != CornerRoundingMode.NONE
  outlineProvider = object : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline?) {
      when (cornerRoundingMode) {
        CornerRoundingMode.ALL_CORNERS -> {
          outline?.setRoundRect(0, 0, view.width, view.height, radius)
        }
        CornerRoundingMode.TOP_CORNERS -> {
          outline?.setRoundRect(0, 0, view.width, view.height + radius.toInt(), radius)
        }
        CornerRoundingMode.BOTTOM_CORNERS -> {
          outline?.setRoundRect(0, -radius.toInt(), view.width, view.height, radius)
        }
        else -> Unit
      }
    }
  }
}

fun View.circle() {
  clipToOutline = true
  outlineProvider = object : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline?) {
      outline?.setRoundRect(
        0,
        0,
        view.width,
        view.height,
        maxOf(view.width, view.height) / 2f
      )
    }
  }
}

@Suppress("DEPRECATION")
fun View.performHapticFeedback() {
  performHapticFeedback(
    HapticFeedbackConstants.VIRTUAL_KEY_RELEASE,
    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
  )
}

fun View.blur() {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    setRenderEffect(
      RenderEffect.createBlurEffect(30f, 30f, Shader.TileMode.CLAMP)
    )
  }
}

fun View.unblur() {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    setRenderEffect(null)
  }
}

fun View.setGreyscale(enabled: Boolean) {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    if (enabled) {
      setRenderEffect(RenderEffect.createColorFilterEffect(GREYSCALE_COLOR_FILTER))
    } else {
      setRenderEffect(null)
    }
  }
}