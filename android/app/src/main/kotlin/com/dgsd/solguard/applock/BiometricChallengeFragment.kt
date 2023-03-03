package com.dgsd.solguard.applock

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.dgsd.solguard.AppCoordinator
import com.dgsd.solguard.R
import com.dgsd.solguard.applock.biometrics.BiometricPromptResult
import com.dgsd.solguard.applock.biometrics.showBiometricPrompt
import com.dgsd.solguard.common.flow.onEach
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class BiometricChallengeFragment : Fragment(R.layout.frag_biometric_challenge) {

  private val appCoordinator by activityViewModel<AppCoordinator>()
  private val viewModel by viewModel<BiometricChallengeViewModel>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    view.requireViewById<View>(R.id.unlock_button).setOnClickListener {
      viewModel.onUnlockClicked()
    }

    onEach(viewModel.showBiometricPrompt) {
      val result = showBiometricPrompt(it)
      viewModel.onAppLockBiometricResult(result == BiometricPromptResult.SUCCESS)
    }

    onEach(viewModel.continueIntoApp) {
      appCoordinator.navigateAfterAppLock()
    }
  }

  override fun onStart() {
    super.onStart()
    viewModel.onStart()
  }

  companion object {

    fun newInstance() = BiometricChallengeFragment()
  }
}