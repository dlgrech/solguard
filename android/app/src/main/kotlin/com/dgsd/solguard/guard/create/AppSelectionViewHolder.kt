package com.dgsd.solguard.guard.create

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dgsd.solguard.R
import com.dgsd.solguard.common.ui.roundedCorners
import com.dgsd.solguard.model.InstalledAppInfo

class AppSelectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  private val icon = itemView.requireViewById<ImageView>(R.id.icon)
  private val appName = itemView.requireViewById<TextView>(R.id.app_name)

  init {
    icon.roundedCorners(
      itemView.resources.getDimensionPixelSize(R.dimen.app_icon_corner_radius).toFloat()
    )
  }

  fun bind(item: InstalledAppInfo) {
    appName.text = item.displayName
    icon.setImageDrawable(item.appIcon)
  }

  companion object {

    fun create(parent: ViewGroup): AppSelectionViewHolder {
      val view = LayoutInflater.from(parent.context).inflate(
        R.layout.li_app_selection,
        parent,
        false
      )

      return AppSelectionViewHolder(view)
    }
  }
}