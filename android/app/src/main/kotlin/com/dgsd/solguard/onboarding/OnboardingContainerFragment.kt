package com.dgsd.solguard.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dgsd.solguard.AppCoordinator
import com.dgsd.solguard.R
import com.dgsd.solguard.common.flow.onEach
import com.dgsd.solguard.common.fragment.model.ScreenTransitionType
import com.dgsd.solguard.common.fragment.navigate
import com.dgsd.solguard.onboarding.OnboardingCoordinator.Destination
import com.dgsd.solguard.onboarding.amountinput.OnboardingAmountInputFragment
import com.dgsd.solguard.onboarding.applaunchlimit.OnboardingAppLaunchLimitFragment
import com.dgsd.solguard.onboarding.appselection.OnboardingAppSelectionFragment
import com.dgsd.solguard.onboarding.biometrics.OnboardingBiometricsFragment
import com.dgsd.solguard.onboarding.guardsetupsplash.OnboardingGuardSetupSplashFragment
import com.dgsd.solguard.onboarding.mwacheck.OnboardingMwaCheckFragment
import com.dgsd.solguard.onboarding.permissions.OnboardingGrantNotificationPermissionsFragment
import com.dgsd.solguard.onboarding.permissions.OnboardingGrantPermissionsFragment
import com.dgsd.solguard.onboarding.splash.OnboardingSplashFragment
import com.dgsd.solguard.onboarding.success.OnboardingSuccessFragment
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class OnboardingContainerFragment : Fragment(R.layout.frag_onboarding_container) {

  private val appCoordinator by activityViewModel<AppCoordinator>()
  private val onboardingCoordinator by viewModel<OnboardingCoordinator>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    childFragmentManager.addOnBackStackChangedListener {
      val destinationName = childFragmentManager.primaryNavigationFragment?.tag
      if (destinationName != null) {
        onboardingCoordinator.onBackStackChanged(Destination.valueOf(destinationName))
      }
    }

    onEach(onboardingCoordinator.destination) {
      onDestinationChanged(it)
    }

    onEach(onboardingCoordinator.continueInApp) {
      appCoordinator.navigateToHome()
    }

    lifecycleScope.launchWhenStarted {
      onboardingCoordinator.onCreate()
    }
  }

  private fun onDestinationChanged(destination: Destination) {
    childFragmentManager.navigate(
      containerId = R.id.fragment_container,
      fragment = getFragmentForDestination(destination),
      fragmentTag = destination.name,
      screenTransitionType = ScreenTransitionType.SLIDE_FROM_RIGHT
    )
  }

  private fun getFragmentForDestination(destination: Destination): Fragment {
    return when (destination) {
      Destination.AmountInput -> OnboardingAmountInputFragment.newInstance()
      Destination.AppLaunchLimit -> OnboardingAppLaunchLimitFragment.newInstance()
      Destination.AppSelection -> OnboardingAppSelectionFragment.newInstance()
      Destination.BiometricsSetup -> OnboardingBiometricsFragment.newInstance()
      Destination.GrantPermissions -> OnboardingGrantPermissionsFragment.newInstance()
      Destination.GrantNotificationPermissions ->
        OnboardingGrantNotificationPermissionsFragment.newInstance()
      Destination.GuardSetupSplash -> OnboardingGuardSetupSplashFragment.newInstance()
      Destination.MobileWalletAdapterCheck -> OnboardingMwaCheckFragment.newInstance()
      Destination.Splash -> OnboardingSplashFragment.newInstance()
      Destination.Success -> OnboardingSuccessFragment.newInstance()
    }
  }

  companion object {

    fun newInstance() = OnboardingContainerFragment()
  }
}