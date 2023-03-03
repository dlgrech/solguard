package com.dgsd.solguard.history

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dgsd.solguard.history.model.HistoryItem
import com.dgsd.solguard.history.viewholder.HistoryItemViewHolder

class HistoryAdapter : RecyclerView.Adapter<HistoryItemViewHolder>() {

  private val items = mutableListOf<HistoryItem>()

  init {
    setHasStableIds(true)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryItemViewHolder {
    return HistoryItemViewHolder.create(parent)
  }

  override fun onBindViewHolder(holder: HistoryItemViewHolder, position: Int) {
    holder.bind(items[position])
  }

  override fun getItemCount(): Int {
    return items.size
  }

  fun updateItems(newItems: List<HistoryItem>) {
    items.clear()
    items.addAll(newItems)

    notifyDataSetChanged()
  }
}