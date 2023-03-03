package com.dgsd.solguard.guard.block.manager.strategies

import android.content.Context
import com.dgsd.solguard.guard.block.manager.AppBlockStrategy

/**
 * [AppBlockStrategy] to ensure we dont block ourselves!
 */
internal class NotSelfAppBlockStrategy(private val context: Context) : AppBlockStrategy {

  override suspend fun shouldBlock(packageName: String): AppBlockStrategy.Result {
    return if (packageName == context.packageName) {
      AppBlockStrategy.Result.PASS
    } else {
      AppBlockStrategy.Result.FALLTHROUGH
    }
  }
}