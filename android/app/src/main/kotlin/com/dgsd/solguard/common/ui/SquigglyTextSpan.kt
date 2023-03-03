package com.dgsd.solguard.common.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.text.TextPaint
import android.text.style.ReplacementSpan
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import com.dgsd.solguard.R
import kotlin.math.ceil
import kotlin.math.sin

private const val SEGMENTS_PER_WAVELENGTH = 10

class SquigglyTextSpan(
  context: Context,
  @ColorInt private val color: Int,
  private val waveLength: Int,
) : ReplacementSpan() {

  @FloatRange(from = 0.0, to = 1.0, fromInclusive = true)
  var animationProgress: Float = 0f

  private val linePaint = Paint().apply {
    isDither = true
    isAntiAlias = true
    style = Paint.Style.STROKE
    color = this@SquigglyTextSpan.color
    strokeCap = Paint.Cap.ROUND
    strokeWidth =
      context.resources.getDimensionPixelSize(R.dimen.squiggly_text_stroke_width).toFloat()
  }

  private val waveAmplitude = context.resources.getDimensionPixelSize(R.dimen.squiggly_text_wave_amplitude)

  private val path = Path()

  override fun getSize(
    paint: Paint,
    text: CharSequence?,
    start: Int,
    end: Int,
    fm: Paint.FontMetricsInt?
  ): Int {
    return paint.measureText(text, start, end).toInt()
  }

  override fun draw(
    canvas: Canvas,
    text: CharSequence,
    start: Int,
    end: Int,
    x: Float,
    top: Int,
    y: Int,
    bottom: Int,
    paint: Paint
  ) {
    paint.color = color
    canvas.drawText(text, start, end, x, y.toFloat(), paint)

    val textWidth = paint.measureText(text, start, end).toInt()
    val descent = (paint as TextPaint).descent()
    val lineY = y + (descent * 1.25f)

    val segmentWidth = waveLength / SEGMENTS_PER_WAVELENGTH.toFloat()
    val numOfPoints = ceil(textWidth / segmentWidth).toInt() + 1

    path.reset()

    var pointX = x
    for (point in 0..numOfPoints) {
      val proportionOfWavelength = (pointX - x) / waveLength
      val offset = 2 * Math.PI * animationProgress
      val radiansX = proportionOfWavelength * 2 * Math.PI
      val offsetY = (lineY + (sin(radiansX + offset) * waveAmplitude)).toFloat()

      when (point) {
        0 -> path.moveTo(pointX, offsetY)
        else -> path.lineTo(pointX, offsetY)
      }
      pointX = minOf(pointX + segmentWidth, x + textWidth)
    }

    canvas.drawPath(path, linePaint)
  }
}