package com.dgsd.solguard.guard.create

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dgsd.solguard.model.InstalledAppInfo

class AppSelectionAdapter(
  private val onAppClicked: (appInfo: InstalledAppInfo) -> Unit,
) : RecyclerView.Adapter<AppSelectionViewHolder>() {

  private val items = mutableListOf<InstalledAppInfo>()

  init {
    setHasStableIds(true)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppSelectionViewHolder {
    return AppSelectionViewHolder.create(parent)
  }

  override fun onBindViewHolder(holder: AppSelectionViewHolder, position: Int) {
    val item = items[position]
    holder.bind(item)
    holder.itemView.setOnClickListener {
      onAppClicked.invoke(item)
    }
  }

  override fun getItemId(position: Int): Long {
    return items[position].packageName.hashCode().toLong()
  }

  override fun getItemCount(): Int {
    return items.size
  }

  fun updateItems(newItems: List<InstalledAppInfo>) {
    items.clear()
    items.addAll(newItems)

    notifyDataSetChanged()
  }
}