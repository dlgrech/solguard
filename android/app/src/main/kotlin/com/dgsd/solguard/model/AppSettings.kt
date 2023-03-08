package com.dgsd.solguard.model

import com.dgsd.ksol.core.model.Cluster

data class AppSettings(
  val cluster: Cluster,
  val defaultPaymentToken: PaymentToken,
  val increasePaymentOnEachUnblock: Boolean,
  val showWarningOnLastLaunch: Boolean,
  val showTimeRemainingWhileUnlocked: Boolean,
  val showSuggestedGuardNotifications: Boolean,
  val donationToSolGuardPercentage: Float,
)