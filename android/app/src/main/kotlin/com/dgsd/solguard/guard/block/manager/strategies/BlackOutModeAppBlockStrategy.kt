package com.dgsd.solguard.guard.block.manager.strategies

import com.dgsd.solguard.common.clock.Clock
import com.dgsd.solguard.data.AppLaunchRepository
import com.dgsd.solguard.guard.block.manager.AppBlockStrategy

/**
 * [AppBlockStrategy] to ensure we block apps if blackout mode is enabled
 */
internal class BlackOutModeAppBlockStrategy(
  private val clock: Clock,
  private val appLaunchRepository: AppLaunchRepository,
) : AppBlockStrategy {

  @Suppress("DEPRECATION")
  override suspend fun shouldBlock(packageName: String): AppBlockStrategy.Result {
    val blackoutModeEvent = appLaunchRepository.getBlackoutModeEvent(clock.now().toLocalDate())
    return if (blackoutModeEvent?.isEnabled == true) {
      AppBlockStrategy.Result.BLOCK
    } else {
      AppBlockStrategy.Result.FALLTHROUGH
    }
  }
}