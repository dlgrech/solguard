package com.dgsd.solguard.common.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.dgsd.solguard.common.ui.SquigglyTextSpan

/**
 * [TextView] which supports animating any [SquigglyTextSpan] instances
 */
class SquigglyTextView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

  init {
    movementMethod = LinkMovementMethod.getInstance()
    highlightColor = Color.TRANSPARENT
  }

  private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
    repeatMode = ValueAnimator.RESTART
    repeatCount = ValueAnimator.INFINITE
    interpolator = LinearInterpolator()
    duration = 1000
    addUpdateListener { animator ->
      val progress = animator.animatedValue as Float

      val squiggles = (text as? Spanned)?.getSpans(0, text.length, SquigglyTextSpan::class.java)
      if (!squiggles.isNullOrEmpty()) {
        squiggles.forEach { it.animationProgress = progress }
        invalidate()
      }
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    animator.start()
  }

  override fun onDetachedFromWindow() {
    animator.pause()
    super.onDetachedFromWindow()
  }
}