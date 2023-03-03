package com.dgsd.solguard.onboarding.biometrics

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.dgsd.solguard.R
import com.dgsd.solguard.applock.biometrics.BiometricPromptResult
import com.dgsd.solguard.applock.biometrics.showBiometricPrompt
import com.dgsd.solguard.common.flow.onEach
import com.dgsd.solguard.common.fragment.navigateBack
import com.dgsd.solguard.di.util.parentViewModel
import com.dgsd.solguard.onboarding.OnboardingCoordinator
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class OnboardingBiometricsFragment : Fragment(R.layout.frag_onboarding_biometrics) {

  private val coordinator by parentViewModel<OnboardingCoordinator>()
  private val viewModel by viewModel<OnboardingBiometricsViewModel>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val lottieView = view.requireViewById<LottieAnimationView>(R.id.lottie_view)
    lottieView.setMaxFrame(ANIMATION_END_OF_FINGERPRINT_FRAME)

    view.requireViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
      navigateBack()
    }

    view.requireViewById<View>(R.id.next_button).setOnClickListener {
      viewModel.onNextClicked()
    }

    viewLifecycleOwner.lifecycleScope.launchWhenResumed {
      lottieView.playAnimation()
    }

    onEach(viewModel.showBiometricPrompt) { promptInfo ->
      val result = showBiometricPrompt(promptInfo)
      if (result == BiometricPromptResult.SUCCESS) {
        lottieView.addAnimatorListener(
          object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
              viewLifecycleOwner.lifecycleScope.launch {
                delay(200)
                coordinator.navigateToNextStep()
              }
            }
          }
        )
        lottieView.setMaxFrame(ANIMATION_END_OF_CHECKMARK_FRAME)
        lottieView.resumeAnimation()
      }
    }
  }

  companion object {

    private const val ANIMATION_END_OF_FINGERPRINT_FRAME = 42
    private const val ANIMATION_END_OF_CHECKMARK_FRAME = 102

    fun newInstance() = OnboardingBiometricsFragment()
  }
}