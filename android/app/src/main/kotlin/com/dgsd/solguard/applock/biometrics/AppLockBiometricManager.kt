package com.dgsd.solguard.applock.biometrics

import androidx.biometric.BiometricPrompt

interface AppLockBiometricManager {

  fun isAvailableOnDevice(): Boolean

  fun createPrompt(
    title: CharSequence,
    description: CharSequence? = null
  ): BiometricPrompt.PromptInfo
}