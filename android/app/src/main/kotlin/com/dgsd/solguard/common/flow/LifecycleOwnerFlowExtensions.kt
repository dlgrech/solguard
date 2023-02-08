package com.dgsd.solguard.common.flow

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

fun <T> LifecycleOwner.onEach(flow: Flow<T>, action: suspend (T) -> Unit) {
    flow.onEach(action).launchIn(lifecycleScope)
}