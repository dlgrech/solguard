package com.dgsd.solguard.guard.create

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dgsd.solguard.R
import com.dgsd.solguard.common.amount.toSolAmountOrNull
import com.dgsd.solguard.common.flow.stateFlowOf
import com.dgsd.solguard.common.resource.ResourceFlowConsumer
import com.dgsd.solguard.common.ui.PaymentTokenFormatter
import com.dgsd.solguard.common.viewmodel.getString
import com.dgsd.solguard.data.AppSettingsRepository
import com.dgsd.solguard.model.AppSettings
import com.dgsd.solguard.model.PaymentToken
import com.dgsd.solguard.model.TokenAmount
import kotlinx.coroutines.flow.*

class TokenAmountViewModel(
  application: Application,
  appSettingsRepository: AppSettingsRepository,
  tokenAmount: TokenAmount?
) : AndroidViewModel(application) {

  private val appSettingsResourceFlowConsumer =
    ResourceFlowConsumer<AppSettings>(viewModelScope)

  val tokenOptions = stateFlowOf { PaymentToken.values() }

  private val _selectedPaymentToken = MutableStateFlow<PaymentToken?>(null)
  val selectedPaymentToken = _selectedPaymentToken.filterNotNull()

  val amountInputHintText = selectedPaymentToken.map {
    getString(R.string.create_new_guard_amount_input_description_template, it.displayName)
  }

  private val _rawAmountInput = MutableStateFlow(
    tokenAmount?.let { PaymentTokenFormatter.formatAmount(it).toString() }.orEmpty()
  )
  val rawAmountInput = _rawAmountInput.asStateFlow()

  val amount =
    combine(
      selectedPaymentToken,
      rawAmountInput.mapNotNull { it.toSolAmountOrNull() }
    ) { token, amount ->
      TokenAmount(amount, token.account)
    }

  init {
    _selectedPaymentToken.value =
      tokenAmount?.let { PaymentToken.fromAccountAddress(it.tokenAddress) }
    appSettingsResourceFlowConsumer.data
      .filterNotNull()
      .take(1)
      .onEach { settings ->
        if (_selectedPaymentToken.value == null) {
          _selectedPaymentToken.value = settings.defaultPaymentToken
        }
      }.launchIn(viewModelScope)

    appSettingsResourceFlowConsumer.collectFlow(appSettingsRepository.getAppSettings())
  }

  fun onTokenOptionClicked(paymentToken: PaymentToken) {
    _selectedPaymentToken.value = paymentToken
  }

  fun onAmountInputTextChanged(text: String) {
    _rawAmountInput.value = text
  }
}