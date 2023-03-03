package com.dgsd.solguard.common.intent

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

class IntentFactory(private val context: Context) {

  fun createHomeIntent(): Intent {
    return Intent(Intent.ACTION_MAIN)
      .addCategory(Intent.CATEGORY_HOME)
      .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
  }

  fun createUrlIntent(url: String): Intent {
    return Intent(Intent.ACTION_VIEW)
      .setData(Uri.parse(url))
      .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
  }

  fun createAccessibilitySettingsIntent(): Intent {
    return Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
      .addCategory(Intent.CATEGORY_DEFAULT)
  }

  fun createAppNotificationSettingsIntent(): Intent {
    return Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
      .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
  }
}