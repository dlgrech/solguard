package com.dgsd.solguard.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.os.Process
import androidx.palette.graphics.Palette
import com.dgsd.solguard.common.flow.resourceFlowOf
import com.dgsd.solguard.common.resource.model.Resource
import com.dgsd.solguard.common.ui.toBitmap
import com.dgsd.solguard.model.InstalledAppInfo
import kotlinx.coroutines.flow.Flow

class InstalledAppInfoRepositoryImpl(
  private val context: Context,
) : InstalledAppInfoRepository {

  private val packageManager = context.packageManager
  private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

  private var installedAppsCache: List<InstalledAppInfo>? = null

  override fun getInstalledApp(packageName: String): Flow<Resource<InstalledAppInfo>> {
    return resourceFlowOf { getAppInfo(packageName) }
  }

  override fun getInstalledApps(packageNames: List<String>): Flow<Resource<List<InstalledAppInfo>>> {
    return resourceFlowOf { packageNames.map(::getAppInfo) }
  }

  override fun getInstalledApps(): Flow<Resource<List<InstalledAppInfo>>> {
    return resourceFlowOf(cachedValue = installedAppsCache) {
      launcherApps.getActivityList(null, Process.myUserHandle())
        .asSequence()
        .map { it.applicationInfo }
        .filterNot { it.packageName == context.packageName }
        .distinctBy { it.packageName }
        .map { it.toAppInfo() }
        .sortedBy { it.displayName.trim().toString().lowercase() }
        .toList()
        .also { installedAppsCache = it }
    }
  }

  override suspend fun isInstalled(packageName: String): Boolean {
    return getAppInfoOrNull(packageName) != null
  }

  override suspend fun getInstalledAppInfo(packageName: String): InstalledAppInfo? {
    return launcherApps.getActivityList(packageName, Process.myUserHandle())
      .asSequence()
      .map { it.applicationInfo }
      .distinctBy { it.packageName }
      .filterNot { it.packageName == context.packageName }
      .map { it.toAppInfo() }
      .singleOrNull()
  }

  private fun getAppInfo(packageName: String): InstalledAppInfo {
    return checkNotNull(getAppInfoOrNull(packageName))
  }

  private fun getAppInfoOrNull(packageName: String): InstalledAppInfo? {
    return launcherApps.getApplicationInfo(packageName, 0, Process.myUserHandle())?.toAppInfo()
  }

  private fun ApplicationInfo.toAppInfo(): InstalledAppInfo {
    val appIcon = packageManager.getApplicationIcon(packageName)
    val appIconBitmap = appIcon.toBitmap()
    val palette = try {
      Palette.from(appIconBitmap).generate()
    } finally {
      appIconBitmap.recycle()
    }

    return InstalledAppInfo(
      packageName = packageName,
      displayName = loadLabel(packageManager),
      appIcon = appIcon,
      iconPalette = palette
    )
  }
}