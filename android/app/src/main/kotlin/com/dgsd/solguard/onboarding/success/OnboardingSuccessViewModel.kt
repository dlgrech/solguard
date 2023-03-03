package com.dgsd.solguard.onboarding.success

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dgsd.solguard.common.flow.asEventFlow
import com.dgsd.solguard.common.resource.ResourceFlowConsumer
import com.dgsd.solguard.data.AppLaunchRepository
import com.dgsd.solguard.model.AppLaunchGuard
import com.dgsd.solguard.model.InstalledAppInfo
import com.dgsd.solguard.model.TokenAmount
import kotlinx.coroutines.flow.filterNotNull

class OnboardingSuccessViewModel(
  application: Application,
  private val appInfo: InstalledAppInfo,
  private val launchLimit: Int,
  private val amount: TokenAmount,
  private val appLaunchRepository: AppLaunchRepository,
) : AndroidViewModel(application) {

  private val saveGuardResourceFlowConsumer = ResourceFlowConsumer<Unit>(viewModelScope)

  val isLoading = saveGuardResourceFlowConsumer.isLoading

  val guardSavedSuccessfully =
    saveGuardResourceFlowConsumer.data.filterNotNull().asEventFlow(viewModelScope)

  init {
    saveGuardResourceFlowConsumer.collectFlow {
      appLaunchRepository.saveAppLaunchGuard(
        AppLaunchGuard(
          packageName = appInfo.packageName,
          numberOfLaunchesPerDay = launchLimit,
          amount = amount,
          isEnabled = true
        )
      )
    }
  }
}