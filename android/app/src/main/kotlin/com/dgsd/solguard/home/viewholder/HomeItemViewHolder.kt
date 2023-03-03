package com.dgsd.solguard.home.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.dgsd.solguard.home.model.HomeItem

/**
 * Marker interface for items shown on the home tab
 */
sealed class HomeItemViewHolder<T : HomeItem>(
  itemView: View
) : RecyclerView.ViewHolder(itemView) {

  abstract fun bind(item: T)
}