package com.dgsd.solguard.common.viewmodel

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
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
  return getContext().getString(resId, *args)
}

fun AndroidViewModel.getDrawable(
  @DrawableRes resId: Int
): Drawable {
  return checkNotNull(ContextCompat.getDrawable(getContext(), resId))
}

fun AndroidViewModel.getContext(): Context {
  return ViewModelActivityHolder.getCurrentActivity() ?: getApplication()
}