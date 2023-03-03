package com.dgsd.solguard.onboarding.appselection

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dgsd.solguard.model.InstalledAppInfo
import com.dgsd.solguard.onboarding.appselection.model.OnboardingAppSelectionItem

class OnboardingAppSelectionAdapter(
  private val onAppClicked: (appInfo: InstalledAppInfo) -> Unit,
) : RecyclerView.Adapter<OnboardingAppSelectionViewHolder>() {

  private val items = mutableListOf<OnboardingAppSelectionItem>()

  init {
    setHasStableIds(true)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingAppSelectionViewHolder {
    return OnboardingAppSelectionViewHolder.create(parent)
  }

  override fun onBindViewHolder(holder: OnboardingAppSelectionViewHolder, position: Int) {
    val item = items[position]
    holder.bind(item)
    holder.itemView.setOnClickListener {
      onAppClicked.invoke(item.appInfo)
    }
  }

  override fun getItemId(position: Int): Long {
    return items[position].appInfo.packageName.hashCode().toLong()
  }

  override fun getItemCount(): Int {
    return items.size
  }

  fun updateItems(newItems: List<OnboardingAppSelectionItem>) {
    items.clear()
    items.addAll(newItems)

    notifyDataSetChanged()
  }
}