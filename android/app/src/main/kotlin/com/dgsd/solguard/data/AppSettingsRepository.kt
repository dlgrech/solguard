package com.dgsd.solguard.data

import com.dgsd.solguard.common.resource.model.Resource
import com.dgsd.solguard.model.AppSettings
import com.dgsd.solguard.model.PaymentToken
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {

  suspend fun getAppSettingsSync(): AppSettings

  fun getAppSettings(): Flow<Resource<AppSettings>>

  fun setDefaultPaymentToken(token: PaymentToken)

  fun setIncreasePaymentOnEachUnblock(enabled: Boolean)

  fun setShowWarningOnLastLaunchNotification(enabled: Boolean)

  fun setShowTimeRemainingWhileUnlockedNotification(enabled: Boolean)

  fun setShowSuggestedGuardNotifications(enabled: Boolean)

  fun setDonationToSolGuardPercentage(percentage: Float)
}