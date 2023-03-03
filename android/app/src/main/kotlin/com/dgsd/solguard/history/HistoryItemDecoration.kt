package com.dgsd.solguard.history

import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView

data class HistoryItemDecoration(
  @ColorInt private val dividerColor: Int,
) : RecyclerView.ItemDecoration() {

  private val dividerPaint = Paint().apply {
    isAntiAlias = true
    isDither = true
    color = dividerColor
    style = Paint.Style.FILL_AND_STROKE
    alpha = 80
  }

  override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
    for (childIndex in 1 until parent.childCount) {
      val currentChild = parent[childIndex]
      c.drawLine(
        currentChild.left.toFloat(),
        currentChild.top.toFloat(),
        currentChild.right.toFloat(),
        currentChild.top.toFloat(),
        dividerPaint
      )
    }
  }
}