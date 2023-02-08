package com.dgsd.solguard.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import coil.ImageLoader
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.koin.core.module.Module
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
    }
  }
}