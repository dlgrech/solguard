package com.dgsd.solguard.common.ui

import android.icu.text.NumberFormat

object PercentageFormatter {

  private val percentageFormatter = NumberFormat.getPercentInstance().apply {
    maximumFractionDigits = 2
  }

  fun format(amount: Float): String {
    return percentageFormatter.format(amount)
  }
}