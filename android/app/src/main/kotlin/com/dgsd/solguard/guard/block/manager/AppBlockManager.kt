package com.dgsd.solguard.guard.block.manager

interface AppBlockManager {

  suspend fun recordAppLaunch(appPackage: String)

  /**
   * @return [true] if we should block this app from launching, [false] otherwise
   */
  suspend fun shouldBlockApp(appPackage: String): Boolean

  /**
   * @return [true] if we should alert the user they're using this app a lot, and suggest a block
   */
  suspend fun shouldNotifyOfHeavyUse(appPackage: String): Boolean
}