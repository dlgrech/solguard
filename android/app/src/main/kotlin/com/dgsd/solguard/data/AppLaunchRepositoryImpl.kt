package com.dgsd.solguard.data

import android.content.Context
import app.cash.sqldelight.coroutines.asFlow
import com.dgsd.ksol.core.model.Lamports
import com.dgsd.ksol.core.model.PublicKey
import com.dgsd.solguard.*
import com.dgsd.solguard.common.clock.Clock
import com.dgsd.solguard.common.date.toEpochDay
import com.dgsd.solguard.common.date.toEpochMillis
import com.dgsd.solguard.common.date.toLocalDateTime
import com.dgsd.solguard.common.flow.asResourceFlow
import com.dgsd.solguard.common.resource.model.Resource
import com.dgsd.solguard.model.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

internal class AppLaunchRepositoryImpl(
  private val context: Context,
  private val clock: Clock,
  private val solGuardDatabase: SolGuardDatabase,
  private val coroutineDispatcher: CoroutineDispatcher,
) : AppLaunchRepository {

  private val mostRecentAppLaunchEvent = MutableStateFlow<String?>(null)

  override fun getAllAppLaunchGuards(): Flow<Resource<List<AppLaunchGuard>>> {
    return solGuardDatabase.appLaunchGuardRecordQueries
      .selectAll()
      .asFlow()
      .map { query ->
        query.executeAsList().map { record -> record.toAppLaunchGuard() }
      }.asResourceFlow()
  }

  override fun getHistoricalRecordsForDate(date: LocalDate): Flow<Resource<List<HistoricalRecord>>> {
    return solGuardDatabase.appLaunchEventRecordQueries
      .getAllOnDate(date.toEpochDay())
      .asFlow()
      .map { query ->
        query.executeAsList().map { record ->
          record.toHistoricalRecord()
        }
      }.asResourceFlow()
  }

  override fun getHistoricalRecords(): Flow<Resource<List<HistoricalRecord>>> {
    val appLaunchEvents = solGuardDatabase.appLaunchEventRecordQueries
      .selectAll()
      .asFlow()
      .map { query ->
        query.executeAsList().map { it.toHistoricalRecord() }
      }
    val blackoutEvents = solGuardDatabase.blackoutModeRecordQueries
      .selectAll()
      .asFlow()
      .map { query ->
        query.executeAsList().map { it.toBlackoutEvent() }
      }
    val appBlackoutEvents = solGuardDatabase.appBlackoutRecordQueries
      .selectAll()
      .asFlow()
      .map { query ->
        query.executeAsList().map { it.toBlackoutEvent() }
      }
    val unlockEvents = solGuardDatabase.unlockAppEventRecordQueries
      .selectAll()
      .asFlow()
      .map { query ->
        query.executeAsList().map { record ->
          record.toEvent()
        }
      }

    return combine(
      appLaunchEvents,
      unlockEvents,
      blackoutEvents,
      appBlackoutEvents,
    ) { appLaunches, unlocks, blackouts, appBlackouts ->
      (appLaunches + unlocks + blackouts + appBlackouts).sortedByDescending { historicalRecord ->
        when (historicalRecord) {
          is AppLaunchEvent -> historicalRecord.date
          is BlackoutModeEvent -> historicalRecord.date
          is BlackoutAppEvent -> historicalRecord.date
          is UnlockAppEvent -> historicalRecord.timestamp.toLocalDate()
        }
      }
    }.asResourceFlow()
  }

  override fun getAppBlackoutEvents(date: LocalDate): Flow<Resource<List<BlackoutAppEvent>>> {
    return solGuardDatabase.appBlackoutRecordQueries
      .getByEpochDay(date.toEpochDay())
      .asFlow()
      .map { query ->
        query.executeAsList().map { record ->
          record.toBlackoutEvent()
        }
      }
      .asResourceFlow()
  }

  override suspend fun getAppBlackoutEvent(
    date: LocalDate,
    packageName: String
  ): BlackoutAppEvent? {
    return solGuardDatabase.appBlackoutRecordQueries
      .getByPackageAndEpochDay(date.toEpochDay(), packageName)
      .executeAsOneOrNull()
      ?.toBlackoutEvent()
  }

  override suspend fun setAppBlackoutMode(
    date: LocalDate,
    packageName: String,
    amount: TokenAmount
  ): BlackoutAppEvent {
    val record = AppBlackoutRecord(
      packageName = packageName,
      epochDay = date.toEpochDay(),
      amount = amount.amount,
      paymentToken = amount.tokenAddress.toBase58String(),
      enabled = true,
    )
    solGuardDatabase.appBlackoutRecordQueries.insert(record)
    return record.toBlackoutEvent()
  }

  override suspend fun disableAppBlackout(date: LocalDate, packageName: String) {
    solGuardDatabase.appBlackoutRecordQueries
      .disableForPackageAndEpochDay(date.toEpochDay(), packageName)
  }

  override suspend fun saveAppLaunchGuard(
    guard: AppLaunchGuard
  ) = withContext(coroutineDispatcher) {
    solGuardDatabase.appLaunchGuardRecordQueries.insert(
      AppLaunchGuardRecord(
        guard.packageName,
        guard.numberOfLaunchesPerDay.toLong(),
        guard.amount.tokenAddress.toBase58String(),
        guard.amount.amount,
        guard.isEnabled
      )
    )
  }

  override suspend fun removeAppLaunchGuard(
    packageName: String
  ) = withContext(coroutineDispatcher) {
    solGuardDatabase.appLaunchGuardRecordQueries.remove(packageName)
  }

  override suspend fun getAppLaunchGuard(
    packageName: String
  ): AppLaunchGuard? = withContext(coroutineDispatcher) {
    solGuardDatabase.appLaunchGuardRecordQueries
      .getByPackageName(packageName)
      .executeAsOneOrNull()
      ?.toAppLaunchGuard()
  }

  override suspend fun getAppLaunchHistoricalRecord(
    packageName: String,
    date: LocalDate
  ): AppLaunchEvent = withContext(coroutineDispatcher) {
    val numberOfLaunches = solGuardDatabase.appLaunchEventRecordQueries
      .getCountByPackageNameAndDate(packageName = packageName, epochDay = date.toEpochDay())
      .executeAsOneOrNull()
    AppLaunchEvent(packageName, date, numberOfLaunches ?: 0)
  }

  override suspend fun recordAppLaunch(packageName: String) = withContext(coroutineDispatcher) {
    if (mostRecentAppLaunchEvent.value == packageName) {
      return@withContext
    }

    val today = clock.now().toLocalDate()
    val existing = getAppLaunchHistoricalRecord(packageName, today)
    val record = AppLaunchRecord(
      packageName,
      today.toEpochDay(),
      existing.launchCount + 1
    )

    solGuardDatabase.appLaunchEventRecordQueries.insert(record)

    if (context.packageName != packageName) {
      mostRecentAppLaunchEvent.value = packageName
    }
  }

  override suspend fun saveUnlockEvent(event: UnlockAppEvent) {
    solGuardDatabase.unlockAppEventRecordQueries.insert(event.toRecord())
  }

  override suspend fun getUnlockEvent(
    packageName: String,
    fromTime: LocalDateTime,
    toTime: LocalDateTime
  ): List<UnlockAppEvent> {
    return solGuardDatabase.unlockAppEventRecordQueries.getByPackageAndTimeRange(
      packageName,
      fromTime.toEpochMillis(),
      toTime.toEpochMillis()
    ).executeAsList().map { it.toEvent() }
  }

  override fun getMostRecentAppLaunch(): StateFlow<String?> {
    return mostRecentAppLaunchEvent
  }

  override suspend fun setBlackoutModeEnabled(penaltyAmount: Lamports, paymentToken: PaymentToken) {
    solGuardDatabase.blackoutModeRecordQueries.insert(
      BlackoutModeRecord(
        epochDay = clock.now().toEpochDay(),
        amount = penaltyAmount,
        paymentToken = paymentToken.account.toBase58String(),
        enabled = true
      )
    )
  }

  override suspend fun disableBlackoutMode() {
    solGuardDatabase.blackoutModeRecordQueries.disableForEpochDay(clock.now().toEpochDay())
  }

  override suspend fun getBlackoutModeEvent(date: LocalDate): BlackoutModeEvent? {
    return solGuardDatabase.blackoutModeRecordQueries
      .getByEpochDay(date.toEpochDay())
      .executeAsOneOrNull()
      ?.toBlackoutEvent()
  }

  private fun BlackoutModeRecord.toBlackoutEvent(): BlackoutModeEvent {
    return BlackoutModeEvent(
      date = LocalDate.ofEpochDay(epochDay),
      amount = TokenAmount(
        amount = amount,
        tokenAddress = PublicKey.fromBase58(paymentToken)
      ),
      isEnabled = enabled,
    )
  }

  private fun AppBlackoutRecord.toBlackoutEvent(): BlackoutAppEvent {
    return BlackoutAppEvent(
      packageName = packageName,
      date = LocalDate.ofEpochDay(epochDay),
      amount = TokenAmount(
        amount = amount,
        tokenAddress = PublicKey.fromBase58(paymentToken)
      ),
      isEnabled = enabled,
    )
  }

  private fun AppLaunchRecord.toHistoricalRecord(): AppLaunchEvent {
    return AppLaunchEvent(
      packageName,
      LocalDate.ofEpochDay(epochDay),
      launchCount
    )
  }

  private fun AppLaunchGuardRecord.toAppLaunchGuard(): AppLaunchGuard {
    return AppLaunchGuard(
      packageName = packageName,
      numberOfLaunchesPerDay = numberOfLaunchesPerDay.toInt(),
      amount = TokenAmount(
        tokenAddress = PublicKey.fromBase58(tokenAddress),
        amount = amount
      ),
      isEnabled = enabled
    )
  }

  private fun UnlockAppEvent.toRecord(): UnlockAppEventRecord {
    return UnlockAppEventRecord(
      packageName = this.packageName,
      timestamp = this.timestamp.toEpochMillis(),
      launchNumber = this.launchNumber.toLong(),
      transactionSignature = this.transactionSignature,
      amountPaid = this.amount.amount,
      tokenAddress = this.amount.tokenAddress.toBase58String()
    )
  }

  private fun UnlockAppEventRecord.toEvent(): UnlockAppEvent {
    return UnlockAppEvent(
      packageName = this.packageName,
      timestamp = this.timestamp.toLocalDateTime(),
      launchNumber = this.launchNumber.toInt(),
      transactionSignature = this.transactionSignature,
      amount = TokenAmount(
        amount = this.amountPaid,
        tokenAddress = PublicKey.fromBase58(this.tokenAddress)
      )
    )
  }
}