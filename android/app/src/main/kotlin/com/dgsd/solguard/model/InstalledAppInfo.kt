package com.dgsd.solguard.model

import android.graphics.drawable.Drawable
import androidx.palette.graphics.Palette

data class InstalledAppInfo(
  val packageName: String,
  val displayName: CharSequence,
  val appIcon: Drawable,
  val iconPalette: Palette
)