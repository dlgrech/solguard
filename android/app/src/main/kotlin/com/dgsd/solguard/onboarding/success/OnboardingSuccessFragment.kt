package com.dgsd.solguard.onboarding.success

import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.dgsd.solguard.R
import com.dgsd.solguard.common.flow.onEach
import com.dgsd.solguard.common.ui.SwallowBackpressLifecycleObserver
import com.dgsd.solguard.di.util.parentViewModel
import com.dgsd.solguard.onboarding.OnboardingCoordinator
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class OnboardingSuccessFragment : Fragment(R.layout.frag_onboarding_success) {

  private val coordinator by parentViewModel<OnboardingCoordinator>()
  private val viewModel by viewModel<OnboardingSuccessViewModel> {
    parametersOf(
      checkNotNull(coordinator.createGuardAppInfo),
      checkNotNull(coordinator.amount),
      checkNotNull(coordinator.appLaunchLimit),
    )
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    SwallowBackpressLifecycleObserver.attach(this)

    val loadingIndicator = view.requireViewById<View>(R.id.loading_indicator)
    val content = view.requireViewById<View>(R.id.content)
    val confetti = view.requireViewById<LottieAnimationView>(R.id.confetti)
    val nextButton = view.requireViewById<View>(R.id.next_button)

    nextButton.setOnClickListener {
      coordinator.navigateToNextStep()
    }

    onEach(viewModel.isLoading) {
      loadingIndicator.isVisible = it
      content.isInvisible = it
    }

    onEach(viewModel.guardSavedSuccessfully) {
      confetti.playAnimation()
    }
  }

  companion object {

    fun newInstance() = OnboardingSuccessFragment()
  }
}