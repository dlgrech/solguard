package com.dgsd.solguard.onboarding.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.dgsd.solguard.AppCoordinator
import com.dgsd.solguard.R
import com.dgsd.solguard.di.util.parentViewModel
import com.dgsd.solguard.onboarding.OnboardingCoordinator
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class OnboardingSplashFragment : Fragment(R.layout.frag_onboarding_splash) {

  private val appCoordinator by activityViewModel<AppCoordinator>()
  private val coordinator by parentViewModel<OnboardingCoordinator>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    view.requireViewById<View>(R.id.next_button).setOnClickListener {
      coordinator.navigateToNextStep()
    }

    view.requireViewById<View>(R.id.how_it_works).setOnClickListener {
      appCoordinator.navigateToHowItWorks()
    }
  }

  companion object {

    fun newInstance() = OnboardingSplashFragment()
  }
}