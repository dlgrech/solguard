package com.dgsd.solguard.common.clock

import java.time.LocalDateTime

interface Clock {

  fun now(): LocalDateTime
}