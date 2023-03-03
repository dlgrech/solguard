package com.dgsd.solguard.model

import java.time.LocalDate

data class BlackoutModeEvent(
  val date: LocalDate,
  val amount: TokenAmount,
  val isEnabled: Boolean,
) : HistoricalRecord