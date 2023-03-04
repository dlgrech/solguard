package com.dgsd.solguard.model

import com.dgsd.ksol.core.model.PublicKey

data class Charity(
  val id: String,
  val name: String,
  val url: String,
  val imageUrl: String,
  val longDescription: String,
  val shortDescription: String,
  val publicKey: PublicKey,
)