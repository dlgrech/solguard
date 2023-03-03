package com.dgsd.solguard.applock.biometrics

import android.app.Application
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt

class AppLockBiometricManagerImpl(
  private val application: Application,
) : AppLockBiometricManager {

  private val manager = BiometricManager.from(application)

  override fun isAvailableOnDevice(): Boolean {
    return manager.canAuthenticate(VALID_AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS
  }

  override fun createPrompt(
    title: CharSequence,
    description: CharSequence?
  ): BiometricPrompt.PromptInfo {
    return BiometricPrompt.PromptInfo.Builder()
      .setAllowedAuthenticators(VALID_AUTHENTICATORS)
      .setConfirmationRequired(false)
      .setTitle(title)
      .setDescription(description)
      .setNegativeButtonText(
        application.getString(android.R.string.cancel)
      )
      .build()
  }

  companion object {

    private const val VALID_AUTHENTICATORS = BiometricManager.Authenticators.BIOMETRIC_STRONG
  }
}