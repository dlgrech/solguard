package com.dgsd.solguard.common.ui

import android.content.Context
import com.dgsd.solguard.R
import com.dgsd.solguard.common.clock.Clock
import com.dgsd.solguard.common.date.toEpochDay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object DateFormatter {

  private val mediumDateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

  fun formatRelativeDateAndTime(
    context: Context,
    clock: Clock,
    date: LocalDate
  ): CharSequence {
    val now = clock.now()
    val today = now.toEpochDay()
    return if (today == date.toEpochDay()) {
      context.getString(R.string.common_date_time_today)
    } else if ((today - 1) == date.toEpochDay()) {
      context.getString(R.string.common_date_time_yesterday)
    } else {
      mediumDateFormatter.format(date)
    }
  }
}