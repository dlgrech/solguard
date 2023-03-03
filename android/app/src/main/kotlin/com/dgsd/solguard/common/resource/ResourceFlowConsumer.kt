package com.dgsd.solguard.common.resource

import com.dgsd.solguard.common.flow.resourceFlowOf
import com.dgsd.solguard.common.resource.model.Resource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Helper class for mapping a [Flow<Resource>] to something more useful at the UI level
 */
class ResourceFlowConsumer<T>(
  scope: CoroutineScope,
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

  private val _isLoading = MutableStateFlow(false)
  val isLoading = _isLoading.asStateFlow()

  private val _error = MutableStateFlow<Throwable?>(null)
  val error = _error.asStateFlow()

  private val _data = MutableStateFlow<T?>(null)
  val data = _data.asStateFlow()

  val isLoadingOrError = combine(isLoading, error) { isLoading, error ->
    isLoading || error != null
  }.distinctUntilChanged()

  private val _isLoadingWithNoData = MutableStateFlow(false)
  val isLoadingWithNoData = _isLoadingWithNoData.asStateFlow()

  private var existingJob: Job? = null

  private val coroutineScope = (scope + dispatcher)

  fun collectFlow(flow: Flow<Resource<T>>) {
    existingJob?.cancel()
    existingJob = coroutineScope.launch {
      flow.collectLatest { resource ->
        _isLoading.value = resource is Resource.Loading
        _isLoadingWithNoData.value = resource is Resource.Loading && resource.data == null
        _data.value = resource.dataOrNull()
        _error.value = (resource as? Resource.Error)?.error
      }
    }
  }

  fun collectFlow(action: suspend () -> T) {
    collectFlow(resourceFlowOf(context = dispatcher, action = action))
  }
}