package com.dgsd.solguard.guard.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dgsd.solguard.R
import com.dgsd.solguard.common.clock.Clock
import com.dgsd.solguard.common.date.toEpochMillis
import com.dgsd.solguard.data.AppLaunchRepository
import com.dgsd.solguard.data.AppSettingsRepository
import com.dgsd.solguard.data.InstalledAppInfoRepository
import com.dgsd.solguard.deeplink.SolGuardDeeplinkingFactory
import com.dgsd.solguard.guard.unlock.AppBlockUnlockConstants
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import kotlin.random.Random

data class AppBlockNotificationManager(
  private val context: Context,
  private val installedAppInfoRepository: InstalledAppInfoRepository,
  private val appLaunchRepository: AppLaunchRepository,
  private val appSettingsRepository: AppSettingsRepository,
  private val clock: Clock,
) {

  private val notificationManager = NotificationManagerCompat.from(context)

  suspend fun showAppUnlockedNotification(packageName: String, unlockTime: LocalDateTime) {
    if (!appSettingsRepository.getAppSettingsSync().showTimeRemainingWhileUnlocked) {
      return
    }

    val displayName = installedAppInfoRepository.getInstalledAppInfo(packageName)?.displayName
    if (displayName == null) {
      return
    }

    val channel = ensureAppUnlockedNotificationChannelCreated()

    val notification = NotificationCompat.Builder(context, channel.id)
      .setAutoCancel(true)
      .setDefaults(NotificationCompat.DEFAULT_ALL)
      .setSmallIcon(R.drawable.ic_notification_icon)
      .setChronometerCountDown(true)
      .setUsesChronometer(true)
      .setOngoing(true)
      .setTimeoutAfter(TimeUnit.MINUTES.toMillis(AppBlockUnlockConstants.MINUTES_TO_UNBLOCK_AFTER_UNLOCK.toLong()))
      .setWhen(unlockTime.plusMinutes(AppBlockUnlockConstants.MINUTES_TO_UNBLOCK_AFTER_UNLOCK.toLong()).toEpochMillis())
      .setContentTitle(
        "$displayName is unlocked!"
      )
      .setContentText(
        "Not for long though..."
      )
      .build()

    notificationManager.notify(Random.nextInt(), notification)
  }

  suspend fun showHeavyUseNotification(packageName: String) {
    if (!appSettingsRepository.getAppSettingsSync().showSuggestedGuardNotifications) {
      return
    }

    val displayName = installedAppInfoRepository.getInstalledAppInfo(packageName)?.displayName
    if (displayName == null) {
      return
    }

    val launchCount = appLaunchRepository.getAppLaunchHistoricalRecord(
      packageName,
      clock.now().toLocalDate()
    ).launchCount

    val channel = ensureHeavyUseNotificationChannelCreated()

    val notification = NotificationCompat.Builder(context, channel.id)
      .setAutoCancel(true)
      .setDefaults(NotificationCompat.DEFAULT_ALL)
      .setSmallIcon(R.drawable.ic_notification_icon)
      .setContentTitle(
        context.getString(
          R.string.notification_title_heavy_use_template,
          displayName,
          launchCount.toString()
        )
      )
      .setContentText(
        context.getString(R.string.notification_title_heavy_use_description)
      )
      .setContentIntent(
        PendingIntent.getActivity(
          context,
          Random.nextInt(),
          SolGuardDeeplinkingFactory.createNewGuard(context, packageName),
          PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
      )
      .build()

    notificationManager.notify(Random.nextInt(), notification)
  }

  private fun ensureHeavyUseNotificationChannelCreated(): NotificationChannelCompat {
    val channel = NotificationChannelCompat.Builder(
      CHANNEL_ID_HEAVY_USE,
      NotificationManager.IMPORTANCE_DEFAULT
    )
      .setName(
        context.getString(R.string.notification_channel_heavy_use_name)
      )
      .setDescription(
        context.getString(R.string.notification_channel_heavy_use_description)
      )
      .build()
    notificationManager.createNotificationChannel(channel)
    return channel
  }

  private fun ensureAppUnlockedNotificationChannelCreated(): NotificationChannelCompat {
    val channel = NotificationChannelCompat.Builder(
      CHANNEL_ID_APP_UNLOCKED,
      NotificationManager.IMPORTANCE_DEFAULT
    )
      .setName(
        context.getString(R.string.notification_channel_app_unlocked_name)
      )
      .setDescription(
        context.getString(R.string.notification_channel_app_unlocked_description)
      )
      .build()
    notificationManager.createNotificationChannel(channel)
    return channel
  }

  companion object {

    private const val CHANNEL_ID_APP_UNLOCKED = "app_unlocked"
    private const val CHANNEL_ID_HEAVY_USE = "heavy_use"
  }
}