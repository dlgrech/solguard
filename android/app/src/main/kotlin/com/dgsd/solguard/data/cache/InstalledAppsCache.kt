package com.dgsd.solguard.data.cache

import com.dgsd.solguard.common.cache.InMemoryCache
import com.dgsd.solguard.model.AppConfig
import com.dgsd.solguard.model.InstalledAppInfo

class InstalledAppsCache : InMemoryCache<String, List<InstalledAppInfo>>()