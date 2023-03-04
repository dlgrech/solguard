package com.dgsd.solguard.common.flow

import com.dgsd.solguard.common.cache.Cache
import com.dgsd.solguard.common.cache.CacheStrategy
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
  action: suspend () -> T,
): Flow<Resource<T>> {
  return flow<Resource<T>> {
    emit(Resource.Loading())
    runCatching {
      action.invoke()
    }.onSuccess {
      emit(Resource.Success(it))
    }.onFailure {
      emit(Resource.Error(it))
    }
  }.flowOn(context)
}

fun <T> Flow<Resource<T>>.onEachSuccess(
  action: suspend (T) -> Unit
): Flow<Resource<T>> {
  return onEach { resource ->
    if (resource is Resource.Success) {
      action.invoke(resource.data)
    }
  }
}

fun <T> Flow<T>.asResourceFlow(): Flow<Resource<T>> {
  return map { Resource.Success(it) as Resource<T> }
    .onStart { emit(Resource.Loading(null)) }
    .catch { emit(Resource.Error(it)) }
}

fun <K, V> Cache<K, V>.asResourceFlow(key: K): Flow<Resource<V>> {
  return get(key).flatMapLatest { cacheEntry ->
    if (cacheEntry == null) {
      flowOf(Resource.Error(IllegalStateException("Value missing from cache")))
    } else {
      flowOf(Resource.Success(cacheEntry.cacheData))
    }
  }.onStart { emit(Resource.Loading(null)) }
}

fun <K, V> Flow<Resource<V>>.withCache(
  key: K,
  cache: Cache<K, V>
): Flow<Resource<V>> {
  return onEachSuccess { cache.set(key, it) }
}

fun <K, V> executeWithCache(
  cacheKey: K,
  cacheStrategy: CacheStrategy,
  cache: Cache<K, V>,
  networkFlowProvider: () -> Flow<Resource<V>>
): Flow<Resource<V>> {
  return when (cacheStrategy) {
    CacheStrategy.CACHE_ONLY -> {
      cache.asResourceFlow(cacheKey)
    }

    CacheStrategy.NETWORK_ONLY -> {
      networkFlowProvider.invoke().withCache(cacheKey, cache)
    }

    CacheStrategy.CACHE_IF_PRESENT -> {
      cache.get(cacheKey).flatMapLatest { cacheEntry ->
        if (cacheEntry != null) {
          flowOf(Resource.Success(cacheEntry.cacheData))
        } else {
          executeWithCache(
            cacheKey,
            CacheStrategy.NETWORK_ONLY,
            cache,
            networkFlowProvider
          )
        }
      }.onStart { emit(Resource.Loading(null)) }
    }

    CacheStrategy.CACHE_AND_NETWORK -> {
      val networkFlow = executeWithCache(
        cacheKey,
        CacheStrategy.NETWORK_ONLY,
        cache,
        networkFlowProvider
      )

      val cacheFlow = cache
        .get(cacheKey)
        .mapNotNull { cacheEntry ->
          if (cacheEntry == null) {
            null
          } else {
            Resource.Success(cacheEntry.cacheData) as Resource<V>
          }
        }.onStart { emit(Resource.Loading(null)) }

      return combine(cacheFlow, networkFlow) { cacheResource, networkResource ->
        when (networkResource) {
          is Resource.Error<*> -> {
            if (networkResource.data != null) {
              networkResource
            } else {
              Resource.Error(networkResource.error, cacheResource.dataOrNull())
            }
          }
          is Resource.Success<*> -> {
            cacheResource
          }
          is Resource.Loading<*> -> {
            // If we have no data with the Loading state, use our cache
            if (networkResource.data != null) {
              networkResource
            } else {
              cacheResource
            }
          }
        }
      }
    }
  }
}