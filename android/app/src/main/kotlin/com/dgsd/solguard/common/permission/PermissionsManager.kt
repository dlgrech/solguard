package com.dgsd.solguard.common.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

class PermissionsManager(
  private val context: Context,
) {

  fun hasNotificationsPermission(): Boolean {
    return Build.VERSION.SDK_INT < 33 || hasPermission(Manifest.permission.POST_NOTIFICATIONS)
  }

  private fun hasPermission(permission: String): Boolean {
    val result = ContextCompat.checkSelfPermission(context, permission)
    return result  == PackageManager.PERMISSION_GRANTED
  }
}