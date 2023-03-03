package com.dgsd.solguard.mwa

import android.content.Context
import android.content.pm.PackageManager
import com.dgsd.solguard.common.resource.model.Resource
import com.dgsd.solguard.data.InstalledAppInfoRepository
import com.dgsd.solguard.model.InstalledAppInfo
import com.solana.mobilewalletadapter.clientlib.associationDetails
import com.solana.mobilewalletadapter.clientlib.scenario.LocalAssociationIntentCreator
import com.solana.mobilewalletadapter.clientlib.scenario.LocalAssociationScenario
import kotlinx.coroutines.flow.Flow

@Suppress("DEPRECATION")
class MobileWalletAdapterAvailabilityManagerImpl(
  private val context: Context,
  private val installedAppInfoRepository: InstalledAppInfoRepository,
) : MobileWalletAdapterAvailabilityManager {

  private val mwaIntent by lazy {
    val details = LocalAssociationScenario(0).associationDetails()
    LocalAssociationIntentCreator.createAssociationIntent(
      details.uriPrefix,
      details.port,
      details.session
    )
  }

  override fun getInstalledMwaCompatibleWallets(): Flow<Resource<List<InstalledAppInfo>>> {
    return installedAppInfoRepository.getInstalledApps(getMwaWalletPackageNames())
  }

  override suspend fun getInstalledMwaCompatibleWalletPackages(): Set<String> {
    return getMwaWalletPackageNames().toSet()
  }

  private fun getMwaWalletPackageNames(): List<String> {
    val activities = context.packageManager.queryIntentActivities(
      mwaIntent,
      PackageManager.MATCH_ALL
    )

    return activities.map { it.activityInfo.packageName }
  }
}
