package com.dgsd.solguard.model

data class AppSettings(
  val defaultPaymentToken: PaymentToken,
  val increasePaymentOnEachUnblock: Boolean,
  val showWarningOnLastLaunch: Boolean,
  val showTimeRemainingWhileUnlocked: Boolean,
  val showSuggestedGuardNotifications: Boolean,
  val donationToSolGuardPercentage: Float,
)