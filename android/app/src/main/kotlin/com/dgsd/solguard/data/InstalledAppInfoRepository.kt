package com.dgsd.solguard.data

import com.dgsd.solguard.common.resource.model.Resource
import com.dgsd.solguard.model.InstalledAppInfo
import kotlinx.coroutines.flow.Flow

interface InstalledAppInfoRepository {

  suspend fun getInstalledAppInfo(packageName: String): InstalledAppInfo?

  suspend fun isInstalled(packageName: String): Boolean

  fun getInstalledApp(packageName: String): Flow<Resource<InstalledAppInfo>>

  fun getInstalledApps(packageNames: List<String>): Flow<Resource<List<InstalledAppInfo>>>

  fun getInstalledApps(): Flow<Resource<List<InstalledAppInfo>>>
}