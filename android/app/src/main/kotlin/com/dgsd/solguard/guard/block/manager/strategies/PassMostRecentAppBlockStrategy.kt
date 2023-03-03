package com.dgsd.solguard.guard.block.manager.strategies

import com.dgsd.solguard.data.AppLaunchRepository
import com.dgsd.solguard.guard.block.manager.AppBlockStrategy

/**
 * [AppBlockStrategy] to ensure we dont block any app launchers
 */
internal class PassMostRecentAppBlockStrategy(
  private val appLaunchRepository: AppLaunchRepository,
) : AppBlockStrategy {

  @Suppress("DEPRECATION")
  override suspend fun shouldBlock(packageName: String): AppBlockStrategy.Result {
    val mostRecentAppLaunched = appLaunchRepository.getMostRecentAppLaunch().value
    return if (mostRecentAppLaunched == packageName) {
      AppBlockStrategy.Result.PASS
    } else {
      AppBlockStrategy.Result.FALLTHROUGH
    }
  }
}