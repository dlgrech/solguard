package com.dgsd.solguard.onboarding.biometrics

import android.app.Application
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.AndroidViewModel
import com.dgsd.solguard.R
import com.dgsd.solguard.applock.biometrics.AppLockBiometricManager
import com.dgsd.solguard.common.flow.MutableEventFlow
import com.dgsd.solguard.common.flow.asEventFlow
import com.dgsd.solguard.common.viewmodel.getString

class OnboardingBiometricsViewModel(
  application: Application,
  private val appLockBiometricManager: AppLockBiometricManager,
) : AndroidViewModel(application) {

  private val _showBiometricPrompt = MutableEventFlow<BiometricPrompt.PromptInfo>()
  val showBiometricPrompt = _showBiometricPrompt.asEventFlow()

  fun onNextClicked() {
    _showBiometricPrompt.tryEmit(
      appLockBiometricManager.createPrompt(
        title = getString(R.string.app_lock_entry_biometric_prompt_title),
        description = getString(R.string.app_lock_entry_biometric_prompt_message),
      )
    )
  }
}