package com.dgsd.solguard.model

import java.time.LocalDate

data class AppLaunchEvent(
  val packageName: String,
  val date: LocalDate,
  val launchCount: Long
) : HistoricalRecord