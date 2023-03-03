package com.dgsd.solguard.onboarding.appselection

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dgsd.solguard.common.error.ErrorMessageFactory
import com.dgsd.solguard.common.flow.asEventFlow
import com.dgsd.solguard.common.flow.mapData
import com.dgsd.solguard.common.resource.ResourceFlowConsumer
import com.dgsd.solguard.common.ui.CornerRoundingMode
import com.dgsd.solguard.data.InstalledAppInfoRepository
import com.dgsd.solguard.model.InstalledAppInfo
import com.dgsd.solguard.mwa.MobileWalletAdapterAvailabilityManager
import com.dgsd.solguard.onboarding.appselection.model.OnboardingAppSelectionItem
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

class OnboardingAppSelectionViewModel(
  application: Application,
  private val errorMessageFactory: ErrorMessageFactory,
  private val installedAppInfoRepository: InstalledAppInfoRepository,
  private val mwaAvailabilityManager: MobileWalletAdapterAvailabilityManager,
) : AndroidViewModel(application) {

  private val installedAppsResourceConsumer =
    ResourceFlowConsumer<List<InstalledAppInfo>>(viewModelScope)

  val isLoading = installedAppsResourceConsumer.isLoadingWithNoData

  val errorMessage = installedAppsResourceConsumer.error.mapNotNull { error ->
    if (error == null) {
      null
    } else {
      errorMessageFactory.create(error)
    }
  }.asEventFlow(viewModelScope)

  val appSelectionItems = installedAppsResourceConsumer.data
    .filterNotNull()
    .map { installedApps ->
      installedApps.mapIndexed { index, appInfo ->
        OnboardingAppSelectionItem(
          appInfo = appInfo,
          cornerRounding = CornerRoundingMode.compute(
            totalItemCount = installedApps.size,
            itemIndex = index
          )
        )
      }
    }

  fun onStart() {
    installedAppsResourceConsumer.collectFlow(
      installedAppInfoRepository.getInstalledApps().mapData { installedApps ->
        val walletPackages = mwaAvailabilityManager.getInstalledMwaCompatibleWalletPackages()
        installedApps.filterNot { it.packageName in walletPackages }
      }
    )
  }
}