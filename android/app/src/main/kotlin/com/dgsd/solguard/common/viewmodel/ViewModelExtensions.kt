package com.dgsd.solguard.common.viewmodel

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

fun <T> ViewModel.onEach(flow: Flow<T>, action: suspend (T) -> Unit): Job {
  return flow.onEach(action).launchIn(viewModelScope)
}

fun AndroidViewModel.getString(
  @StringRes resId: Int,
  vararg args: Any
): String {
  return getApplication<Application>().getString(resId, *args)
}