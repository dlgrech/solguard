package com.dgsd.solguard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dgsd.solguard.AppCoordinator.Destination
import com.dgsd.solguard.AppCoordinator.Destination.BottomSheetDestination
import com.dgsd.solguard.AppCoordinator.Destination.InlineDestination
import com.dgsd.solguard.common.flow.onEach
import com.dgsd.solguard.common.fragment.generateTag
import com.dgsd.solguard.common.fragment.model.ScreenTransitionType
import com.dgsd.solguard.common.fragment.navigate
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

  private val appCoordinator: AppCoordinator by viewModel()

  override fun onCreate(savedInstanceState: Bundle?) {
    // We explicitly don't restore our state here, so that we're always starting afresh
    super.onCreate(null)

    setContentView(R.layout.act_main)

    onEach(appCoordinator.destination) {
      onDestinationChanged(it)
    }

    lifecycleScope.launchWhenStarted {
      appCoordinator.onCreate()
    }
  }

  private fun onDestinationChanged(destination: Destination) {
    when (destination) {
      is InlineDestination -> navigateToInlineDestination(destination)
      is BottomSheetDestination -> navigateToBottomSheetDestination(destination)
    }
  }

  private fun navigateToBottomSheetDestination(destination: BottomSheetDestination) {
    val fragment = getFragmentForDestination(destination) as DialogFragment
    fragment.show(supportFragmentManager, fragment.generateTag())
  }

  private fun navigateToInlineDestination(destination: InlineDestination) {
    val fragment = getFragmentForDestination(destination)
    val shouldResetBackStack = shouldResetBackStackForDestination(destination)
    val transitionType = getScreenTransitionForDestination(destination)

    navigateToFragment(
      fragment = fragment,
      resetBackStack = shouldResetBackStack,
      screenTransitionType = transitionType,
    )
  }

  private fun navigateToFragment(
    fragment: Fragment,
    resetBackStack: Boolean,
    screenTransitionType: ScreenTransitionType,
  ) {
    supportFragmentManager.navigate(
      containerId = R.id.fragment_container,
      fragment = fragment,
      screenTransitionType = screenTransitionType,
      resetBackStack = resetBackStack,
    )
  }

  private fun shouldResetBackStackForDestination(destination: InlineDestination): Boolean {
    return when (destination) {
      else -> true
    }
  }

  private fun getScreenTransitionForDestination(destination: InlineDestination): ScreenTransitionType {
    return when (destination) {
      else -> ScreenTransitionType.DEFAULT
    }
  }

  private fun getFragmentForDestination(destination: Destination): Fragment {
    return when (destination) {
      else -> error("No destination for $destination")
    }
  }
}
