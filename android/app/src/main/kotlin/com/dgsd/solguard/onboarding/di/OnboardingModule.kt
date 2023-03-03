package com.dgsd.solguard.onboarding.di

import com.dgsd.solguard.onboarding.OnboardingCoordinator
import com.dgsd.solguard.onboarding.amountinput.OnboardingAmountInputViewModel
import com.dgsd.solguard.onboarding.applaunchlimit.OnboardingAppLaunchLimitViewModel
import com.dgsd.solguard.onboarding.appselection.OnboardingAppSelectionViewModel
import com.dgsd.solguard.onboarding.biometrics.OnboardingBiometricsViewModel
import com.dgsd.solguard.onboarding.mwacheck.OnboardingMwaCheckViewModel
import com.dgsd.solguard.onboarding.permissions.OnboardingGrantNotificationPermissionsViewModel
import com.dgsd.solguard.onboarding.permissions.OnboardingGrantPermissionsViewModel
import com.dgsd.solguard.onboarding.success.OnboardingSuccessViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.Module
import org.koin.dsl.module

object OnboardingModule {

  fun create(): Module {
    return module {
      viewModelOf(::OnboardingCoordinator)
      viewModelOf(::OnboardingMwaCheckViewModel)
      viewModelOf(::OnboardingGrantPermissionsViewModel)
      viewModelOf(::OnboardingGrantNotificationPermissionsViewModel)
      viewModelOf(::OnboardingAppSelectionViewModel)
      viewModelOf(::OnboardingAppLaunchLimitViewModel)
      viewModelOf(::OnboardingAmountInputViewModel)
      viewModelOf(::OnboardingBiometricsViewModel)
      viewModelOf(::OnboardingSuccessViewModel)
    }
  }
}