package com.dgsd.solguard.guard.blackout

import android.app.Application
import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dgsd.solguard.R
import com.dgsd.solguard.common.amount.toSolAmountOrNull
import com.dgsd.solguard.common.error.ErrorMessageFactory
import com.dgsd.solguard.common.error.UserFacingException
import com.dgsd.solguard.common.flow.asEventFlow
import com.dgsd.solguard.common.flow.stateFlowOf
import com.dgsd.solguard.common.resource.ResourceFlowConsumer
import com.dgsd.solguard.common.viewmodel.getString
import com.dgsd.solguard.data.AppLaunchRepository
import com.dgsd.solguard.data.AppSettingsRepository
import com.dgsd.solguard.model.AppSettings
import com.dgsd.solguard.model.PaymentToken
import kotlinx.coroutines.flow.*

private const val HAS_PROMPTED_USER_TO_ADD_QS_TILE = "has_prompted_user_to_add_blackout_mode_tile"

class EnableBlackoutModeViewModel(
  application: Application,
  private val sharedPreferences: SharedPreferences,
  private val appLaunchRepository: AppLaunchRepository,
  private val appSettingsRepository: AppSettingsRepository,
  private val errorMessageFactory: ErrorMessageFactory,
) : AndroidViewModel(application) {

  private val enableResourceConsumer = ResourceFlowConsumer<Unit>(viewModelScope)
  private val appSettingsResourceFlowConsumer =
    ResourceFlowConsumer<AppSettings>(viewModelScope)

  val showAddQsTileUpsell =
    enableResourceConsumer.data
      .filterNotNull()
      .filter { Build.VERSION.SDK_INT >= 33 }
      .filter { !sharedPreferences.getBoolean(HAS_PROMPTED_USER_TO_ADD_QS_TILE, false) }
      .map { }
      .asEventFlow(viewModelScope)
      .onEach {
        sharedPreferences.edit { putBoolean(HAS_PROMPTED_USER_TO_ADD_QS_TILE, true) }
      }

  val closeOnSuccess =
    enableResourceConsumer.data
      .filterNotNull()
      .map { getString(R.string.enable_blackout_mode_success_message) }
      .asEventFlow(viewModelScope)

  val tokenOptions = stateFlowOf { PaymentToken.values() }

  private val _selectedPaymentToken = MutableStateFlow<PaymentToken?>(null)
  val selectedPaymentToken = _selectedPaymentToken.filterNotNull()

  private val _amountInput = MutableStateFlow("")
  val amountInput = _amountInput.asStateFlow()

  val amountInputHintText = selectedPaymentToken.map {
    getString(R.string.enable_blackout_mode_amount_input_description_template, it.displayName)
  }

  val errorMessage = enableResourceConsumer.error.mapNotNull { error ->
    if (error == null) {
      null
    } else {
      errorMessageFactory.create(error)
    }
  }.asEventFlow(viewModelScope)

  init {
    appSettingsResourceFlowConsumer.collectFlow(
      appSettingsRepository.getAppSettings()
    )

    appSettingsResourceFlowConsumer.data
      .filterNotNull()
      .take(1)
      .onEach { settings ->
        _selectedPaymentToken.value = settings.defaultPaymentToken
      }.launchIn(viewModelScope)
  }

  fun onAmountInputTextChanged(text: String) {
    _amountInput.value = text
  }

  fun onTokenOptionClicked(paymentToken: PaymentToken) {
    _selectedPaymentToken.value = paymentToken
  }

  fun onEnableConfirmed() {
    enableResourceConsumer.collectFlow {
      val paymentToken = _selectedPaymentToken.value
      val amount = amountInput.value.toSolAmountOrNull()

      if (paymentToken == null) {
        throw UserFacingException(errorMessageFactory.createDefault())
      }

      if (amount == null || amount <= 0f) {
        throw UserFacingException(getString(R.string.enable_blackout_mode_amount_input_missing_error))
      }

      appLaunchRepository.setBlackoutModeEnabled(amount, paymentToken)
    }
  }
}