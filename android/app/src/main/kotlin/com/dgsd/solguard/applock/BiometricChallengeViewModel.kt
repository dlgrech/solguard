package com.dgsd.solguard.applock

import android.app.Application
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.AndroidViewModel
import com.dgsd.solguard.R
import com.dgsd.solguard.applock.biometrics.AppLockBiometricManager
import com.dgsd.solguard.common.flow.MutableEventFlow
import com.dgsd.solguard.common.flow.SimpleMutableEventFlow
import com.dgsd.solguard.common.flow.asEventFlow
import com.dgsd.solguard.common.flow.call
import com.dgsd.solguard.common.viewmodel.getString

class BiometricChallengeViewModel(
  application: Application,
  private val appLockBiometricManager: AppLockBiometricManager,
) : AndroidViewModel(application) {

  private val _showBiometricPrompt = MutableEventFlow<BiometricPrompt.PromptInfo>()
  val showBiometricPrompt = _showBiometricPrompt.asEventFlow()

  private val _continueIntoApp = SimpleMutableEventFlow()
  val continueIntoApp = _continueIntoApp.asEventFlow()

  fun onStart() {
    showBiometricPrompt()
  }

  fun onUnlockClicked() {
    showBiometricPrompt()
  }

  fun onAppLockBiometricResult(success: Boolean) {
    if (success) {
      _continueIntoApp.call()
    }
  }

  private fun showBiometricPrompt() {
    _showBiometricPrompt.tryEmit(
      appLockBiometricManager.createPrompt(
        title = getString(R.string.app_lock_entry_biometric_prompt_title),
        description = getString(R.string.app_lock_entry_biometric_prompt_message),
      )
    )
  }
}