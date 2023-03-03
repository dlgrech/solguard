package com.dgsd.solguard.model

import java.time.LocalDate

data class BlackoutAppEvent(
  val date: LocalDate,
  val packageName: String,
  val amount: TokenAmount,
  val isEnabled: Boolean,
) : HistoricalRecord