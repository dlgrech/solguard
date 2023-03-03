package com.dgsd.solguard.guard.block.manager.strategies

import android.content.Context
import android.content.Intent
import com.dgsd.solguard.guard.block.manager.AppBlockStrategy

/**
 * [AppBlockStrategy] to ensure we dont block any app launchers
 */
internal class NotLaunchersAppBlockStrategy(private val context: Context) : AppBlockStrategy {

  @Suppress("DEPRECATION")
  override suspend fun shouldBlock(packageName: String): AppBlockStrategy.Result {
    val resolveInfos = context.packageManager.queryIntentActivities(launcherIntent, 0)
    val isLauncher = resolveInfos.any { resolveInfo ->
      resolveInfo.activityInfo.packageName == packageName
    }

    return if (isLauncher) {
      AppBlockStrategy.Result.PASS
    } else {
      AppBlockStrategy.Result.FALLTHROUGH
    }
  }

  private companion object {

    private val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
  }
}