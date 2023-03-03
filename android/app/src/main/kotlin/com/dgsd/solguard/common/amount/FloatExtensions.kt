package com.dgsd.solguard.common.amount

import com.dgsd.ksol.core.model.Lamports
import com.dgsd.ksol.core.utils.isValidSolAmount
import java.math.BigDecimal

fun Float.toLamportsOrNull(): Lamports? {
  return runCatching {
    BigDecimal(this.toDouble())
  }.map {
    if (it.isValidSolAmount()) {
      it.longValueExact()
    } else {
      null
    }
  }.getOrNull()
}