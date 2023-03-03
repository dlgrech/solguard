package com.dgsd.solguard.common.actionsheet.model

import android.graphics.drawable.Drawable

data class ActionSheetItem(
  val title: CharSequence,
  val icon: Drawable? = null,
  val onClick: () -> Unit,
)