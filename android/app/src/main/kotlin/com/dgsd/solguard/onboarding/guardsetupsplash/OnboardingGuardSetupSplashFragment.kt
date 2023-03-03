package com.dgsd.solguard.onboarding.guardsetupsplash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.dgsd.solguard.R
import com.dgsd.solguard.common.ui.SwallowBackpressLifecycleObserver
import com.dgsd.solguard.di.util.parentViewModel
import com.dgsd.solguard.onboarding.OnboardingCoordinator

class OnboardingGuardSetupSplashFragment : Fragment(R.layout.frag_onboarding_guard_setup_splash) {

  private val coordinator by parentViewModel<OnboardingCoordinator>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    SwallowBackpressLifecycleObserver.attach(this)

    view.requireViewById<View>(R.id.next_button).setOnClickListener {
      coordinator.navigateToNextStep()
    }
  }

  companion object {

    fun newInstance() = OnboardingGuardSetupSplashFragment()
  }
}