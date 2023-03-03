package com.dgsd.solguard.common.amount

import com.dgsd.ksol.core.model.Lamports
import com.dgsd.ksol.core.utils.isValidSolAmount
import com.dgsd.ksol.core.utils.solToLamports
import java.math.BigDecimal

fun String.toSolAmountOrNull(): Lamports? {
  return runCatching {
    BigDecimal(this)
  }.mapCatching {
    if (it.isValidSolAmount()) {
      it.solToLamports()
    } else {
      null
    }
  }.getOrNull()
}