package com.dgsd.solguard.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppConfigResponse(
  @SerialName("identity_uri") val identityUrl: String,
  @SerialName("identity_icon_uri") val identityIconUriPath: String,
  @SerialName("charities") val charities: List<CharityResponse>
) {

  @Serializable
  data class CharityResponse(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("url") val url: String,
    @SerialName("short_desc") val shortDesc: String,
    @SerialName("long_desc") val longDesc: String,
    @SerialName("public_key") val publicKey: String,
  )
}