package androidx.recyclerview.widget

import androidx.core.view.children

class ClearableItemTouchHelper(
  private val recyclerView: RecyclerView,
  callback: Callback
) : ItemTouchHelper(callback) {

  fun clearSelection() {
    recyclerView.children
      .mapNotNull(recyclerView::getChildViewHolder)
      .forEach(::startSwipe)
  }
}