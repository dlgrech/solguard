package com.dgsd.solguard.common.clock

import java.time.LocalDateTime

object SystemClock : Clock {
  override fun now(): LocalDateTime {
    return LocalDateTime.now()
  }
}