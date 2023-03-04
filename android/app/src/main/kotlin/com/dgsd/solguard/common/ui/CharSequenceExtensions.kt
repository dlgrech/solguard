package com.dgsd.solguard.common.ui

import android.content.Context
import android.text.style.ClickableSpan
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.text.*
import com.dgsd.solguard.R

fun CharSequence.italic(): CharSequence {
  val original = this
  return buildSpannedString { italic { append(original) } }
}

fun CharSequence.bold(): CharSequence {
  val original = this
  return buildSpannedString { bold { append(original) } }
}

fun CharSequence.underlined(): CharSequence {
  val original = this
  return buildSpannedString { underline { append(original) } }
}

fun CharSequence.colored(@ColorInt color: Int): CharSequence {
  val original = this
  return buildSpannedString { color(color) { append(original) } }
}

fun CharSequence.squiggle(
  context: Context,
  @ColorInt color: Int = context.getColorAttr(R.attr.colorPrimary),
  @Px waveLength: Int = context.resources.getDimensionPixelSize(R.dimen.squiggly_text_wave_length),
): CharSequence {
  val original = this
  return buildSpannedString { inSpans(SquigglyTextSpan(context, color, waveLength)) { append(original) } }
}

fun CharSequence.onClick(action: () -> Unit): CharSequence {
  val original = this
  val span = object : ClickableSpan() {
    override fun onClick(widget: View) {
      action.invoke()
    }
  }
  return buildSpannedString { inSpans(span) { append(original) } }
}