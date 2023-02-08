package com.dgsd.solguard.di

import com.dgsd.solguard.AppCoordinator
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

object ViewModelModule {

  fun create(): Module {
    return module {

      viewModel<AppCoordinator>()
    }
  }
}