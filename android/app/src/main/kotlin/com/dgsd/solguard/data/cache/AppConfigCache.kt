package com.dgsd.solguard.data.cache

import com.dgsd.solguard.common.cache.InMemoryCache
import com.dgsd.solguard.model.AppConfig

class AppConfigCache : InMemoryCache<String, AppConfig>()