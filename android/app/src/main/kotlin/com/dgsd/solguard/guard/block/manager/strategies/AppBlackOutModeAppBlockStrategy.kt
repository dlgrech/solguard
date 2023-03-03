package com.dgsd.solguard.guard.block.manager.strategies

import com.dgsd.solguard.common.clock.Clock
import com.dgsd.solguard.data.AppLaunchRepository
import com.dgsd.solguard.guard.block.manager.AppBlockStrategy

/**
 * [AppBlockStrategy] to ensure we block apps that have blackout mode enabled
 */
internal class AppBlackOutModeAppBlockStrategy(
  private val clock: Clock,
  private val appLaunchRepository: AppLaunchRepository,
) : AppBlockStrategy {

  @Suppress("DEPRECATION")
  override suspend fun shouldBlock(packageName: String): AppBlockStrategy.Result {
    val blackoutModeEvent = appLaunchRepository.getAppBlackoutEvent(
      clock.now().toLocalDate(),
      packageName
    )
    return if (blackoutModeEvent?.isEnabled == true) {
      AppBlockStrategy.Result.BLOCK
    } else {
      AppBlockStrategy.Result.FALLTHROUGH
    }
  }
}