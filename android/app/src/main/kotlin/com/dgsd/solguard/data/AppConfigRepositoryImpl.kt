package com.dgsd.solguard.data

import androidx.core.net.toUri
import com.dgsd.ksol.core.model.PublicKey
import com.dgsd.solguard.common.cache.CacheStrategy
import com.dgsd.solguard.common.flow.executeWithCache
import com.dgsd.solguard.common.flow.resourceFlowOf
import com.dgsd.solguard.common.resource.model.Resource
import com.dgsd.solguard.data.cache.AppConfigCache
import com.dgsd.solguard.model.AppConfig
import com.dgsd.solguard.model.Charity
import kotlinx.coroutines.flow.*

class AppConfigRepositoryImpl(
  private val solGuardApi: SolGuardApi,
  private val appConfigCache: AppConfigCache,
) : AppConfigRepository {

  override suspend fun initialize() {
    runCatching {
      getAppConfig().filterIsInstance<Resource.Success<*>>().take(1).collect()
    }
  }

  override fun getAppConfig(): Flow<Resource<AppConfig>> {
    return executeWithCache(
      cacheKey = "app_config",
      cacheStrategy = CacheStrategy.CACHE_IF_PRESENT,
      cache = appConfigCache,
      networkFlowProvider = { resourceFlowOf { fetchAppConfig() } }
    )
  }

  private suspend fun fetchAppConfig(): AppConfig {
    val response = checkNotNull(solGuardApi.getAppConfig().body())
    return AppConfig(
      identityUri = response.identityUrl.toUri(),
      identityIconUri = response.identityIconUriPath.toUri(),
      charities = response.charities.map { charityResponse ->
        Charity(
          id = charityResponse.id,
          name = charityResponse.name,
          url = charityResponse.url,
          imageUrl = charityResponse.imageUrl,
          longDescription = charityResponse.longDesc,
          shortDescription = charityResponse.shortDesc,
          publicKey = PublicKey.fromBase58(charityResponse.publicKey)
        )
      }
    )
  }
}