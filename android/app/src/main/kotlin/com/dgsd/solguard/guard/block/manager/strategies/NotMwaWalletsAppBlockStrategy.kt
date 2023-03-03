package com.dgsd.solguard.guard.block.manager.strategies

import com.dgsd.solguard.guard.block.manager.AppBlockStrategy
import com.dgsd.solguard.mwa.MobileWalletAdapterAvailabilityManager

/**
 * [AppBlockStrategy] to ensure we dont block any MWA wallets (which we may need access to to unlock)
 */
internal class NotMwaWalletsAppBlockStrategy(
  private val mwaAvailabilityManager: MobileWalletAdapterAvailabilityManager,
) : AppBlockStrategy {

  @Suppress("DEPRECATION")
  override suspend fun shouldBlock(packageName: String): AppBlockStrategy.Result {
    val walletPackages = mwaAvailabilityManager.getInstalledMwaCompatibleWalletPackages()
    return if (packageName in walletPackages) {
      AppBlockStrategy.Result.PASS
    } else {
      AppBlockStrategy.Result.FALLTHROUGH
    }
  }
}