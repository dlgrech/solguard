package com.dgsd.solguard.common.date

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

fun LocalDateTime.toEpochMillis(): Long {
  val seconds = this.toEpochSecond(OffsetDateTime.now().offset)
  return TimeUnit.SECONDS.toMillis(seconds)
}

fun LocalDateTime.toEpochDay(): Long {
  return toLocalDate().toEpochDay()
}

fun Long.toLocalDateTime(): LocalDateTime {
  val seconds = TimeUnit.MILLISECONDS.toSeconds(this)
  val offset = OffsetDateTime.now().offset

  return LocalDateTime.ofEpochSecond(seconds, 0, offset)
}