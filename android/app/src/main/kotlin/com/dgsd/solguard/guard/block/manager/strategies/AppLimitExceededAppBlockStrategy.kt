package com.dgsd.solguard.guard.block.manager.strategies

import com.dgsd.solguard.common.clock.Clock
import com.dgsd.solguard.data.AppLaunchRepository
import com.dgsd.solguard.guard.block.manager.AppBlockStrategy

/**
 * [AppBlockStrategy] to ensure we block apps that have exceeded their launch limit
 */
internal class AppLimitExceededAppBlockStrategy(
  private val clock: Clock,
  private val appLaunchRepository: AppLaunchRepository,
) : AppBlockStrategy {

  @Suppress("DEPRECATION")
  override suspend fun shouldBlock(packageName: String): AppBlockStrategy.Result {
    val guard = appLaunchRepository.getAppLaunchGuard(packageName)
    if (guard == null || !guard.isEnabled) {
      return AppBlockStrategy.Result.FALLTHROUGH
    }

    val historicalRecord = appLaunchRepository.getAppLaunchHistoricalRecord(packageName, clock.now().toLocalDate())
    return if (historicalRecord.launchCount >= guard.numberOfLaunchesPerDay) {
      AppBlockStrategy.Result.BLOCK
    } else {
      AppBlockStrategy.Result.FALLTHROUGH
    }
  }
}