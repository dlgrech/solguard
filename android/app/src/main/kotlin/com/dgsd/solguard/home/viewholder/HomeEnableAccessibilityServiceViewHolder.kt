package com.dgsd.solguard.home.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dgsd.solguard.R
import com.dgsd.solguard.home.model.HomeItem

class HomeEnableAccessibilityServiceViewHolder private constructor(
  itemView: View
) : HomeItemViewHolder<HomeItem.EnableAccessibilityServiceItem>(itemView) {

  override fun bind(item: HomeItem.EnableAccessibilityServiceItem) = Unit

  companion object {
    fun create(parent: ViewGroup): HomeEnableAccessibilityServiceViewHolder {
      val view = LayoutInflater.from(parent.context).inflate(
        R.layout.li_home_tab_enable_accessibility_service,
        parent,
        false
      )

      return HomeEnableAccessibilityServiceViewHolder(view)
    }
  }
}