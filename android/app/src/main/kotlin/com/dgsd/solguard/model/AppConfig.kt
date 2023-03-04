package com.dgsd.solguard.model

import android.net.Uri

data class AppConfig(
  val identityUri: Uri,
  val identityIconUri: Uri,
  val charities: List<Charity>
)