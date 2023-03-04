package com.dgsd.solguard.di

import com.dgsd.solguard.AppCoordinator
import com.dgsd.solguard.applock.BiometricChallengeViewModel
import com.dgsd.solguard.charity.CharityInfoViewModel
import com.dgsd.solguard.guard.blackout.EnableBlackoutModeViewModel
import com.dgsd.solguard.guard.block.ui.AppBlockViewModel
import com.dgsd.solguard.guard.create.AppSelectionViewModel
import com.dgsd.solguard.guard.create.CreateGuardViewModel
import com.dgsd.solguard.guard.create.TokenAmountViewModel
import com.dgsd.solguard.history.HistoryViewModel
import com.dgsd.solguard.home.HomeViewModel
import com.dgsd.solguard.model.TokenAmount
import com.dgsd.solguard.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.Module
import org.koin.dsl.module

object ViewModelModule {

  fun create(): Module {
    return module {
      viewModelOf(::AppCoordinator)
      viewModelOf(::HomeViewModel)
      viewModelOf(::HistoryViewModel)
      viewModelOf(::SettingsViewModel)
      viewModelOf(::EnableBlackoutModeViewModel)
      viewModelOf(::BiometricChallengeViewModel)
      viewModelOf(::AppSelectionViewModel)

      viewModel { (tokenAmount: TokenAmount?) ->
        TokenAmountViewModel(
          application = get(),
          appSettingsRepository = get(),
          tokenAmount = tokenAmount,
        )
      }

      viewModel { (charityId: String) ->
        CharityInfoViewModel(
          application = get(),
          charityId = charityId,
          appConfigRepository = get(),
          errorMessageFactory = get(),
        )
      }

      viewModel { (packageName: String, isForBlackoutDisableOnly: Boolean, isForInApp: Boolean) ->
        AppBlockViewModel(
          application = get(),
          packageName = packageName,
          isForBlackoutDisableOnly = isForBlackoutDisableOnly,
          isForInApp = isForInApp,
          clock = get(),
          solanaApi = get(),
          errorMessageFactory = get(),
          installedAppInfoRepository = get(),
          appLaunchRepository = get(),
          appConfigRepository = get(),
          appSettingsRepository = get(),
          appBlockNotificationManager = get(),
        )
      }

      viewModel { (packageToEdit: String?) ->
        CreateGuardViewModel(
          application = get(),
          editGuardForPackageName = packageToEdit,
          errorMessageFactory = get(),
          appLaunchRepository = get(),
          appInfoRepository = get(),
        )
      }
    }
  }
}