package com.dgsd.solguard.onboarding.mwacheck

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dgsd.solguard.common.resource.ResourceFlowConsumer
import com.dgsd.solguard.model.InstalledAppInfo
import com.dgsd.solguard.mwa.MobileWalletAdapterAvailabilityManager
import kotlinx.coroutines.flow.filterNotNull

class OnboardingMwaCheckViewModel(
  application: Application,
  private val mwaAvailabilityManager: MobileWalletAdapterAvailabilityManager
): AndroidViewModel(application) {

  private val installedWalletsResourceConsumer =
    ResourceFlowConsumer<List<InstalledAppInfo>>(viewModelScope)

  val installedWallets = installedWalletsResourceConsumer.data.filterNotNull()

  val showLoadingState = installedWalletsResourceConsumer.isLoading

  init {
    installedWalletsResourceConsumer.collectFlow(
      mwaAvailabilityManager.getInstalledMwaCompatibleWallets()
    )
  }
}