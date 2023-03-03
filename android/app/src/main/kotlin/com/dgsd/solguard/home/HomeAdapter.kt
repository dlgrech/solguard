package com.dgsd.solguard.home

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dgsd.solguard.home.model.HomeItem
import com.dgsd.solguard.home.viewholder.HomeAppLaunchGuardViewHolder
import com.dgsd.solguard.home.viewholder.HomeEnableAccessibilityServiceViewHolder
import com.dgsd.solguard.home.viewholder.HomeItemViewHolder

private const val VIEW_TYPE_EXISTING_GUARD = 0
private const val VIEW_TYPE_ENABLE_ACCESSIBILITY_SERVICE = 1

class HomeTabAdapter(
  private val onEnableAccessibilityServiceClicked: () -> Unit,
  private val onExistingAppGuardClicked: (item: HomeItem.AppLaunchGuardItem) -> Unit,
  private val onExistingAppGuardDeleteClicked: (packageName: String) -> Unit,
  private val onExistingAppGuardBlackoutClicked: (item: HomeItem.AppLaunchGuardItem) -> Unit,
  private val onExistingAppGuardEnableDisableClicked: (packageName: String) -> Unit,
) : RecyclerView.Adapter<HomeItemViewHolder<*>>() {

  private val items = mutableListOf<HomeItem>()

  init {
    setHasStableIds(true)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeItemViewHolder<*> {
    return when (viewType) {
      VIEW_TYPE_EXISTING_GUARD -> HomeAppLaunchGuardViewHolder.create(
        parent,
        onExistingAppGuardClicked,
        onExistingAppGuardDeleteClicked,
        onExistingAppGuardBlackoutClicked,
        onExistingAppGuardEnableDisableClicked,
      )
      VIEW_TYPE_ENABLE_ACCESSIBILITY_SERVICE ->
        HomeEnableAccessibilityServiceViewHolder.create(parent)
      else -> error("Unknown view type: $viewType")
    }
  }

  override fun onBindViewHolder(holder: HomeItemViewHolder<*>, position: Int) {
    val item = items[position]
    when (holder) {
      is HomeAppLaunchGuardViewHolder -> {
        holder.bind(item as HomeItem.AppLaunchGuardItem)
      }

      is HomeEnableAccessibilityServiceViewHolder -> {
        holder.bind(item as HomeItem.EnableAccessibilityServiceItem)
        holder.itemView.setOnClickListener {
          onEnableAccessibilityServiceClicked.invoke()
        }
      }
    }
  }

  override fun getItemId(position: Int): Long {
    return when (val item = items[position]) {
      is HomeItem.AppLaunchGuardItem -> item.packageName.hashCode().toLong()
      is HomeItem.EnableAccessibilityServiceItem -> item.hashCode().toLong()
    }
  }

  override fun getItemCount(): Int {
    return items.size
  }

  override fun getItemViewType(position: Int): Int {
    return when (items[position]) {
      is HomeItem.AppLaunchGuardItem -> VIEW_TYPE_EXISTING_GUARD
      is HomeItem.EnableAccessibilityServiceItem -> VIEW_TYPE_ENABLE_ACCESSIBILITY_SERVICE
    }
  }

  fun updateItems(newItems: List<HomeItem>) {
    items.clear()
    items.addAll(newItems)

    notifyDataSetChanged()
  }
}