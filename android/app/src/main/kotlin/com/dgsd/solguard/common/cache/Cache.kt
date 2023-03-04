package com.dgsd.solguard.common.cache

import kotlinx.coroutines.flow.Flow

/**
 * Simple/generic cache interface, for saving and retrieving data by key
 */
interface Cache<K, V> {

  suspend fun clear()

  suspend fun set(key: K, value: V)

  fun get(key: K): Flow<CacheEntry<V>?>

}