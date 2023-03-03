package com.dgsd.solguard.onboarding.applaunchlimit

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dgsd.solguard.R
import com.dgsd.solguard.common.flow.onEach
import com.dgsd.solguard.common.fragment.navigateBack
import com.dgsd.solguard.common.view.NumberPickerView
import com.dgsd.solguard.di.util.parentViewModel
import com.dgsd.solguard.onboarding.OnboardingCoordinator
import com.google.android.material.appbar.MaterialToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class OnboardingAppLaunchLimitFragment : Fragment(R.layout.frag_onboarding_app_launch_limit) {

  private val coordinator by parentViewModel<OnboardingCoordinator>()
  private val viewModel by viewModel<OnboardingAppLaunchLimitViewModel> {
    parametersOf(checkNotNull(coordinator.createGuardAppInfo))
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val message = view.requireViewById<TextView>(R.id.message)
    val limitPerDayPicker = view.requireViewById<NumberPickerView>(R.id.limit_per_day)

    view.requireViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
      navigateBack()
    }

    view.requireViewById<View>(R.id.next_button).setOnClickListener {
      viewModel.onNextClicked()
    }

    limitPerDayPicker.setOnPositionChangedListener {
      viewModel.onLimitPerDaySelected(it)
    }

    onEach(viewModel.screenMessageText) {
      message.text = it
    }

    onEach(viewModel.limitPerDay) { limit ->
      limitPerDayPicker.post {
        limitPerDayPicker.setSelected(limit)
      }
    }

    onEach(viewModel.continueWithFlow) {
      coordinator.navigateWithAppLaunchLimit(it)
    }
  }

  companion object {

    fun newInstance() = OnboardingAppLaunchLimitFragment()
  }
}