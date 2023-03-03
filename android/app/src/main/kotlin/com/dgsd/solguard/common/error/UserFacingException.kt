package com.dgsd.solguard.common.error

/**
 * [RuntimeException] subclass whos message can be presented to the user
 */
class UserFacingException(
  val userVisibleMessage: CharSequence
) : RuntimeException(userVisibleMessage.toString())