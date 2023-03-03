package com.dgsd.solguard.data

import android.net.Uri
import com.dgsd.ksol.core.model.PublicKey
import com.dgsd.solguard.common.flow.resourceFlowOf
import com.dgsd.solguard.common.resource.model.Resource
import com.dgsd.solguard.model.AppConfig
import kotlinx.coroutines.flow.Flow

class AppConfigRepositoryImpl : AppConfigRepository {

  override fun getAppConfig(): Flow<Resource<AppConfig>> {
    // TODO: Fetch from remote service
    return resourceFlowOf {
      AppConfig(
        feeReceiver = PublicKey.fromBase58("GYg8cTa1qGShyAufHv8EDq3Kj9vPFzoKD9df4AYGNKbS"),
        identityUri = Uri.parse("solguard://app"),
        identityIconUri = Uri.EMPTY
      )
    }
  }
}