package com.dgsd.solguard

import android.app.Application
import android.os.StrictMode
import com.dgsd.solguard.di.AppModule
import com.dgsd.solguard.di.ViewModelModule
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
      )
    }

    if (BuildConfig.DEBUG) {
      StrictMode.enableDefaults()
    }
  }
}