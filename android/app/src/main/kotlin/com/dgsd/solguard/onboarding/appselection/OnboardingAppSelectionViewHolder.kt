package com.dgsd.solguard.onboarding.appselection

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dgsd.solguard.R
import com.dgsd.solguard.common.ui.CornerRoundingMode
import com.dgsd.solguard.common.ui.getColorAttr
import com.dgsd.solguard.common.ui.roundedCorners
import com.dgsd.solguard.onboarding.appselection.model.OnboardingAppSelectionItem

class OnboardingAppSelectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  private val icon = itemView.requireViewById<ImageView>(R.id.icon)
  private val appName = itemView.requireViewById<TextView>(R.id.app_name)

  init {
    itemView.clipToOutline = true
    icon.roundedCorners(
      itemView.resources.getDimensionPixelSize(R.dimen.app_icon_corner_radius).toFloat()
    )
  }

  fun bind(item: OnboardingAppSelectionItem) {
    appName.text = item.appInfo.displayName
    icon.setImageDrawable(item.appInfo.appIcon)

    when (item.cornerRounding) {
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

    fun create(parent: ViewGroup): OnboardingAppSelectionViewHolder {
      val view = LayoutInflater.from(parent.context).inflate(
        R.layout.li_onboarding_app_selection,
        parent,
        false
      )

      return OnboardingAppSelectionViewHolder(view)
    }
  }
}