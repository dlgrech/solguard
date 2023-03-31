package com.dgsd.solguard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dgsd.solguard.AppCoordinator.Destination
import com.dgsd.solguard.AppCoordinator.Destination.BottomSheetDestination
import com.dgsd.solguard.AppCoordinator.Destination.InlineDestination
import com.dgsd.solguard.analytics.SolguardScreenAnalyticsManager
import com.dgsd.solguard.applock.BiometricChallengeFragment
import com.dgsd.solguard.common.flow.onEach
import com.dgsd.solguard.common.fragment.generateTag
import com.dgsd.solguard.common.fragment.model.ScreenTransitionType
import com.dgsd.solguard.common.fragment.navigate
import com.dgsd.solguard.guard.blackout.EnableBlackoutModeBottomSheetFragment
import com.dgsd.solguard.guard.block.ui.AppBlockActivity
import com.dgsd.solguard.guard.create.CreateGuardFragment
import com.dgsd.solguard.history.HistoryFragment
import com.dgsd.solguard.home.HomeFragment
import com.dgsd.solguard.howitworks.HowItWorksFragment
import com.dgsd.solguard.onboarding.OnboardingContainerFragment
import com.dgsd.solguard.settings.SettingsFragment
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(), AndroidScopeComponent {

  override val scope by activityScope()

  private val analytics by scope.inject<SolguardScreenAnalyticsManager>()
  private val appCoordinator: AppCoordinator by viewModel()

  override fun onCreate(savedInstanceState: Bundle?) {
    // We explicitly don't restore our state here, so that we're always starting afresh
    super.onCreate(null)

    setContentView(R.layout.act_main)

    analytics.monitorFragmentScreens()

    onEach(appCoordinator.destination) {
      onDestinationChanged(it)
    }

    lifecycleScope.launchWhenStarted {
      appCoordinator.onCreate()
    }
  }

  override fun onResume() {
    super.onResume()
    lifecycleScope.launchWhenResumed {
      appCoordinator.onResume(intent?.data)
      intent = intent?.apply { data = null }
    }
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    appCoordinator.onNewIntent(intent?.data)
    setIntent(intent?.apply { data = null })
  }

  private fun onDestinationChanged(destination: Destination) {
    if (destination is Destination.CompositeDestination) {
      destination.destinations.forEachIndexed { index, dest ->
        onDestinationChanged(dest, commitNow = index == 0)
      }
    } else {
      onDestinationChanged(destination, commitNow = false)
    }
  }

  private fun onDestinationChanged(destination: Destination, commitNow: Boolean) {
    when (destination) {
      is InlineDestination -> navigateToInlineDestination(destination, commitNow)
      is BottomSheetDestination -> navigateToBottomSheetDestination(destination)
      is Destination.CompositeDestination -> error("Trying to navigate to composite")
    }
  }

  private fun navigateToBottomSheetDestination(destination: BottomSheetDestination) {
    when (destination) {
      BottomSheetDestination.DisableBlackoutMode -> {
        startActivity(
          AppBlockActivity.getDisableBlackoutModeIntent(this)
        )
      }

      is BottomSheetDestination.DisableAppBlackoutMode -> {
        startActivity(
          AppBlockActivity.getDisableAppBlockIntent(this, destination.packageName)
        )
      }

      is BottomSheetDestination.AppBlock -> {
        startActivity(
          AppBlockActivity.getLaunchIntent(
            context = this,
            packageNameBeingBlocked = destination.packageName,
            homeOnDismiss = false
          )
        )
      }

      else -> {
        val fragment = getFragmentForDestination(destination) as DialogFragment
        fragment.show(supportFragmentManager, fragment.generateTag())
      }
    }
  }

  private fun navigateToInlineDestination(destination: InlineDestination, commitNow: Boolean) {
    val fragment = getFragmentForDestination(destination)
    val shouldResetBackStack = shouldResetBackStackForDestination(destination)
    val shouldAddOnTop = showAddOnTopForDestination(destination)
    val transitionType = getScreenTransitionForDestination(destination)

    navigateToFragment(
      fragment = fragment,
      resetBackStack = shouldResetBackStack,
      screenTransitionType = transitionType,
      commitNow = commitNow,
      addOnTop = shouldAddOnTop
    )
  }

  private fun navigateToFragment(
    fragment: Fragment,
    resetBackStack: Boolean,
    screenTransitionType: ScreenTransitionType,
    commitNow: Boolean,
    addOnTop: Boolean
  ) {
    supportFragmentManager.navigate(
      containerId = R.id.fragment_container,
      fragment = fragment,
      screenTransitionType = screenTransitionType,
      resetBackStack = resetBackStack,
      commitNow = commitNow,
      addOnTop = addOnTop
    )
  }

  private fun shouldResetBackStackForDestination(destination: InlineDestination): Boolean {
    return when (destination) {
      InlineDestination.Home -> true
      InlineDestination.Onboarding -> true
      InlineDestination.BiometricChallenge -> true
      else -> false
    }
  }

  private fun showAddOnTopForDestination(destination: InlineDestination): Boolean {
    return when (destination) {
      InlineDestination.CreateNew -> true
      is InlineDestination.EditGuard -> true
      is InlineDestination.HowItWorks -> true
      else -> false
    }
  }

  private fun getScreenTransitionForDestination(destination: InlineDestination): ScreenTransitionType {
    return when (destination) {
      InlineDestination.CreateNew -> ScreenTransitionType.SLIDE_FROM_BOTTOM
      is InlineDestination.EditGuard -> ScreenTransitionType.SLIDE_FROM_BOTTOM
      is InlineDestination.HowItWorks -> ScreenTransitionType.SLIDE_FROM_BOTTOM
      else -> ScreenTransitionType.DEFAULT
    }
  }

  private fun getFragmentForDestination(destination: Destination): Fragment {
    return when (destination) {
      InlineDestination.Home -> HomeFragment.newInstance()
      InlineDestination.History -> HistoryFragment.newInstance()
      InlineDestination.Settings -> SettingsFragment.newInstance()
      InlineDestination.Onboarding -> OnboardingContainerFragment.newInstance()
      InlineDestination.CreateNew -> CreateGuardFragment.newInstance()
      InlineDestination.HowItWorks -> HowItWorksFragment.newInstance()
      is InlineDestination.EditGuard -> CreateGuardFragment.newEditInstance(destination.packageName)
      is BottomSheetDestination.DisableAppBlackoutMode -> error("Should display activity")
      is BottomSheetDestination.AppBlock -> error("Should display activity")
      BottomSheetDestination.DisableBlackoutMode -> error("Should display activity")
      BottomSheetDestination.EnableBlackoutMode -> EnableBlackoutModeBottomSheetFragment.newInstance()
      else -> BiometricChallengeFragment.newInstance()
    }
  }
}
