package com.dgsd.solguard.common.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView class that supports both drawing fading edges with clipToPadding disabled
 */
open class ToggleableItemTouchListenerRecyclerView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

  interface TouchInterceptAwareViewHolder {
    fun allowTouchIntercept(x: Float, y: Float): Boolean
  }

  private val listenerToWrapped = mutableMapOf<OnItemTouchListener, WrappedOnItemTouchListener>()

  override fun addOnItemTouchListener(listener: OnItemTouchListener) {
    val wrapped = WrappedOnItemTouchListener(listener)
    listenerToWrapped[listener] = wrapped
    super.addOnItemTouchListener(wrapped)
  }

  override fun removeOnItemTouchListener(listener: OnItemTouchListener) {
    val wrapped = listenerToWrapped[listener]
    if (wrapped != null) {
      super.removeOnItemTouchListener(wrapped)
    }
  }

  private class WrappedOnItemTouchListener(
    val wrapped: OnItemTouchListener
  ) : OnItemTouchListener {

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
      val childView = rv.findChildViewUnder(e.x, e.y)
      val viewHolder = childView?.let { rv.getChildViewHolder(it) }

      return if (viewHolder is TouchInterceptAwareViewHolder) {
        viewHolder.allowTouchIntercept(e.x, e.y) && wrapped.onInterceptTouchEvent(rv, e)
      } else {
        wrapped.onInterceptTouchEvent(rv, e)
      }
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
      wrapped.onTouchEvent(rv, e)
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
      wrapped.onRequestDisallowInterceptTouchEvent(disallowIntercept)
    }
  }
}