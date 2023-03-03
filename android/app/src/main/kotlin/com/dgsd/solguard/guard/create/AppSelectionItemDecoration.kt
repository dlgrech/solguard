package com.dgsd.solguard.guard.create

import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView

data class AppSelectionItemDecoration(
  @ColorInt private val dividerColor: Int,
  private val dividerStartPadding: Int,
) : RecyclerView.ItemDecoration() {

  private val dividerPaint = Paint().apply {
    isAntiAlias = true
    isDither = true
    color = dividerColor
    style = Paint.Style.FILL_AND_STROKE
    alpha = 80
  }

  override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
    super.onDraw(c, parent, state)
    val childCount = parent.childCount

    for (childIndex in 1 until childCount) {
      val currentChild = parent[childIndex]

      c.drawLine(
        currentChild.left.toFloat() + dividerStartPadding,
        currentChild.top.toFloat(),
        currentChild.right.toFloat(),
        currentChild.top.toFloat(),
        dividerPaint
      )
    }
  }
}