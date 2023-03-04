package com.dgsd.solguard

import android.app.Application
import android.os.StrictMode
import com.dgsd.solguard.common.viewmodel.ViewModelActivityHolder
import com.dgsd.solguard.data.AppConfigRepository
import com.dgsd.solguard.di.AppModule
import com.dgsd.solguard.di.ViewModelModule
import com.dgsd.solguard.onboarding.di.OnboardingModule
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class SolGuardApplication : Application() {

  override fun onCreate() {
    super.onCreate()

    startKoin {
      if (BuildConfig.DEBUG) {
        androidLogger()
      }

      androidContext(this@SolGuardApplication)

      modules(
        AppModule.create(),
        ViewModelModule.create(),
        OnboardingModule.create(),
      )
    }

    if (BuildConfig.DEBUG) {
      StrictMode.enableDefaults()
    }

    registerActivityLifecycleCallbacks(ViewModelActivityHolder)

    GlobalScope.launch {
      runCatching {
        get<AppConfigRepository>().initialize()
      }
    }
  }
}