package com.dgsd.solguard.guard.create

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dgsd.solguard.R
import com.dgsd.solguard.common.error.ErrorMessageFactory
import com.dgsd.solguard.common.error.UserFacingException
import com.dgsd.solguard.common.flow.MutableEventFlow
import com.dgsd.solguard.common.flow.asEventFlow
import com.dgsd.solguard.common.resource.ResourceFlowConsumer
import com.dgsd.solguard.common.ui.*
import com.dgsd.solguard.common.viewmodel.getContext
import com.dgsd.solguard.common.viewmodel.getString
import com.dgsd.solguard.data.AppLaunchRepository
import com.dgsd.solguard.data.InstalledAppInfoRepository
import com.dgsd.solguard.model.AppLaunchGuard
import com.dgsd.solguard.model.InstalledAppInfo
import com.dgsd.solguard.model.TokenAmount
import kotlinx.coroutines.flow.*

private const val DEFAULT_LAUNCH_COUNT = 5

class CreateGuardViewModel(
  application: Application,
  private val editGuardForPackageName: String?,
  private val errorMessageFactory: ErrorMessageFactory,
  private val appInfoRepository: InstalledAppInfoRepository,
  private val appLaunchRepository: AppLaunchRepository,
) : AndroidViewModel(application) {

  private val saveGuardResourceFlowConsumer = ResourceFlowConsumer<Unit>(viewModelScope)
  private val deleteGuardResourceFlowConsumer = ResourceFlowConsumer<Unit>(viewModelScope)
  private val existingAppInfoResourceConsumer =
    ResourceFlowConsumer<InstalledAppInfo>(viewModelScope)
  private val existingAppLaunchGuardResourceConsumer =
    ResourceFlowConsumer<AppLaunchGuard?>(viewModelScope)

  private val selectedApp = MutableStateFlow<InstalledAppInfo?>(null)
  private val launchCount = MutableStateFlow(DEFAULT_LAUNCH_COUNT)
  private val paymentAmount = MutableStateFlow<TokenAmount?>(null)

  private val selectedAppHasError = MutableStateFlow(false)
  private val amountInputHasError = MutableStateFlow(false)

  private val _showAppSelection = MutableEventFlow<InstalledAppInfo?>()
  val showAppSelection = _showAppSelection.asEventFlow()

  private val _showLaunchCountSelection = MutableEventFlow<Int>()
  val showLaunchCountSelection = _showLaunchCountSelection.asEventFlow()

  private val _showPaymentAmountSelection = MutableEventFlow<TokenAmount?>()
  val showPaymentAmountSelection = _showPaymentAmountSelection.asEventFlow()

  private val isEditingExistingGuard = MutableStateFlow(false)

  val inputText = combine(
    isEditingExistingGuard,
    selectedApp,
    launchCount,
    paymentAmount,
    selectedAppHasError,
    amountInputHasError,
  ) { results ->
    val isEditingExistingGuard = results[0] as Boolean
    val appInfo = results[1] as? InstalledAppInfo?
    val count = results[2] as Int
    val payment = results[3] as? TokenAmount
    val selectedAppError = results[4] as Boolean
    val amountInputError = results[5] as Boolean

    val context = getContext()

    val appNameText =
      appInfo?.displayName ?: getString(R.string.create_guard_input_text_default_app_text)
    val launchCountText =
      context.resources.getQuantityString(R.plurals.common_x_times, count, count)
    val paymentText = if (payment == null) {
      context.getString(R.string.create_guard_input_text_default_payment_text)
    } else {
      PaymentTokenFormatter.format(payment)
    }
    TextUtils.expandTemplate(
      getString(R.string.create_guard_input_text_template),
      if (isEditingExistingGuard) {
        appNameText.colored(appInfo.getDisplayColor())
      } else {
        appNameText.withSquiggle(appInfo, selectedAppError).onClick(::onAppInfoClicked)
      },
      launchCountText.withSquiggle(appInfo, false).onClick(::onLaunchCountClicked),
      paymentText.withSquiggle(appInfo, amountInputError).onClick(::onAmountInputClicked),
    )
  }

  val errorMessage = saveGuardResourceFlowConsumer.error.mapNotNull { error ->
    if (error == null) {
      null
    } else {
      errorMessageFactory.create(error)
    }
  }.asEventFlow(viewModelScope)

  val isDeleteGuardEnabled = isEditingExistingGuard.asStateFlow()

  val closeOnSuccess = combine(
    saveGuardResourceFlowConsumer.data,
    deleteGuardResourceFlowConsumer.data
  ) { saved, deleted ->
    if (saved != null) {
      true
    } else if (deleted != null) {
      false
    } else {
      null
    }
  }.filterNotNull().map { didSave ->
    if (didSave) {
      if (isEditingExistingGuard.value) {
        getString(R.string.edit_guard_success)
      } else {
        getString(R.string.create_new_guard_success)
      }
    } else {
      getString(R.string.deleted_guard_success)
    }
  }.asEventFlow(viewModelScope)

  init {
    existingAppInfoResourceConsumer.data
      .filterNotNull()
      .take(1)
      .onEach { appInfo ->
        selectedApp.value = appInfo
      }.launchIn(viewModelScope)

    existingAppLaunchGuardResourceConsumer.data
      .filterNotNull()
      .take(1)
      .onEach { existingGuard ->
        isEditingExistingGuard.value = true
        launchCount.value = existingGuard.numberOfLaunchesPerDay
        paymentAmount.value = existingGuard.amount
      }.launchIn(viewModelScope)

    if (editGuardForPackageName != null) {
      existingAppLaunchGuardResourceConsumer.collectFlow {
        appLaunchRepository.getAppLaunchGuard(editGuardForPackageName)
      }

      existingAppInfoResourceConsumer.collectFlow(
        appInfoRepository.getInstalledApp(editGuardForPackageName)
      )
    }
  }

  fun onSaveClicked() {
    saveGuardResourceFlowConsumer.collectFlow {
      val appInfo = selectedApp.value
      val amount = paymentAmount.value

      if (appInfo == null || amount == null || amount.amount <= 0) {
        if (appInfo == null) {
          selectedAppHasError.value = true
        } else {
          amountInputHasError.value = true
          throw UserFacingException(getString(R.string.create_new_guard_amount_input_missing_error))
        }
      } else {
        selectedAppHasError.value = false
        amountInputHasError.value = false

        appLaunchRepository.saveAppLaunchGuard(
          AppLaunchGuard(
            packageName = appInfo.packageName,
            numberOfLaunchesPerDay = launchCount.value,
            amount = amount,
            isEnabled = true
          )
        )
      }
    }
  }

  fun onDeleteClicked() {
    deleteGuardResourceFlowConsumer.collectFlow {
      appLaunchRepository.removeAppLaunchGuard(checkNotNull(editGuardForPackageName))
    }
  }

  fun onLaunchCountChanged(count: Int) {
    launchCount.value = count
  }

  fun onAppSelected(appInfo: InstalledAppInfo) {
    selectedAppHasError.value = false
    selectedApp.value = appInfo
  }

  fun onPaymentAmountChanged(amount: TokenAmount) {
    amountInputHasError.value = false
    paymentAmount.value = amount
  }

  private fun onAppInfoClicked() {
    _showAppSelection.tryEmit(selectedApp.value)
  }

  private fun onLaunchCountClicked() {
    _showLaunchCountSelection.tryEmit(launchCount.value)
  }

  private fun onAmountInputClicked() {
    _showPaymentAmountSelection.tryEmit(paymentAmount.value)
  }

  private fun CharSequence.withSquiggle(
    appInfo: InstalledAppInfo?,
    isError: Boolean
  ): CharSequence {
    val context = getContext()
    return if (isError) {
      squiggle(
        context,
        context.getColorAttr(R.attr.colorError),
        context.resources.getDimensionPixelSize(R.dimen.squiggly_text_wave_length_error)
      )
    } else {
      squiggle(
        context,
        appInfo.getDisplayColor(),
        context.resources.getDimensionPixelSize(R.dimen.squiggly_text_wave_length)
      )
    }
  }

  private fun InstalledAppInfo?.getDisplayColor(): Int {
    val fallback = getContext().getColorAttr(R.attr.colorPrimary)
    return this?.iconPalette?.getVibrantColor(fallback)
      ?: this?.iconPalette?.getDominantColor(fallback)
      ?: fallback
  }
}