package com.dgsd.solguard.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.dgsd.solguard.applock.biometrics.AppLockBiometricManager
import com.dgsd.solguard.common.flow.MutableEventFlow
import com.dgsd.solguard.common.flow.SimpleMutableEventFlow
import com.dgsd.solguard.common.flow.asEventFlow
import com.dgsd.solguard.common.flow.call
import com.dgsd.solguard.common.permission.PermissionsManager
import com.dgsd.solguard.common.viewmodel.onEach
import com.dgsd.solguard.model.InstalledAppInfo
import com.dgsd.solguard.model.TokenAmount

class OnboardingCoordinator(
  application: Application,
  private val permissionsManager: PermissionsManager,
  private val appLockBiometricManager: AppLockBiometricManager,
) : AndroidViewModel(application) {

  enum class Destination {
    Splash,
    GrantPermissions,
    GrantNotificationPermissions,
    MobileWalletAdapterCheck,
    GuardSetupSplash,
    AppSelection,
    AppLaunchLimit,
    AmountInput,
    BiometricsSetup,
    Success,
  }

  private val _destination = MutableEventFlow<Destination>()
  val destination = _destination.asEventFlow()

  private val _continueIntoApp = SimpleMutableEventFlow()
  val continueInApp = _continueIntoApp.asEventFlow()

  private var lastDestination: Destination? = null

  var createGuardAppInfo: InstalledAppInfo? = null
    private set

  var appLaunchLimit: Int? = null
    private set

  var amount: TokenAmount? = null
    private set

  fun onCreate() {
    onEach(destination) {
      lastDestination = it
    }

    _destination.tryEmit(Destination.Splash)
  }

  fun onBackStackChanged(destination: Destination) {
    lastDestination = destination
  }

  fun navigateWithApp(appInfo: InstalledAppInfo) {
    createGuardAppInfo = appInfo
    navigateToNextStep()
  }

  fun navigateWithAppLaunchLimit(limit: Int) {
    appLaunchLimit = limit
    navigateToNextStep()
  }

  fun navigateWithAmount(amount: TokenAmount) {
    this.amount = amount
    navigateToNextStep()
  }

  fun navigateToNextStep() {
    val nextStep = when (lastDestination) {
      null -> Destination.Splash
      Destination.Splash -> Destination.MobileWalletAdapterCheck
      Destination.MobileWalletAdapterCheck -> Destination.GrantPermissions
      Destination.GrantPermissions -> {
        if (permissionsManager.hasNotificationsPermission()) {
          Destination.GuardSetupSplash
        } else {
          Destination.GrantNotificationPermissions
        }
      }
      Destination.GrantNotificationPermissions -> Destination.GuardSetupSplash
      Destination.GuardSetupSplash -> Destination.AppSelection
      Destination.AppSelection -> Destination.AppLaunchLimit
      Destination.AppLaunchLimit -> Destination.AmountInput
      Destination.AmountInput -> {
        if (appLockBiometricManager.isAvailableOnDevice()) {
          Destination.BiometricsSetup
        } else {
          Destination.Success
        }
      }
      Destination.BiometricsSetup -> Destination.Success
      Destination.Success -> null
    }

    if (nextStep == null) {
      _continueIntoApp.call()
    } else {
      _destination.tryEmit(nextStep)
    }
  }
}