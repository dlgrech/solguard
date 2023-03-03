package com.dgsd.solguard.guard.block.manager

/**
 * Defines a single check that we should undergo to determine if an app
 * launch should be blocked or not
 */
internal interface AppBlockStrategy {

  enum class Result {

    /**
     * Allow the app to launch
     */
    PASS,

    /**
     * No opinion on the app launch. Default behaviour should continue
     */
    FALLTHROUGH,

    /**
     * Explicitly block this app launch
     */
    BLOCK
  }

  /**
   * @return an [AppBlockResult] representing how this package should be considered
   */
  suspend fun shouldBlock(packageName: String): Result
}