package com.dgsd.solguard.onboarding.permissions

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import com.dgsd.solguard.common.flow.MutableEventFlow
import com.dgsd.solguard.common.flow.SimpleMutableEventFlow
import com.dgsd.solguard.common.flow.asEventFlow
import com.dgsd.solguard.common.flow.call
import com.dgsd.solguard.common.intent.IntentFactory
import com.dgsd.solguard.common.viewmodel.getContext
import com.dgsd.solguard.guard.block.AppBlockAccessibilityService

class OnboardingGrantPermissionsViewModel(
  application: Application,
  private val intentFactory: IntentFactory,
): AndroidViewModel(application) {

  private val _continueWithFlow = SimpleMutableEventFlow()
  val continueWithFlow = _continueWithFlow.asEventFlow()


  private val _openSystemPermissionsScreen = MutableEventFlow<Intent>()
  val openSystemPermissionsScreen = _openSystemPermissionsScreen.asEventFlow()

  fun onGrantPermissionsClicked() {
    if (AppBlockAccessibilityService.isEnabled(getContext())) {
      _continueWithFlow.call()
    } else {
      _openSystemPermissionsScreen.tryEmit(intentFactory.createAccessibilitySettingsIntent())
    }
  }

  fun onOpenPermissionScreenResult() {
    if (AppBlockAccessibilityService.isEnabled(getContext())) {
      _continueWithFlow.call()
    }
  }
}