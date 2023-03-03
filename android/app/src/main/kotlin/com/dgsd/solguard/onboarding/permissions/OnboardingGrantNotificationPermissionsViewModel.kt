package com.dgsd.solguard.onboarding.permissions

import android.annotation.TargetApi
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.dgsd.solguard.common.flow.MutableEventFlow
import com.dgsd.solguard.common.flow.SimpleMutableEventFlow
import com.dgsd.solguard.common.flow.asEventFlow
import com.dgsd.solguard.common.flow.call
import com.dgsd.solguard.common.permission.PermissionsManager

class OnboardingGrantNotificationPermissionsViewModel(
  application: Application,
  private val permissionsManager: PermissionsManager,
) : AndroidViewModel(application) {

  private val _continueWithFlow = SimpleMutableEventFlow()
  val continueWithFlow = _continueWithFlow.asEventFlow()


  private val _requestSystemNotificationPermission = MutableEventFlow<String>()
  val requestSystemNotificationPermission = _requestSystemNotificationPermission.asEventFlow()

  fun onGrantPermissionsClicked() {
    if (permissionsManager.hasNotificationsPermission()) {
      _continueWithFlow.call()
    } else {
      _requestSystemNotificationPermission.tryEmit(android.Manifest.permission.POST_NOTIFICATIONS)
    }
  }

  fun onNotNowClicked() {
    _continueWithFlow.call()
  }

  @TargetApi(33)
  fun onNotificationPermissionResult() {
    _continueWithFlow.call()
  }
}