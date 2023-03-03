package com.dgsd.solguard.deeplink

import android.content.Context
import android.content.Intent
import android.net.Uri

object SolGuardDeeplinkingFactory {

  fun createEnableBlackoutMode(context: Context): Intent {
    return Intent(
      Intent.ACTION_VIEW,
      Uri.Builder()
        .scheme(SolGuardDeeplinkingConstants.SCHEME)
        .authority(SolGuardDeeplinkingConstants.DestinationHosts.ENABLE_BLACKOUT_MODE)
        .build()
    ).setPackage(context.packageName)
  }

  fun createNewGuard(context: Context, packageName: String): Intent {
    return Intent(
      Intent.ACTION_VIEW,
      Uri.Builder()
        .scheme(SolGuardDeeplinkingConstants.SCHEME)
        .authority(SolGuardDeeplinkingConstants.DestinationHosts.CREATE_NEW_GUARD)
        .appendPath(packageName)
        .build()
    ).setPackage(context.packageName)
  }
}