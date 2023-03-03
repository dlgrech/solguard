package com.dgsd.solguard.guard.block.manager

import android.content.Context
import com.dgsd.solguard.common.clock.Clock
import com.dgsd.solguard.data.AppLaunchRepository
import com.dgsd.solguard.guard.block.manager.strategies.*
import com.dgsd.solguard.mwa.MobileWalletAdapterAvailabilityManager

internal class AppBlockStrategyFactory(
  private val context: Context,
  private val clock: Clock,
  private val appLaunchRepository: AppLaunchRepository,
  private val mwaAvailabilityManager: MobileWalletAdapterAvailabilityManager,
) {

  fun createStrategies(): List<AppBlockStrategy> {
    return listOf(
      NotSelfAppBlockStrategy(context),
      NotLaunchersAppBlockStrategy(context),
      NotMwaWalletsAppBlockStrategy(mwaAvailabilityManager),
      PassMostRecentAppBlockStrategy(appLaunchRepository),
      BlackOutModeAppBlockStrategy(clock, appLaunchRepository),
      AppBlackOutModeAppBlockStrategy(clock, appLaunchRepository),
      UnblockedAppsAppBlockStrategy(clock, appLaunchRepository),
      AppLimitExceededAppBlockStrategy(clock, appLaunchRepository)
    )
  }
}