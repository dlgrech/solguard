package com.dgsd.solguard.model

import com.dgsd.ksol.core.model.TransactionSignature
import java.time.LocalDateTime

/**
 * Represents the settings for what is allowed for a specific Android package
 */
data class UnlockAppEvent(
  val packageName: String,
  val timestamp: LocalDateTime,
  val transactionSignature: TransactionSignature,
  val launchNumber: Int,
  val amount: TokenAmount,
): HistoricalRecord