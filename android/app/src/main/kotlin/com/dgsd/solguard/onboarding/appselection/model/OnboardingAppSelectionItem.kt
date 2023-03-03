package com.dgsd.solguard.onboarding.appselection.model

import com.dgsd.solguard.common.ui.CornerRoundingMode
import com.dgsd.solguard.model.InstalledAppInfo

data class OnboardingAppSelectionItem(
  val appInfo: InstalledAppInfo,
  val cornerRounding: CornerRoundingMode,
)