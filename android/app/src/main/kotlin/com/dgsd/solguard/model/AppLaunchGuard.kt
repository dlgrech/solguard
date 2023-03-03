package com.dgsd.solguard.model

/**
 * Represents the settings for what is allowed for a specific Android package
 */
data class AppLaunchGuard(
  val packageName: String,
  val numberOfLaunchesPerDay: Int,
  val amount: TokenAmount,
  val isEnabled: Boolean,
)