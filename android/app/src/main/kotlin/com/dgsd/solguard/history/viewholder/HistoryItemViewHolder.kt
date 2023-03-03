package com.dgsd.solguard.history.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.dgsd.solguard.R
import com.dgsd.solguard.common.ui.CornerRoundingMode
import com.dgsd.solguard.common.ui.circle
import com.dgsd.solguard.history.model.HistoryItem

/**
 * Marker interface for items shown on the history tab
 */
class HistoryItemViewHolder private constructor(
  itemView: View
) : RecyclerView.ViewHolder(itemView) {

  private val title = itemView.requireViewById<TextView>(R.id.title)
  private val summary = itemView.requireViewById<TextView>(R.id.summary)

  private val appIcon1 = itemView.requireViewById<ImageView>(R.id.app_icon_1)
  private val appIcon2 = itemView.requireViewById<ImageView>(R.id.app_icon_2)
  private val appIcon3 = itemView.requireViewById<ImageView>(R.id.app_icon_3)

  init {
    appIcon1.circle()
    appIcon2.circle()
    appIcon3.circle()
  }

  fun bind(item: HistoryItem) {
    title.text = item.title
    summary.text = item.summary

    appIcon1.isVisible = false
    appIcon2.isVisible = false
    appIcon3.isVisible = false

    when (item.appIcons.size) {
      0 -> Unit
      1 -> {
        appIcon1.setImageDrawable(item.appIcons[0])
        appIcon1.isVisible = true
      }
      2 -> {
        appIcon1.setImageDrawable(item.appIcons[0])
        appIcon1.isVisible = true

        appIcon2.setImageDrawable(item.appIcons[1])
        appIcon2.isVisible = true
      }
      else -> {
        appIcon1.setImageDrawable(item.appIcons[0])
        appIcon1.isVisible = true

        appIcon2.setImageDrawable(item.appIcons[1])
        appIcon2.isVisible = true

        appIcon3.setImageDrawable(item.appIcons[2])
        appIcon3.isVisible = true
      }
    }

    when (item.cornerRoundingMode) {
      CornerRoundingMode.ALL_CORNERS -> {
        itemView.setBackgroundResource(R.drawable.rounded_rect_color_section_color)
      }
      CornerRoundingMode.TOP_CORNERS -> {
        itemView.setBackgroundResource(R.drawable.rounded_rect_top_only_section_color)
      }
      CornerRoundingMode.BOTTOM_CORNERS -> {
        itemView.setBackgroundResource(R.drawable.rounded_rect_bottom_only_section_color)
      }
      else -> itemView.setBackgroundColor(
        ContextCompat.getColor(itemView.context, R.color.section_color)
      )
    }
  }

  companion object {

    fun create(parent: ViewGroup): HistoryItemViewHolder {
      val view = LayoutInflater.from(parent.context).inflate(
        R.layout.li_history_tab_item,
        parent,
        false
      )

      return HistoryItemViewHolder(view)
    }
  }
}