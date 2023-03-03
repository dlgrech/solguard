package com.dgsd.solguard.home.model

import android.graphics.drawable.Drawable
import androidx.palette.graphics.Palette
import com.dgsd.solguard.model.TokenAmount

sealed interface HomeItem {

  object EnableAccessibilityServiceItem : HomeItem

  data class AppLaunchGuardItem(
    val packageName: String,
    val numberOfLaunchesAllowedPerDay: Int,
    val amount: TokenAmount,
    val numberOfLaunchesToday: Int,
    val displayName: CharSequence,
    val isBlackedOut: Boolean,
    val isGuardEnabled: Boolean,
    val icon: Drawable,
    val palette: Palette,
  ) : HomeItem {

    val hasExceededDailyLimit = numberOfLaunchesToday > numberOfLaunchesAllowedPerDay
  }
}