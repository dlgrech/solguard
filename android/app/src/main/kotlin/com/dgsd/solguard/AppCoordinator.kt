package com.dgsd.solguard

import android.app.Application
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import com.dgsd.solguard.applock.biometrics.AppLockBiometricManager
import com.dgsd.solguard.common.flow.MutableEventFlow
import com.dgsd.solguard.common.flow.asEventFlow
import com.dgsd.solguard.deeplink.SolGuardDeeplinkingConstants

private const val HAS_SEEN_ONBOARDING = "has_seen_onboarding"

class AppCoordinator(
  application: Application,
  private val sharedPreferences: SharedPreferences,
  private val appLockBiometricManager: AppLockBiometricManager,
) : AndroidViewModel(application) {

  sealed interface Destination {

    sealed interface InlineDestination : Destination {
      object Onboarding : InlineDestination
      object HowItWorks : InlineDestination
      object Home : InlineDestination
      object History : InlineDestination
      object Settings : InlineDestination
      object BiometricChallenge : InlineDestination
      object CreateNew : InlineDestination
      data class EditGuard(val packageName: String) : InlineDestination
    }

    sealed interface BottomSheetDestination : Destination {
      object EnableBlackoutMode : BottomSheetDestination
      object DisableBlackoutMode : BottomSheetDestination
      data class DisableAppBlackoutMode(val packageName: String) : BottomSheetDestination
      data class AppBlock(val packageName: String) : BottomSheetDestination
    }

    data class CompositeDestination(
      val destinations: List<Destination>
    ) : Destination
  }

  private val _destination = MutableEventFlow<Destination>()
  val destination = _destination.asEventFlow()

  private var pendingDeeplinkAfterAppLock: Uri? = null

  private var isShowingAppLock = false

  fun onCreate() {
    if (!hasCompletedOnboarding()) {
      _destination.tryEmit(Destination.InlineDestination.Onboarding)
    } else {
      if (appLockBiometricManager.isAvailableOnDevice()) {
        _destination.tryEmit(Destination.InlineDestination.BiometricChallenge)
      } else {
        navigateToHome()
      }
    }
  }

  fun onResume(incomingDeeplink: Uri?) {
    if (isShowingAppLock) {
      pendingDeeplinkAfterAppLock = incomingDeeplink
    } else if (incomingDeeplink != null) {
      maybeNavigateWithUri(incomingDeeplink)
    }
  }

  fun onNewIntent(incomingDeeplink: Uri?) {
    if (incomingDeeplink != null) {
      maybeNavigateWithUri(incomingDeeplink)
    }
  }

  fun navigateAfterAppLock() {
    isShowingAppLock = false

    val deeplink = pendingDeeplinkAfterAppLock
    pendingDeeplinkAfterAppLock = null
    if (deeplink == null || !maybeNavigateWithUri(deeplink)) {
      navigateToHome()
    }
  }

  fun navigateToHome() {
    sharedPreferences.edit { putBoolean(HAS_SEEN_ONBOARDING, true) }
    _destination.tryEmit(Destination.InlineDestination.Home)
  }

  fun navigateToHowItWorks() {
    _destination.tryEmit(Destination.InlineDestination.HowItWorks)
  }

  fun navigateToSettings() {
    _destination.tryEmit(Destination.InlineDestination.Settings)
  }

  fun navigateToHistory() {
    _destination.tryEmit(Destination.InlineDestination.History)
  }

  fun navigateToNewGuard() {
    _destination.tryEmit(Destination.InlineDestination.CreateNew)
  }

  fun navigateToEditGuard(packageName: String) {
    _destination.tryEmit(Destination.InlineDestination.EditGuard(packageName))
  }

  fun navigateToDisableAppBlackoutMode(packageName: String) {
    _destination.tryEmit(Destination.BottomSheetDestination.DisableAppBlackoutMode(packageName))
  }

  fun navigateToEnableBlackoutMode() {
    _destination.tryEmit(Destination.BottomSheetDestination.EnableBlackoutMode)
  }

  fun navigateToDisableBlackoutMode() {
    _destination.tryEmit(Destination.BottomSheetDestination.DisableBlackoutMode)
  }

  fun navigateToAppBlock(packageName: String) {
    _destination.tryEmit(Destination.BottomSheetDestination.AppBlock(packageName))
  }

  private fun hasCompletedOnboarding(): Boolean {
    return sharedPreferences.getBoolean(HAS_SEEN_ONBOARDING, false)
  }

  private fun maybeNavigateWithUri(incomingDeeplink: Uri): Boolean {
    if (!hasCompletedOnboarding()) {
      // We're not logged in
      return false
    }

    if (incomingDeeplink.scheme == SolGuardDeeplinkingConstants.SCHEME) {
      if (maybeHandleAppSpecificDeeplink(incomingDeeplink)) {
        return true
      }
    }

    return false
  }

  private fun maybeHandleAppSpecificDeeplink(incomingDeeplink: Uri): Boolean {
    val destination = when (incomingDeeplink.host) {
      SolGuardDeeplinkingConstants.DestinationHosts.ENABLE_BLACKOUT_MODE -> {
        Destination.CompositeDestination(
          listOf(
            Destination.InlineDestination.Home,
            Destination.BottomSheetDestination.EnableBlackoutMode
          )
        )
      }
      SolGuardDeeplinkingConstants.DestinationHosts.CREATE_NEW_GUARD -> {
        val packageName = incomingDeeplink.pathSegments.firstOrNull()
        if (packageName == null) {
          null
        } else {
          Destination.InlineDestination.EditGuard(packageName)
        }
      }
      else -> null
    }

    return if (destination != null) {
      _destination.tryEmit(destination)
      true
    } else {
      false
    }
  }
}
