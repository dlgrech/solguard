package com.dgsd.solguard.onboarding.permissions

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.dgsd.solguard.R
import com.dgsd.solguard.common.flow.onEach
import com.dgsd.solguard.common.fragment.navigateBack
import com.dgsd.solguard.di.util.parentViewModel
import com.dgsd.solguard.onboarding.OnboardingCoordinator
import com.dgsd.solguard.onboarding.mwacheck.OnboardingMwaCheckViewModel
import com.google.android.material.appbar.MaterialToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class OnboardingGrantPermissionsFragment : Fragment(R.layout.frag_onboarding_grant_permissions) {

  private val coordinator by parentViewModel<OnboardingCoordinator>()
  private val viewModel by viewModel<OnboardingGrantPermissionsViewModel>()

  private val openPermissionScreenResultLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
  ) {
    viewModel.onOpenPermissionScreenResult()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    view.requireViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
      navigateBack()
    }

    view.requireViewById<View>(R.id.next_button).setOnClickListener {
      viewModel.onGrantPermissionsClicked()
    }

    onEach(viewModel.continueWithFlow) {
      coordinator.navigateToNextStep()
    }

    onEach(viewModel.openSystemPermissionsScreen) {
      openPermissionScreenResultLauncher.launch(it)
    }
  }

  companion object {

    fun newInstance() = OnboardingGrantPermissionsFragment()
  }
}