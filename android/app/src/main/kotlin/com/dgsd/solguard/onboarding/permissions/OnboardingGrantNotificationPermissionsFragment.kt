package com.dgsd.solguard.onboarding.permissions

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.dgsd.solguard.R
import com.dgsd.solguard.common.flow.onEach
import com.dgsd.solguard.common.fragment.navigateBack
import com.dgsd.solguard.common.intent.IntentFactory
import com.dgsd.solguard.di.util.parentViewModel
import com.dgsd.solguard.onboarding.OnboardingCoordinator
import com.google.android.material.appbar.MaterialToolbar
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class OnboardingGrantNotificationPermissionsFragment : Fragment(R.layout.frag_onboarding_grant_notification_permissions) {

  private val coordinator by parentViewModel<OnboardingCoordinator>()
  private val viewModel by viewModel<OnboardingGrantNotificationPermissionsViewModel>()
  private val intentFactory by inject<IntentFactory>()

  private val notificationPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) {
    viewModel.onNotificationPermissionResult()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    view.requireViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
      navigateBack()
    }

    view.requireViewById<View>(R.id.next_button).setOnClickListener {
      viewModel.onGrantPermissionsClicked()
    }

    view.requireViewById<View>(R.id.not_now).setOnClickListener {
      viewModel.onNotNowClicked()
    }

    onEach(viewModel.continueWithFlow) {
      coordinator.navigateToNextStep()
    }

    onEach(viewModel.requestSystemNotificationPermission) {
      if (shouldShowRequestPermissionRationale(it)) {
        startActivity(intentFactory.createAppNotificationSettingsIntent())
      } else {
        notificationPermissionLauncher.launch(it)
      }
    }
  }

  companion object {

    fun newInstance() = OnboardingGrantNotificationPermissionsFragment()
  }
}