package com.dgsd.solguard.common.flow

import com.dgsd.solguard.common.resource.model.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

/**
 * Utility for presenting a static value as a [StateFlow]
 */
fun <T> stateFlowOf(valueProvider: () -> T): StateFlow<T> {
  return MutableStateFlow(valueProvider.invoke()).asStateFlow()
}

fun <T> Flow<T>.asNullable(): Flow<T?> {
  return this
}

fun <T, R> Flow<Resource<T>>.mapData(
  mapper: suspend (T) -> R
): Flow<Resource<R>> {
  return map { resource ->
    val newData = resource.dataOrNull()?.let { mapper.invoke(it) }
    when (resource) {
      is Resource.Error -> Resource.Error(resource.error, newData)
      is Resource.Loading -> Resource.Loading(newData)
      is Resource.Success -> Resource.Success(checkNotNull(newData))
    }
  }
}


/**
 * Converts a `suspend` method into a `Flow<Resource>`
 */
fun <T> resourceFlowOf(
  context: CoroutineContext = Dispatchers.IO,
  cachedValue: T? = null,
  action: suspend () -> T,
): Flow<Resource<T>> {
  return flow<Resource<T>> {
    emit(Resource.Loading(cachedValue))
    runCatching {
      action.invoke()
    }.onSuccess {
      emit(Resource.Success(it))
    }.onFailure {
      emit(Resource.Error(it))
    }
  }.flowOn(context)
}

fun <T> Flow<T>.asResourceFlow(): Flow<Resource<T>> {
  return map { Resource.Success(it) as Resource<T> }
    .onStart { emit(Resource.Loading(null)) }
    .catch { emit(Resource.Error(it)) }
}
