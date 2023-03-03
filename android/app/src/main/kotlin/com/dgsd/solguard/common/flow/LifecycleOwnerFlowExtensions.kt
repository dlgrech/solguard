package com.dgsd.solguard.common.flow

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

fun <T> LifecycleOwner.onEach(flow: Flow<T>, action: suspend (T) -> Unit) {
    val scope = if (this is Fragment) {
        viewLifecycleOwner.lifecycleScope
    } else {
        lifecycleScope
    }

    flow.onEach(action).launchIn(scope)
}