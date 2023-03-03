package com.dgsd.solguard.common.ui

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat

@ColorInt
fun Context.getColorAttr(@AttrRes resId: Int): Int {
  val typedValue = getAttrAsTypedValue(resId)
  return if (typedValue.resourceId == 0) {
    if (
      typedValue.data == TypedValue.DATA_NULL_UNDEFINED ||
      typedValue.data == TypedValue.DATA_NULL_EMPTY
    ) {
      error("Could not find attribute in theme: ${resources.getResourceName(resId)}")
    } else {
      typedValue.data
    }
  } else {
    ContextCompat.getColor(this, typedValue.resourceId)
  }
}

private fun Context.getAttrAsTypedValue(@AttrRes id: Int): TypedValue {
  val resolvedAttr = TypedValue()
  theme.resolveAttribute(id, resolvedAttr, true)
  return resolvedAttr
}