package com.dgsd.solguard.data

import com.dgsd.solguard.data.response.AppConfigResponse
import retrofit2.Response
import retrofit2.http.GET

interface SolGuardApi {

  @GET("config")
  suspend fun getAppConfig(): Response<AppConfigResponse>
}