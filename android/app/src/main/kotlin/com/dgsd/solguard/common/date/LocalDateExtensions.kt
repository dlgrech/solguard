package com.dgsd.solguard.common.date

import java.time.LocalDate
import java.time.LocalDateTime

fun LocalDate.atEndOfDay(): LocalDateTime {
  return this.plusDays(1).atStartOfDay().minusSeconds(1)
}