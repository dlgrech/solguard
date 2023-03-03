package com.dgsd.solguard.onboarding.amountinput

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dgsd.solguard.R
import com.dgsd.solguard.common.amount.toSolAmountOrNull
import com.dgsd.solguard.common.flow.MutableEventFlow
import com.dgsd.solguard.common.flow.asEventFlow
import com.dgsd.solguard.common.flow.stateFlowOf
import com.dgsd.solguard.common.resource.ResourceFlowConsumer
import com.dgsd.solguard.common.ui.bold
import com.dgsd.solguard.common.viewmodel.getContext
import com.dgsd.solguard.common.viewmodel.getString
import com.dgsd.solguard.data.AppSettingsRepository
import com.dgsd.solguard.model.AppSettings
import com.dgsd.solguard.model.InstalledAppInfo
import com.dgsd.solguard.model.PaymentToken
import com.dgsd.solguard.model.TokenAmount
import kotlinx.coroutines.flow.*

class OnboardingAmountInputViewModel(
  application: Application,
  private val appInfo: InstalledAppInfo,
  private val appLaunchLimit: Int,
  appSettingsRepository: AppSettingsRepository,
) : AndroidViewModel(application) {

  private val appSettingsResourceFlowConsumer =
    ResourceFlowConsumer<AppSettings>(viewModelScope)

  val tokenOptions = stateFlowOf { PaymentToken.values() }

  private val _selectedPaymentToken = MutableStateFlow<PaymentToken?>(null)
  val selectedPaymentToken = _selectedPaymentToken.filterNotNull()

  val amountInputHintText = selectedPaymentToken.map {
    getString(R.string.onboarding_amount_input_description_template, it.displayName)
  }

  private val _amountInput = MutableStateFlow("")
  val amountInput = _amountInput.asStateFlow()

  private val _continueWithFlow = MutableEventFlow<TokenAmount>()
  val continueWithFlow = _continueWithFlow.asEventFlow()

  private val _errorMessage = MutableEventFlow<CharSequence>()
  val errorMessage = _errorMessage.asEventFlow()

  val screenMessageText = stateFlowOf {
    if (appLaunchLimit == 0) {
      TextUtils.expandTemplate(
        getString(R.string.onboarding_amount_input_screen_message_zero_limit),
        appInfo.displayName.bold()
      )
    } else {
      TextUtils.expandTemplate(
        getContext().resources.getQuantityString(
          R.plurals.onboarding_amount_input_screen_message_template,
          appLaunchLimit,
        ),
        appInfo.displayName.bold(),
        appLaunchLimit.toString().bold()
      )
    }
  }

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

  fun onNextClicked() {
    val token = _selectedPaymentToken.value
    val amount = amountInput.value.toSolAmountOrNull()

    if (token == null || amount == null) {
      _errorMessage.tryEmit(getString(R.string.onboarding_amount_input_missing_error))
      return
    }

    _continueWithFlow.tryEmit(TokenAmount(amount, token.account))
  }
}