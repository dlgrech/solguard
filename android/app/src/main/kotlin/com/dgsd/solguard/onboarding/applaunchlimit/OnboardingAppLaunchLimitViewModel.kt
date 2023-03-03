package com.dgsd.solguard.onboarding.applaunchlimit

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import com.dgsd.solguard.R
import com.dgsd.solguard.common.flow.MutableEventFlow
import com.dgsd.solguard.common.flow.asEventFlow
import com.dgsd.solguard.common.flow.stateFlowOf
import com.dgsd.solguard.common.ui.bold
import com.dgsd.solguard.common.viewmodel.getString
import com.dgsd.solguard.model.InstalledAppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val DEFAULT_APP_LAUNCH_LIMIT = 5

class OnboardingAppLaunchLimitViewModel(
  application: Application,
  private val appInfo: InstalledAppInfo,
) : AndroidViewModel(application) {

  private val _limitPerDay = MutableStateFlow(DEFAULT_APP_LAUNCH_LIMIT)
  val limitPerDay = _limitPerDay.asStateFlow()

  private val _continueWithFlow = MutableEventFlow<Int>()
  val continueWithFlow = _continueWithFlow.asEventFlow()

  val screenMessageText = stateFlowOf {
    TextUtils.expandTemplate(
      getString(R.string.onboarding_app_launch_limit_screen_message_template),
      appInfo.displayName.bold()
    )
  }

  fun onLimitPerDaySelected(limit: Int) {
    _limitPerDay.value = limit
  }

  fun onNextClicked() {
    _continueWithFlow.tryEmit(_limitPerDay.value)
  }
}