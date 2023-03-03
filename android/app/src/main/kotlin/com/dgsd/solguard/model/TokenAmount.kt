package com.dgsd.solguard.model

import com.dgsd.ksol.core.model.Lamports
import com.dgsd.ksol.core.model.PublicKey

data class TokenAmount(
  val amount: Lamports,
  val tokenAddress: PublicKey,
)