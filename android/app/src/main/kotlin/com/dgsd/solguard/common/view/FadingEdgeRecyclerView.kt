package com.dgsd.solguard.common.view

import android.content.Context
import android.util.AttributeSet

/**
 * RecyclerView class that supports both drawing fading edges with clipToPadding disabled
 */
class FadingEdgeRecyclerView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : ToggleableItemTouchListenerRecyclerView(context, attrs, defStyleAttr) {

  override fun isPaddingOffsetRequired(): Boolean {
    return !clipToPadding
  }

  override fun getLeftPaddingOffset(): Int {
    return if (clipToPadding) 0 else -paddingLeft
  }

  override fun getTopPaddingOffset(): Int {
    return if (clipToPadding) 0 else -paddingTop
  }

  override fun getRightPaddingOffset(): Int {
    return if (clipToPadding) 0 else paddingRight
  }

  override fun getBottomPaddingOffset(): Int {
    return if (clipToPadding) 0 else paddingBottom
  }
}