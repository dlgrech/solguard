package com.dgsd.solguard.data

import com.dgsd.solguard.common.resource.model.Resource
import kotlinx.coroutines.flow.Flow

interface SuggestedAppsRepository {

  fun getSuggestedAppsToGuard(): Flow<Resource<Set<String>>>

}