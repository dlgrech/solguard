package com.dgsd.solguard.data

import com.dgsd.ksol.core.model.Cluster
import com.dgsd.solguard.common.resource.model.Resource
import com.dgsd.solguard.model.AppSettings
import com.dgsd.solguard.model.PaymentToken
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {

  fun getAppSettingsSync(): AppSettings

  fun getAppSettings(): Flow<Resource<AppSettings>>

  fun setDefaultPaymentToken(token: PaymentToken)

  fun setIncreasePaymentOnEachUnblock(enabled: Boolean)

  fun setShowWarningOnLastLaunchNotification(enabled: Boolean)

  fun setShowTimeRemainingWhileUnlockedNotification(enabled: Boolean)

  fun setShowSuggestedGuardNotifications(enabled: Boolean)

  fun setDonationToSolGuardPercentage(percentage: Float)

  fun setCluster(cluster: Cluster)
}