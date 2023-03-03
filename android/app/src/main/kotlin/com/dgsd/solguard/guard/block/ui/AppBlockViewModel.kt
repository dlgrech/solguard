package com.dgsd.solguard.guard.block.ui

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dgsd.ksol.SolanaApi
import com.dgsd.ksol.core.LocalTransactions
import com.dgsd.ksol.core.model.LocalTransaction
import com.dgsd.ksol.core.model.PublicKey
import com.dgsd.ksol.core.utils.EncodingUtils
import com.dgsd.solguard.R
import com.dgsd.solguard.common.amount.toLamportsOrNull
import com.dgsd.solguard.common.clock.Clock
import com.dgsd.solguard.common.error.ErrorMessageFactory
import com.dgsd.solguard.common.flow.MutableEventFlow
import com.dgsd.solguard.common.flow.asEventFlow
import com.dgsd.solguard.common.flow.stateFlowOf
import com.dgsd.solguard.common.resource.ResourceFlowConsumer
import com.dgsd.solguard.common.ui.PaymentTokenFormatter
import com.dgsd.solguard.common.ui.bold
import com.dgsd.solguard.common.viewmodel.getString
import com.dgsd.solguard.data.AppConfigRepository
import com.dgsd.solguard.data.AppLaunchRepository
import com.dgsd.solguard.data.AppSettingsRepository
import com.dgsd.solguard.data.InstalledAppInfoRepository
import com.dgsd.solguard.guard.notifications.AppBlockNotificationManager
import com.dgsd.solguard.guard.unlock.AppBlockUnlockConstants
import com.dgsd.solguard.model.*
import com.solana.mobilewalletadapter.clientlib.AdapterOperations
import com.solana.mobilewalletadapter.clientlib.RpcCluster
import kotlinx.coroutines.flow.*

class AppBlockViewModel(
  application: Application,
  private val packageName: String,
  private val isForBlackoutDisableOnly: Boolean,
  private val isForInApp: Boolean,
  private val clock: Clock,
  private val solanaApi: SolanaApi,
  private val installedAppInfoRepository: InstalledAppInfoRepository,
  private val appLaunchRepository: AppLaunchRepository,
  private val appConfigRepository: AppConfigRepository,
  private val appSettingsRepository: AppSettingsRepository,
  private val errorMessageFactory: ErrorMessageFactory,
  private val appBlockNotificationManager: AppBlockNotificationManager,
) : AndroidViewModel(application) {

  private val installedAppResourceConsumer = ResourceFlowConsumer<InstalledAppInfo>(viewModelScope)
  private val historicalRecordResourceConsumer =
    ResourceFlowConsumer<AppLaunchEvent>(viewModelScope)
  private val appLaunchGuardResourceConsumer = ResourceFlowConsumer<AppLaunchGuard?>(viewModelScope)
  private val blackoutModeEventResourceConsumer =
    ResourceFlowConsumer<BlackoutModeEvent?>(viewModelScope)
  private val blackoutAppEventResourceConsumer =
    ResourceFlowConsumer<BlackoutAppEvent?>(viewModelScope)
  private val appConfigResourceConsumer = ResourceFlowConsumer<AppConfig>(viewModelScope)
  private val appSettingsResourceConsumer = ResourceFlowConsumer<AppSettings>(viewModelScope)

  private val isLoadingData = combine(
    installedAppResourceConsumer.isLoadingOrError,
    historicalRecordResourceConsumer.isLoadingOrError,
    appLaunchGuardResourceConsumer.isLoadingOrError,
    appConfigResourceConsumer.isLoadingOrError,
    appSettingsResourceConsumer.isLoadingOrError,
  ) { appInfoLoading, historyLoading, guardInfoLoading, appConfigLoading, settingsLoading ->
    appInfoLoading || historyLoading || guardInfoLoading || appConfigLoading || settingsLoading
  }

  val isBackgroundAnimationEnabled = stateFlowOf { !isForInApp }

  val screenTitle = stateFlowOf {
    if (isForBlackoutDisableOnly) {
      getString(R.string.app_block_disable_blackout_mode_screen_title)
    } else {
      getString(R.string.app_block_screen_title)
    }
  }

  private val isPaymentLoading = MutableStateFlow(false)
  val showLoadingIndicator = combine(
    isPaymentLoading,
    isLoadingData
  ) { paymentLoading, dataLoading ->
    paymentLoading || dataLoading
  }

  private val _showErrorMessage = MutableEventFlow<CharSequence>()
  val showErrorMessage = _showErrorMessage.asEventFlow()

  private val _showSuccessMessage = MutableEventFlow<CharSequence>()
  val showSuccess = _showSuccessMessage.asEventFlow()

  @Suppress("IfThenToElvis")
  private val amountToPay = combine(
    appLaunchGuardResourceConsumer.data,
    blackoutModeEventResourceConsumer.data,
    blackoutAppEventResourceConsumer.data,
    historicalRecordResourceConsumer.data.filterNotNull(),
    appSettingsResourceConsumer.data.filterNotNull()
  ) { guard, blackoutModeEvent, appBlackoutEvent, todaysRecord, appSettings ->
    if (isForBlackoutDisableOnly) {
      checkNotNull(appBlackoutEvent?.amount ?: blackoutModeEvent?.amount)
    } else if (appBlackoutEvent != null) {
      appBlackoutEvent.amount
    } else if (blackoutModeEvent != null) {
      blackoutModeEvent.amount
    } else {
      val tokenAmount = checkNotNull(guard).amount
      if (!appSettings.increasePaymentOnEachUnblock) {
        tokenAmount
      } else {
        val launches = todaysRecord.launchCount
        val launchesAllowed = guard.numberOfLaunchesPerDay
        val amountOver = launches - launchesAllowed
        val newAmount = if (amountOver <= 1) {
          tokenAmount.amount.toFloat()
        } else {
          tokenAmount.amount +
              (tokenAmount.amount * AppBlockUnlockConstants.UNLOCK_INCREASE_FACTOR * amountOver.toInt())
        }

        TokenAmount(newAmount.toLamportsOrNull() ?: tokenAmount.amount, tokenAmount.tokenAddress)
      }
    }
  }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

  val messageText = combine(
    installedAppResourceConsumer.data.filterNotNull(),
    historicalRecordResourceConsumer.data.filterNotNull(),
    appLaunchGuardResourceConsumer.data,
    blackoutModeEventResourceConsumer.data,
    blackoutAppEventResourceConsumer.data,
    amountToPay.filterNotNull(),
  ) { items ->
    val installedAppInfo = items[0] as InstalledAppInfo
    val todaysRecord = items[1] as AppLaunchEvent
    val appLaunchGuard = items[2] as? AppLaunchGuard?
    val blackoutModeEvent = items[3] as? BlackoutModeEvent?
    val appBlackoutEvent = items[4] as? BlackoutAppEvent?
    val amount = items[5] as TokenAmount

    if (appBlackoutEvent != null) {
      TextUtils.expandTemplate(
        getString(R.string.app_block_screen_instruction_app_blackout_template),
        installedAppInfo.displayName.bold(),
        PaymentTokenFormatter.format(amount).bold()
      )
    } else if (blackoutModeEvent != null) {
      if (isForBlackoutDisableOnly) {
        TextUtils.expandTemplate(
          getString(R.string.app_block_screen_instruction_blackout_only_template),
          PaymentTokenFormatter.format(amount).bold()
        )
      } else {
        TextUtils.expandTemplate(
          getString(R.string.app_block_screen_instruction_blackout_template),
          installedAppInfo.displayName.bold(),
          PaymentTokenFormatter.format(amount).bold()
        )
      }
    } else if (appLaunchGuard != null) {
      TextUtils.expandTemplate(
        getString(R.string.app_block_screen_instruction_template),
        installedAppInfo.displayName.bold(),
        appLaunchGuard.numberOfLaunchesPerDay.toString().bold(),
        todaysRecord.launchCount.toString().bold(),
        getString(R.string.app_block_screen_instruction_really).bold(),
        PaymentTokenFormatter.format(amount).bold()
      )
    } else {
      null
    }
  }

  init {
    combine(
      installedAppResourceConsumer.error,
      historicalRecordResourceConsumer.error,
      appLaunchGuardResourceConsumer.error,
      blackoutModeEventResourceConsumer.error,
      blackoutAppEventResourceConsumer.error,
      appConfigResourceConsumer.error,
      appSettingsResourceConsumer.error
    ) { values -> values.firstOrNull { it != null } }
      .filterNotNull()
      .map { errorMessageFactory.create(it) }
      .onEach { _showErrorMessage.tryEmit(it) }
      .launchIn(viewModelScope)

    appConfigResourceConsumer.collectFlow(
      appConfigRepository.getAppConfig()
    )
    appSettingsResourceConsumer.collectFlow(
      appSettingsRepository.getAppSettings()
    )
    installedAppResourceConsumer.collectFlow(
      installedAppInfoRepository.getInstalledApp(packageName)
    )
    historicalRecordResourceConsumer.collectFlow {
      appLaunchRepository.getAppLaunchHistoricalRecord(packageName, clock.now().toLocalDate())
    }
    appLaunchGuardResourceConsumer.collectFlow {
      appLaunchRepository.getAppLaunchGuard(packageName)
    }
    blackoutModeEventResourceConsumer.collectFlow {
      appLaunchRepository.getBlackoutModeEvent(clock.now().toLocalDate())?.takeIf { it.isEnabled }
    }
    blackoutAppEventResourceConsumer.collectFlow {
      appLaunchRepository.getAppBlackoutEvent(
        clock.now().toLocalDate(),
        packageName
      )?.takeIf { it.isEnabled }
    }
  }

  suspend fun onPayButtonClicked(mobileWalletAdapterOperations: AdapterOperations) {
    isPaymentLoading.value = true
    try {
      val latestBlockhash = solanaApi.getLatestBlockhash().blockhash
      val amountToPay = checkNotNull(amountToPay.value)
      val todaysRecord = checkNotNull(historicalRecordResourceConsumer.data.value)
      val appConfig = checkNotNull(appConfigResourceConsumer.data.value)
      val blackoutModeEvent = blackoutModeEventResourceConsumer.data.value
      val appBlackoutModeEvent = blackoutAppEventResourceConsumer.data.value

      if (isForBlackoutDisableOnly) {
        requireNotNull(blackoutModeEvent ?: appBlackoutModeEvent)
      }

      val authResult = mobileWalletAdapterOperations.authorize(
        appConfig.identityUri,
        appConfig.identityIconUri,
        getString(R.string.app_name),
        RpcCluster.Devnet
      )

      val transaction = createTransactionToSign(
        sender = PublicKey.fromByteArray(authResult.publicKey),
        receiver = appConfig.feeReceiver,
        amountToPay = amountToPay,
        blockhash = latestBlockhash
      )

      val signAndSendResult = mobileWalletAdapterOperations.signAndSendTransactions(
        arrayOf(LocalTransactions.serialize(transaction))
      )

      if (appBlackoutModeEvent != null) {
        appLaunchRepository.disableAppBlackout(
          clock.now().toLocalDate(),
          appBlackoutModeEvent.packageName
        )
        _showSuccessMessage.tryEmit(
          getString(R.string.app_block_disable_blackout_mode_success_message)
        )
      } else if (blackoutModeEvent != null) {
        appLaunchRepository.disableBlackoutMode()
        _showSuccessMessage.tryEmit(
          getString(R.string.app_block_disable_blackout_mode_success_message)
        )
      } else {
        val unlockTime = clock.now()
        appLaunchRepository.saveUnlockEvent(
          UnlockAppEvent(
            packageName = packageName,
            timestamp = unlockTime,
            transactionSignature = EncodingUtils.encodeBase58(signAndSendResult.signatures[0]),
            amount = amountToPay,
            launchNumber = todaysRecord.launchCount.toInt() + 1
          )
        )

        appBlockNotificationManager.showAppUnlockedNotification(packageName, unlockTime)

        _showSuccessMessage.tryEmit(
          TextUtils.expandTemplate(
            getString(R.string.app_block_unlock_success_message_template),
            installedAppResourceConsumer.data.value?.displayName ?: "",
            AppBlockUnlockConstants.MINUTES_TO_UNBLOCK_AFTER_UNLOCK.toString(),
          )
        )
      }
    } catch (ex: Exception) {
      _showErrorMessage.tryEmit(errorMessageFactory.create(ex))
    } finally {
      isPaymentLoading.value = false
    }
  }

  private fun createTransactionToSign(
    sender: PublicKey,
    receiver: PublicKey,
    amountToPay: TokenAmount,
    blockhash: String
  ): LocalTransaction {
    return LocalTransactions.createUnsignedTransferTransaction(
      sender,
      receiver,
      amountToPay.amount,
      TextUtils.expandTemplate(
        getString(R.string.app_block_unlock_memo_template),
        installedAppResourceConsumer.data.value?.displayName
          ?: getString(R.string.app_block_unlock_default_app)
      ).toString(),
      PublicKey.fromBase58(blockhash)
    )
  }
}