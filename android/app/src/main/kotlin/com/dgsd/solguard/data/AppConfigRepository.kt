package com.dgsd.solguard.data

import com.dgsd.solguard.common.resource.model.Resource
import com.dgsd.solguard.model.AppConfig
import kotlinx.coroutines.flow.Flow

interface AppConfigRepository {

  fun getAppConfig(): Flow<Resource<AppConfig>>
}