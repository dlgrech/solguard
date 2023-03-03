package com.dgsd.solguard.model

import androidx.annotation.DrawableRes
import com.dgsd.ksol.core.model.PublicKey
import com.dgsd.solguard.R

enum class PaymentToken(
  val account: PublicKey,
  val displayName: String,
  @DrawableRes val icon: Int
) {

  NATIVE_SOL(
    account = PublicKey.fromBase58("So11111111111111111111111111111111111111112"),
    displayName = "SOL",
    icon = R.drawable.token_icon_sol
  ),

  BONK(
    account = PublicKey.fromBase58("DezXAZ8z7PnrnRJjz3wXBoRgixCa6xjnB7YaB1pPB263"),
    displayName = "BONK",
    icon = R.drawable.token_icon_bonk
  ),

  USDC(
    account = PublicKey.fromBase58("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"),
    displayName = "USDC",
    icon = R.drawable.token_icon_usdc,
  );

  companion object {
    fun fromAccountAddress(account: PublicKey): PaymentToken? {
      return values().firstOrNull { it.account == account }
    }
  }
}