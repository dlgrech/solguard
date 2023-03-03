package com.dgsd.solguard.guard.block.manager.strategies

import com.dgsd.solguard.common.clock.Clock
import com.dgsd.solguard.data.AppLaunchRepository
import com.dgsd.solguard.guard.block.manager.AppBlockStrategy
import com.dgsd.solguard.guard.unlock.AppBlockUnlockConstants

/**
 * [AppBlockStrategy] to ensure we don't block any apps that have just been unlocked
 */
internal class UnblockedAppsAppBlockStrategy(
  private val clock: Clock,
  private val appLaunchRepository: AppLaunchRepository,
) : AppBlockStrategy {

  @Suppress("DEPRECATION")
  override suspend fun shouldBlock(packageName: String): AppBlockStrategy.Result {
    val now = clock.now()
    val unlockEvents = appLaunchRepository.getUnlockEvent(
      packageName = packageName,
      fromTime = now.minusMinutes(AppBlockUnlockConstants.MINUTES_TO_UNBLOCK_AFTER_UNLOCK.toLong()),
      toTime = now
    )

    return if (unlockEvents.isEmpty()) {
      AppBlockStrategy.Result.FALLTHROUGH
    } else {
      AppBlockStrategy.Result.PASS
    }
  }
}