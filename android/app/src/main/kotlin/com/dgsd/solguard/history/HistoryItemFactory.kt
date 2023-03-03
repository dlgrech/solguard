package com.dgsd.solguard.history

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import com.dgsd.solguard.R
import com.dgsd.solguard.common.clock.Clock
import com.dgsd.solguard.common.ui.CornerRoundingMode
import com.dgsd.solguard.common.ui.DateFormatter
import com.dgsd.solguard.common.ui.PaymentTokenFormatter
import com.dgsd.solguard.common.ui.bold
import com.dgsd.solguard.data.InstalledAppInfoRepository
import com.dgsd.solguard.history.model.HistoryItem
import com.dgsd.solguard.model.*
import java.time.LocalDate

object HistoryItemFactory {

  suspend fun create(
    context: Context,
    clock: Clock,
    installedAppInfoRepository: InstalledAppInfoRepository,
    records: List<HistoricalRecord>,
  ): List<HistoryItem> {
    val packageNameToDisplayNameCache = mutableMapOf<String, CharSequence?>()

    return buildList {
      val dateToRecords = records.groupBy { it.date() }.toList()
      dateToRecords.forEachIndexed { index, (date, records) ->
        val blackoutModeEvent = records.filterIsInstance<BlackoutModeEvent>().firstOrNull()
        val unlockEvents = records.filterIsInstance<UnlockAppEvent>()
        val usedApps = records.filterIsInstance<AppLaunchEvent>()

        val (summary, appIcons) = if (blackoutModeEvent != null) {
          createSummaryForBlackoutMode(context, blackoutModeEvent) to emptyList<Drawable>()
        } else if (unlockEvents.isNotEmpty()) {
          createSummaryAndAppIconsForUnlockEvents(
            context,
            installedAppInfoRepository,
            packageNameToDisplayNameCache,
            unlockEvents,
            usedApps
          )
        } else {
          createSummaryAndAppIconsForUsedApps(
            context,
            installedAppInfoRepository,
            packageNameToDisplayNameCache,
            usedApps,
          )
        }

        add(
          HistoryItem(
            date = date,
            title = DateFormatter.formatRelativeDateAndTime(context, clock, date),
            summary = summary,
            appIcons = appIcons,
            cornerRoundingMode = CornerRoundingMode.compute(dateToRecords.size, index)
          )
        )
      }
    }
  }

  private fun createSummaryForBlackoutMode(
    context: Context,
    blackoutModeEvent: BlackoutModeEvent,
  ): CharSequence {
    return if (blackoutModeEvent.isEnabled) {
      context.getString(R.string.history_tab_summary_blackout_mode_enabled)
    } else {
      TextUtils.expandTemplate(
        context.getString(R.string.history_tab_summary_blackout_mode_disabled_template),
        PaymentTokenFormatter.format(blackoutModeEvent.amount).bold()
      )
    }
  }

  private suspend fun createSummaryAndAppIconsForUnlockEvents(
    context: Context,
    appInfoRepository: InstalledAppInfoRepository,
    displayNameCache: MutableMap<String, CharSequence?>,
    unlockEvents: List<UnlockAppEvent>,
    usedApps: List<AppLaunchEvent>,
  ): Pair<CharSequence, List<Drawable>> {
    val unlockEventAnnotated = unlockEvents.mapNotNull {
      val displayName = appInfoRepository.getDisplayNameOrNull(displayNameCache, it.packageName)
      val icon = context.getAppIconOrNull(it.packageName)

      if (displayName == null || icon == null) {
        null
      } else {
        Triple(it, displayName, icon)
      }
    }

    return if (unlockEventAnnotated.isEmpty()) {
      createSummaryAndAppIconsForUsedApps(context, appInfoRepository, displayNameCache, usedApps)
    } else {
      val summary = when (unlockEventAnnotated.size) {
        1 -> {
          val (unlockEvent, displayName) = unlockEventAnnotated.first()
          TextUtils.expandTemplate(
            context.getString(R.string.history_tab_summary_unlock_event_1),
            PaymentTokenFormatter.format(unlockEvent.amount).bold(),
            displayName.bold()
          )
        }
        2 -> {
          val appNames = unlockEventAnnotated.map { it.second }
          TextUtils.expandTemplate(
            context.getString(R.string.history_tab_summary_unlock_event_2),
            appNames[0].bold(),
            appNames[1].bold()
          )
        }
        3 -> {
          val appNames = unlockEventAnnotated.map { it.second }
          TextUtils.expandTemplate(
            context.getString(R.string.history_tab_summary_unlock_event_3),
            appNames[0].bold(),
            appNames[1].bold()
          )
        }
        else -> {
          val appNames = unlockEventAnnotated.map { it.second }
          TextUtils.expandTemplate(
            context.resources.getQuantityString(
              R.plurals.history_tab_summary_unlock_event_multiple,
              appNames.size - 3
            ),
            appNames[0].bold(),
            appNames[1].bold(),
            appNames[2].bold(),
            (appNames.size - 3).toString().bold(),
          )
        }
      }

      val icons = unlockEventAnnotated.take(3).map { it.third }

      summary to icons
    }
  }

  private suspend fun createSummaryAndAppIconsForUsedApps(
    context: Context,
    appInfoRepository: InstalledAppInfoRepository,
    displayNameCache: MutableMap<String, CharSequence?>,
    usedApps: List<AppLaunchEvent>,
  ): Pair<CharSequence, List<Drawable>> {
    val usedAppsAnnotated = usedApps.mapNotNull {
      val displayName = appInfoRepository.getDisplayNameOrNull(displayNameCache, it.packageName)
      val icon = context.getAppIconOrNull(it.packageName)

      if (displayName == null || icon == null) {
        null
      } else {
        Triple(it, displayName, icon)
      }
    }.sortedBy { it.first.launchCount }

    val summary = when (usedAppsAnnotated.size) {
      0 -> {
        context.getString(R.string.history_tab_summary_empty)
      }
      1 -> {
        val (appLaunch, displayName) = usedAppsAnnotated.first()
        TextUtils.expandTemplate(
          context.resources.getQuantityString(
            R.plurals.history_tab_summary_used_app_1,
            appLaunch.launchCount.toInt(),
          ),
          displayName.bold(),
          appLaunch.launchCount.toString()
        )
      }
      2 -> {
        val appNames = usedAppsAnnotated.map { it.second }
        TextUtils.expandTemplate(
          context.getString(R.string.history_tab_summary_used_app_2),
          appNames[0].bold(),
          appNames[1].bold(),
        )
      }
      3 -> {
        val appNames = usedAppsAnnotated.map { it.second }
        TextUtils.expandTemplate(
          context.getString(R.string.history_tab_summary_used_app_3),
          appNames[0].bold(),
          appNames[1].bold(),
          appNames[2].bold(),
        )
      }
      else -> {
        val appNames = usedAppsAnnotated.map { it.second }
        TextUtils.expandTemplate(
          context.resources.getQuantityString(
            R.plurals.history_tab_summary_used_app_multiple,
            appNames.size - 3
          ),
          appNames[0].bold(),
          appNames[1].bold(),
          appNames[2].bold(),
          (appNames.size - 3).toString().bold(),
        )
      }
    }

    val icons = usedAppsAnnotated.take(3).map { it.third }

    return summary to icons
  }

  private fun Context.getAppIconOrNull(packageName: String): Drawable? {
    return runCatching { packageManager.getApplicationIcon(packageName) }.getOrNull()
  }

  private suspend fun InstalledAppInfoRepository.getDisplayNameOrNull(
    cache: MutableMap<String, CharSequence?>,
    packageName: String,
  ): CharSequence? {
    return cache.getOrPut(packageName) { getInstalledAppInfo(packageName)?.displayName }
  }

  private fun HistoricalRecord.date(): LocalDate {
    return when (this) {
      is AppLaunchEvent -> date
      is BlackoutModeEvent -> date
      is BlackoutAppEvent -> date
      is UnlockAppEvent -> timestamp.toLocalDate()
    }
  }
}