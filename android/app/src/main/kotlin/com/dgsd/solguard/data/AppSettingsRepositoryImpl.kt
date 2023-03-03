package com.dgsd.solguard.data

import android.content.SharedPreferences
import androidx.core.content.edit
import com.dgsd.ksol.core.model.PublicKey
import com.dgsd.solguard.common.flow.asResourceFlow
import com.dgsd.solguard.common.resource.model.Resource
import com.dgsd.solguard.guard.unlock.AppBlockUnlockConstants
import com.dgsd.solguard.model.AppSettings
import com.dgsd.solguard.model.PaymentToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

private const val PREF_KEY_DEFAULT_PAYMENT_TOKEN = "default_payment_token"
private const val PREF_KEY_INCREASE_PAYMENT_ON_EACH_UNBLOCK = "increase_payments_on_each_unblock"
private const val PREF_KEY_SHOW_WARNING_ON_LAST_LAUNCH = "show_warning_on_last_launch"
private const val PREF_KEY_SHOW_TIME_REMAINING_WHILE_UNLOCKED = "show_time_remaining_while_unlocked"
private const val PREF_KEY_SHOW_SUGGESTED_GUARD_NOTIFICATIONS = "show_suggested_guard_notifications"
private const val PREF_KEY_DONATION_TO_SOLGUARD_PERCENTAGE = "donation_to_solguard_percentage"

class AppSettingsRepositoryImpl(
  private val sharedPreferences: SharedPreferences,
) : AppSettingsRepository {

  private val appSettings by lazy { MutableStateFlow(createSettings()) }

  override suspend fun getAppSettingsSync(): AppSettings {
    return appSettings.value
  }

  override fun getAppSettings(): Flow<Resource<AppSettings>> {
    return appSettings.asResourceFlow()
  }

  override fun setDefaultPaymentToken(token: PaymentToken) {
    sharedPreferences.edit {
      putString(PREF_KEY_DEFAULT_PAYMENT_TOKEN, token.account.toBase58String())
    }
    updateAppSettings()
  }

  override fun setIncreasePaymentOnEachUnblock(enabled: Boolean) {
    sharedPreferences.edit {
      putBoolean(PREF_KEY_INCREASE_PAYMENT_ON_EACH_UNBLOCK, enabled)
    }
    updateAppSettings()
  }

  override fun setShowWarningOnLastLaunchNotification(enabled: Boolean) {
    sharedPreferences.edit {
      putBoolean(PREF_KEY_SHOW_WARNING_ON_LAST_LAUNCH, enabled)
    }
    updateAppSettings()
  }

  override fun setShowTimeRemainingWhileUnlockedNotification(enabled: Boolean) {
    sharedPreferences.edit {
      putBoolean(PREF_KEY_SHOW_TIME_REMAINING_WHILE_UNLOCKED, enabled)
    }
    updateAppSettings()
  }

  override fun setShowSuggestedGuardNotifications(enabled: Boolean) {
    sharedPreferences.edit {
      putBoolean(PREF_KEY_SHOW_SUGGESTED_GUARD_NOTIFICATIONS, enabled)
    }
    updateAppSettings()
  }

  override fun setDonationToSolGuardPercentage(percentage: Float) {
    sharedPreferences.edit {
      putFloat(PREF_KEY_DONATION_TO_SOLGUARD_PERCENTAGE, percentage)
    }
    updateAppSettings()
  }

  private fun updateAppSettings() {
    appSettings.value = createSettings()
  }

  private fun createSettings(): AppSettings {
    val paymentToken = sharedPreferences.getString(PREF_KEY_DEFAULT_PAYMENT_TOKEN, null)
      ?.let { PaymentToken.fromAccountAddress(PublicKey.fromBase58(it)) }
      ?: PaymentToken.NATIVE_SOL

    return AppSettings(
      defaultPaymentToken = paymentToken,
      increasePaymentOnEachUnblock = sharedPreferences.getBoolean(
        PREF_KEY_INCREASE_PAYMENT_ON_EACH_UNBLOCK,
        true
      ),
      showWarningOnLastLaunch = sharedPreferences.getBoolean(
        PREF_KEY_SHOW_WARNING_ON_LAST_LAUNCH,
        true
      ),
      showTimeRemainingWhileUnlocked = sharedPreferences.getBoolean(
        PREF_KEY_SHOW_TIME_REMAINING_WHILE_UNLOCKED,
        true
      ),
      showSuggestedGuardNotifications = sharedPreferences.getBoolean(
        PREF_KEY_SHOW_SUGGESTED_GUARD_NOTIFICATIONS,
        true
      ),
      donationToSolGuardPercentage = sharedPreferences.getFloat(
        PREF_KEY_DONATION_TO_SOLGUARD_PERCENTAGE,
        AppBlockUnlockConstants.DEFAULT_DONATE_TO_SOLGUARD,
      )
    )
  }
}