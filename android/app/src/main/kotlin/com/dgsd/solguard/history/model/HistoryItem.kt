package com.dgsd.solguard.history.model

import android.graphics.drawable.Drawable
import com.dgsd.solguard.common.ui.CornerRoundingMode
import java.time.LocalDate

data class HistoryItem(
  val date: LocalDate,
  val title: CharSequence,
  val summary: CharSequence,
  val appIcons: List<Drawable>,
  val cornerRoundingMode: CornerRoundingMode,
)
