package com.dgsd.solguard.data

import com.dgsd.ksol.core.model.Lamports
import com.dgsd.solguard.common.resource.model.Resource
import com.dgsd.solguard.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.LocalDateTime

interface AppLaunchRepository {

  fun getMostRecentAppLaunch(): StateFlow<String?>

  fun getAllAppLaunchGuards(): Flow<Resource<List<AppLaunchGuard>>>

  fun getHistoricalRecordsForDate(date: LocalDate): Flow<Resource<List<HistoricalRecord>>>

  fun getHistoricalRecords(): Flow<Resource<List<HistoricalRecord>>>

  fun getAppBlackoutEvents(date: LocalDate): Flow<Resource<List<BlackoutAppEvent>>>

  suspend fun getAppBlackoutEvent(date: LocalDate, packageName: String): BlackoutAppEvent?

  suspend fun setAppBlackoutMode(
    date: LocalDate,
    packageName: String,
    amount: TokenAmount
  ): BlackoutAppEvent

  suspend fun disableAppBlackout(date: LocalDate, packageName: String)

  suspend fun setBlackoutModeEnabled(penaltyAmount: Lamports, paymentToken: PaymentToken)

  suspend fun disableBlackoutMode()

  suspend fun getBlackoutModeEvent(date: LocalDate): BlackoutModeEvent?

  suspend fun saveAppLaunchGuard(guard: AppLaunchGuard)

  suspend fun removeAppLaunchGuard(packageName: String)

  suspend fun getAppLaunchGuard(packageName: String): AppLaunchGuard?

  suspend fun getAppLaunchHistoricalRecord(
    packageName: String,
    date: LocalDate
  ): AppLaunchEvent

  suspend fun recordAppLaunch(packageName: String)

  suspend fun saveUnlockEvent(event: UnlockAppEvent)

  suspend fun getUnlockEvent(
    packageName: String,
    fromTime: LocalDateTime,
    toTime: LocalDateTime,
  ): List<UnlockAppEvent>
}