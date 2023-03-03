package com.dgsd.solguard.common.ui

import android.icu.text.NumberFormat
import com.dgsd.ksol.core.model.LAMPORTS_IN_SOL
import com.dgsd.solguard.model.PaymentToken
import com.dgsd.solguard.model.TokenAmount

object PaymentTokenFormatter {

  private val numberFormatter = NumberFormat.getNumberInstance().apply {
    minimumFractionDigits = 0
    maximumFractionDigits = 9
    minimumIntegerDigits = 1
  }

  fun formatAmount(amount: TokenAmount): CharSequence {
    return numberFormatter.format(
      amount.amount.toBigDecimal().divide(LAMPORTS_IN_SOL)
    )
  }

  fun format(tokenAmount: TokenAmount): CharSequence {
    val tokenName = PaymentToken.fromAccountAddress(tokenAmount.tokenAddress)?.displayName.orEmpty()
    return "${formatAmount(tokenAmount)} $tokenName"
  }
}