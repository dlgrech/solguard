package com.dgsd.solguard.common.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Utility for presenting a static value as a [StateFlow]
 */
fun <T> stateFlowOf(valueProvider: () -> T): StateFlow<T> {
  return MutableStateFlow(valueProvider.invoke()).asStateFlow()
}

fun <T> Flow<T>.asNullable(): Flow<T?> {
  return this
}
