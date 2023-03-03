package com.dgsd.solguard.model

import android.net.Uri
import com.dgsd.ksol.core.model.PublicKey

data class AppConfig(
  val feeReceiver: PublicKey,
  val identityUri: Uri,
  val identityIconUri: Uri
)