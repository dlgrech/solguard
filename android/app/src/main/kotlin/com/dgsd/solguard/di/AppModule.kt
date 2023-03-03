package com.dgsd.solguard.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import coil.ImageLoader
import com.dgsd.ksol.SolanaApi
import com.dgsd.ksol.core.model.Cluster
import com.dgsd.solguard.SolGuardDatabase
import com.dgsd.solguard.applock.biometrics.AppLockBiometricManager
import com.dgsd.solguard.applock.biometrics.AppLockBiometricManagerImpl
import com.dgsd.solguard.common.clock.Clock
import com.dgsd.solguard.common.clock.SystemClock
import com.dgsd.solguard.common.error.ErrorMessageFactory
import com.dgsd.solguard.common.intent.IntentFactory
import com.dgsd.solguard.common.permission.PermissionsManager
import com.dgsd.solguard.data.*
import com.dgsd.solguard.data.AppLaunchRepositoryImpl
import com.dgsd.solguard.guard.block.manager.AppBlockManager
import com.dgsd.solguard.guard.block.manager.AppBlockManagerImpl
import com.dgsd.solguard.guard.block.manager.AppBlockStrategyFactory
import com.dgsd.solguard.guard.notifications.AppBlockNotificationManager
import com.dgsd.solguard.mwa.MobileWalletAdapterAvailabilityManager
import com.dgsd.solguard.mwa.MobileWalletAdapterAvailabilityManagerImpl
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import kotlinx.coroutines.Dispatchers
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

object AppModule {

  fun create(): Module {
    return module {

      single<OkHttpClient> {
        val context = get<Context>()
        OkHttpClient.Builder()
          .callTimeout(60, TimeUnit.SECONDS)
          .connectTimeout(60, TimeUnit.SECONDS)
          .readTimeout(60, TimeUnit.SECONDS)
          .cache(Cache(context.cacheDir, 10 * 1024 * 1024))
          .build()
      }

      single<ImageLoader> {
        ImageLoader.Builder(get())
          .crossfade(true)
          .okHttpClient(get<OkHttpClient>())
          .build()
      }

      single<SharedPreferences> {
        get<Application>().getSharedPreferences("solguard_prefs", Context.MODE_PRIVATE)
      }

      single<AppBlockManager> {
        AppBlockManagerImpl(
          strategyFactory = get(),
          appLaunchRepository = get(),
          clock = get(),
          sharedPreferences = get(),
        )
      }

      single<AppLaunchRepository> {
        AppLaunchRepositoryImpl(
          context = get(),
          clock = get(),
          solGuardDatabase = get(),
          coroutineDispatcher = Dispatchers.IO,
        )
      }

      single<InstalledAppInfoRepository> {
        InstalledAppInfoRepositoryImpl(context = get())
      }

      single<SuggestedAppsRepository> {
        SuggestedAppsRepositoryImpl()
      }

      single<AppConfigRepository> {
        AppConfigRepositoryImpl()
      }

      single<AppSettingsRepository> {
        AppSettingsRepositoryImpl(get())
      }

      single<MobileWalletAdapterAvailabilityManager> {
        MobileWalletAdapterAvailabilityManagerImpl(
          context = get(),
          installedAppInfoRepository = get()
        )
      }

      single<Clock> {
        SystemClock
      }

      single<SolGuardDatabase> {
        val context = get<Context>()
        val driver = AndroidSqliteDriver(SolGuardDatabase.Schema, context, "solguard.db")
        SolGuardDatabase(driver)
      }

      single<MobileWalletAdapter> {
        MobileWalletAdapter()
      }

      single<SolanaApi> {
        SolanaApi(Cluster.DEVNET)
      }

      single<AppLockBiometricManager> {
        AppLockBiometricManagerImpl(get())
      }

      singleOf(::AppBlockNotificationManager)
      singleOf(::ErrorMessageFactory)
      singleOf(::AppBlockStrategyFactory)
      singleOf(::IntentFactory)
      singleOf(::PermissionsManager)
    }
  }
}