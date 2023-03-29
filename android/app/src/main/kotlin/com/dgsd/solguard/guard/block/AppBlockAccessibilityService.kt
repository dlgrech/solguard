package com.dgsd.solguard.guard.block

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import com.dgsd.solguard.guard.block.manager.AppBlockManager
import com.dgsd.solguard.guard.block.ui.AppBlockActivity
import com.dgsd.solguard.guard.notifications.AppBlockNotificationManager
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

class AppBlockAccessibilityService : AccessibilityService() {

  private val appBlockManager by inject<AppBlockManager>()
  private val notificationManager by inject<AppBlockNotificationManager>()

  private val coroutineScope = CoroutineScope(Dispatchers.Default + Job())

  private var lastJob: Job? = null

  override fun onInterrupt() = Unit

  override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
      // Unknown accessibility event
      return
    }

    if (!event.isFullScreen) {
      // We only consider fullscreen events
      return
    }


    if (event.className == INPUT_SERVICE_CLASS_NAME) {
      // Ignore soft keyboard window
      return
    }

    val packageName = event.packageName?.toString()
    if (packageName == null) {
      // Don't know what app is being launched
      return
    }

    lastJob?.cancel()
    lastJob = coroutineScope.launch {
      val shouldBlock = appBlockManager.shouldBlockApp(packageName)
      val showHeavyUseNotification = appBlockManager.shouldNotifyOfHeavyUse(packageName)

      appBlockManager.recordAppLaunch(packageName)

      if (isActive) {
        if (shouldBlock) {
          startActivity(
            AppBlockActivity.getLaunchIntent(
              context = this@AppBlockAccessibilityService,
              packageNameBeingBlocked = packageName,
              homeOnDismiss = true
            )
          )
        } else if (showHeavyUseNotification) {
          notificationManager.showHeavyUseNotification(packageName)
        }
      }
    }
  }

  companion object {
    private const val INPUT_SERVICE_CLASS_NAME = "android.inputmethodservice.SoftInputWindow"

    fun isEnabled(context: Context): Boolean {
      val enabledServicesString = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
      ).orEmpty()

      return enabledServicesString.contains(context.packageName)
    }
  }
}