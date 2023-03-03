package com.dgsd.solguard.settings

import android.Manifest
import android.annotation.TargetApi
import android.app.Application
import android.os.Build
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dgsd.solguard.R
import com.dgsd.solguard.common.flow.*
import com.dgsd.solguard.common.permission.PermissionsManager
import com.dgsd.solguard.common.resource.ResourceFlowConsumer
import com.dgsd.solguard.common.ui.PercentageFormatter
import com.dgsd.solguard.common.viewmodel.getString
import com.dgsd.solguard.data.AppSettingsRepository
import com.dgsd.solguard.guard.unlock.AppBlockUnlockConstants
import com.dgsd.solguard.model.AppSettings
import com.dgsd.solguard.model.PaymentToken
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

class SettingsViewModel(
  application: Application,
  private val appSettingsRepository: AppSettingsRepository,
  private val permissionsManager: PermissionsManager,
) : AndroidViewModel(application) {

  private val appSettingsResourceConsumer = ResourceFlowConsumer<AppSettings>(viewModelScope)

  val isLoading = appSettingsResourceConsumer.isLoading

  val defaultTokenText = appSettingsResourceConsumer.data
    .filterNotNull()
    .map { it.defaultPaymentToken.displayName }

  val increasingFeeText = appSettingsResourceConsumer.data
    .filterNotNull()
    .map { it.increasePaymentOnEachUnblock.toDisplayText() }

  val showWarningText = appSettingsResourceConsumer.data
    .filterNotNull()
    .map { it.showWarningOnLastLaunch && permissionsManager.hasNotificationsPermission() }
    .map { it.toDisplayText() }

  val showTimeRemainingText = appSettingsResourceConsumer.data
    .filterNotNull()
    .map { it.showTimeRemainingWhileUnlocked && permissionsManager.hasNotificationsPermission() }
    .map { it.toDisplayText() }

  val showSuggestedGuardsText = appSettingsResourceConsumer.data
    .filterNotNull()
    .map { it.showSuggestedGuardNotifications && permissionsManager.hasNotificationsPermission() }
    .map { it.toDisplayText() }

  val donationToSolguardText = appSettingsResourceConsumer.data
    .filterNotNull()
    .map {
      PercentageFormatter.format(it.donationToSolGuardPercentage)
    }

  val increasingFeeDescriptionText = stateFlowOf {
    TextUtils.expandTemplate(
      getString(R.string.settings_description_increase_payment_on_each_unlock_template),
      PercentageFormatter.format(AppBlockUnlockConstants.UNLOCK_INCREASE_FACTOR)
    )
  }

  private val _showNotificationPermissionWarning = MutableEventFlow<String>()
  val showNotificationPermissionWarning = _showNotificationPermissionWarning.asEventFlow()

  private val _showTokenOptions = MutableEventFlow<List<PaymentToken>>()
  val showTokenOptions = _showTokenOptions.asEventFlow()

  private val _showDonateToSolGuardOptions = MutableEventFlow<List<DonationOption>>()
  val showDonateToSolGuardOptions = _showDonateToSolGuardOptions.asEventFlow()

  data class DonationOption(
    val percentage: Float,
    val displayText: String
  )

  init {
    appSettingsResourceConsumer.collectFlow(
      appSettingsRepository.getAppSettings()
    )
  }

  fun onDefaultTokenRowClicked() {
    _showTokenOptions.tryEmit(PaymentToken.values().toList())
  }

  fun onTokenOptionClicked(token: PaymentToken) {
    appSettingsRepository.setDefaultPaymentToken(token)
  }

  fun onIncreasePaymentOnEachUnlockClicked() {
    withAppSettings { appSettings ->
      appSettingsRepository.setIncreasePaymentOnEachUnblock(
        !appSettings.increasePaymentOnEachUnblock
      )
    }
  }

  fun onShowWarningNotificationClicked() {
    if (!permissionsManager.hasNotificationsPermission()) {
      showNotificationPermissionWarning()
      return
    }

    withAppSettings { appSettings ->
      appSettingsRepository.setShowWarningOnLastLaunchNotification(
        !appSettings.showWarningOnLastLaunch
      )
    }
  }

  fun onShowTimeRemainingNotificationClicked() {
    if (!permissionsManager.hasNotificationsPermission()) {
      showNotificationPermissionWarning()
      return
    }

    withAppSettings { appSettings ->
      appSettingsRepository.setShowTimeRemainingWhileUnlockedNotification(
        !appSettings.showTimeRemainingWhileUnlocked
      )
    }
  }

  fun onShowSuggestionsNotificationClicked() {
    if (!permissionsManager.hasNotificationsPermission()) {
      showNotificationPermissionWarning()
      return
    }

    withAppSettings { appSettings ->
      appSettingsRepository.setShowSuggestedGuardNotifications(
        !appSettings.showSuggestedGuardNotifications
      )
    }
  }

  fun onDonationToSolguardClicked() {
    _showDonateToSolGuardOptions.tryEmit(
      donationPercentageOptions.map {
        DonationOption(it, PercentageFormatter.format(it))
      }
    )
  }

  fun onDonationOptionClicked(option: DonationOption) {
    appSettingsRepository.setDonationToSolGuardPercentage(option.percentage)
  }

  @TargetApi(Build.VERSION_CODES.TIRAMISU)
  private fun showNotificationPermissionWarning() {
    _showNotificationPermissionWarning.tryEmit(Manifest.permission.POST_NOTIFICATIONS)
  }

  private fun withAppSettings(action: (AppSettings) -> Unit) {
    val appSettings = checkNotNull(appSettingsResourceConsumer.data.value)
    action(appSettings)
  }

  private fun Boolean.toDisplayText(): String {
    return if (this) {
      getString(R.string.common_on)
    } else {
      getString(R.string.common_off)
    }
  }

  companion object {
    private val donationPercentageOptions =
      arrayOf(0f, 0.01f, 0.025f, 0.05f, 0.075f, 0.1f, 0.15f, 0.2f)
  }
}