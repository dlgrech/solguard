package com.dgsd.solguard.home

import android.content.Context
import com.dgsd.solguard.home.model.HomeItem
import com.dgsd.solguard.model.AppLaunchEvent
import com.dgsd.solguard.model.AppLaunchGuard
import com.dgsd.solguard.model.BlackoutAppEvent
import com.dgsd.solguard.model.InstalledAppInfo

internal object HomeItemFactory {

  suspend fun create(
    context: Context,
    isAppBlockServiceEnabled: Boolean,
    installedApps: List<InstalledAppInfo>,
    appLaunchGuards: List<AppLaunchGuard>,
    todaysHistory: List<AppLaunchEvent>,
    appBlackoutRecords: List<BlackoutAppEvent>,
  ): List<HomeItem> {
    val packageNameToAppInfo = installedApps.associateBy { it.packageName }
    val packageNameToHistory = todaysHistory.associateBy { it.packageName }
    val packageNameToBlackoutEvent = appBlackoutRecords.associateBy { it.packageName }

    val items = mutableListOf<HomeItem>()

    if (!isAppBlockServiceEnabled) {
      items += HomeItem.EnableAccessibilityServiceItem
    }

    val guardsWithData = appLaunchGuards.mapNotNull { appLaunchGuard ->
      val appInfo = packageNameToAppInfo[appLaunchGuard.packageName]
      val history = packageNameToHistory[appLaunchGuard.packageName]
      val appBlackoutEvent = packageNameToBlackoutEvent[appLaunchGuard.packageName]

      if (appInfo == null) {
        null
      } else {
        arrayOf(appLaunchGuard, appInfo, history, appBlackoutEvent)
      }
    }.sortedBy { (_, appInfo, _, _) ->
      (appInfo as InstalledAppInfo).displayName.toString()
    }

    if (guardsWithData.isNotEmpty()) {
      items += guardsWithData.map { (appLaunchGuard, appInfo, history, appBlackoutEvent) ->
        create(
          appInfo = appInfo as InstalledAppInfo,
          appLaunchGuard = appLaunchGuard as AppLaunchGuard,
          numberOfLaunchesToday = (history as? AppLaunchEvent)?.launchCount?.toInt() ?: 0,
          isBlackedOut = (appBlackoutEvent as? BlackoutAppEvent)?.isEnabled == true
        )
      }
    }

    return items
  }

  private fun create(
    appInfo: InstalledAppInfo,
    appLaunchGuard: AppLaunchGuard,
    numberOfLaunchesToday: Int,
    isBlackedOut: Boolean,
  ): HomeItem {
    return HomeItem.AppLaunchGuardItem(
      packageName = appLaunchGuard.packageName,
      displayName = appInfo.displayName,
      numberOfLaunchesAllowedPerDay = appLaunchGuard.numberOfLaunchesPerDay,
      amount = appLaunchGuard.amount,
      isGuardEnabled = appLaunchGuard.isEnabled,
      numberOfLaunchesToday = numberOfLaunchesToday,
      isBlackedOut = isBlackedOut,
      icon = appInfo.appIcon,
      palette = appInfo.iconPalette,
    )
  }
}