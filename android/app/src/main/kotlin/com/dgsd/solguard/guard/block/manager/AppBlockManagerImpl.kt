package com.dgsd.solguard.guard.block.manager

import android.content.SharedPreferences
import androidx.core.content.edit
import com.dgsd.solguard.common.clock.Clock
import com.dgsd.solguard.data.AppLaunchRepository
import java.time.LocalDate

internal class AppBlockManagerImpl(
  strategyFactory: AppBlockStrategyFactory,
  private val appLaunchRepository: AppLaunchRepository,
  private val clock: Clock,
  private val sharedPreferences: SharedPreferences,
) : AppBlockManager {

  private val strategies = strategyFactory.createStrategies()

  override suspend fun shouldBlockApp(appPackage: String): Boolean {
    return when (getShouldBlockAppResult(appPackage)) {
      AppBlockStrategy.Result.BLOCK -> true
      AppBlockStrategy.Result.PASS -> false
      AppBlockStrategy.Result.FALLTHROUGH -> false
    }
  }

  override suspend fun shouldNotifyOfHeavyUse(appPackage: String): Boolean {
    if (getShouldBlockAppResult(appPackage) != AppBlockStrategy.Result.FALLTHROUGH) {
      return false
    }

    if (appLaunchRepository.getAppLaunchGuard(appPackage) != null) {
      return false
    }

    val today = clock.now().toLocalDate()

    if (hasShownHeavyUseNotificationRecently(appPackage, today)) {
      return false
    }

    val todaysRecord = appLaunchRepository.getAppLaunchHistoricalRecord(appPackage, today)
    if (todaysRecord.launchCount >= LAUNCH_LIMIT_BEFORE_WARNING_OF_HEAVY_USE) {
      sharedPreferences.edit { putBoolean(createHeavyUseNotificationKey(appPackage, today), true) }
      return true
    }

    return false
  }

  private fun hasShownHeavyUseNotificationRecently(appPackage: String, today: LocalDate): Boolean {
    for (i in 0 until NOTIFICATION_FOR_SAME_APP_LIMIT_DAYS) {
      val key = createHeavyUseNotificationKey(appPackage, today.minusDays(i.toLong()))
      if (sharedPreferences.getBoolean(key, false)) {
        return true
      }
    }

    return false
  }

  override suspend fun recordAppLaunch(appPackage: String) {
    appLaunchRepository.recordAppLaunch(appPackage)
  }

  private suspend fun getShouldBlockAppResult(appPackage: String): AppBlockStrategy.Result {
    for (strategy in strategies) {
      val result = strategy.shouldBlock(appPackage)
      if (result != AppBlockStrategy.Result.FALLTHROUGH) {
        return result
      }
    }

    return AppBlockStrategy.Result.FALLTHROUGH
  }

  private fun createHeavyUseNotificationKey(
    appPackage: String,
    date: LocalDate
  ): String {
    return "${PREF_KEY_HAS_SHOWN_HEAVY_USE_NOTIFICATION_TEMPLATE}_${appPackage}_${date.toEpochDay()}"
  }

  companion object {
    const val LAUNCH_LIMIT_BEFORE_WARNING_OF_HEAVY_USE = 20
    const val NOTIFICATION_FOR_SAME_APP_LIMIT_DAYS = 7
    const val PREF_KEY_HAS_SHOWN_HEAVY_USE_NOTIFICATION_TEMPLATE = "has_shown_heavy_use_notif"
  }
}