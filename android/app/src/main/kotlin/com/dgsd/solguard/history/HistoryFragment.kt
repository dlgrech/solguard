package com.dgsd.solguard.history

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dgsd.solguard.R
import com.dgsd.solguard.common.flow.onEach
import com.dgsd.solguard.common.fragment.navigateBack
import com.dgsd.solguard.common.ui.getColorAttr
import com.google.android.material.appbar.MaterialToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class HistoryFragment : Fragment(R.layout.frag_history) {

  private val viewModel by viewModel<HistoryViewModel>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val recyclerView = view.requireViewById<RecyclerView>(R.id.recyclerview)
    val loadingIndicator = view.requireViewById<View>(R.id.loading_indicator)

    view.requireViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
      navigateBack()
    }

    val layoutManager = LinearLayoutManager(requireContext())
    val adapter = HistoryAdapter()

    recyclerView.layoutManager = layoutManager
    recyclerView.adapter = adapter
    recyclerView.addItemDecoration(
      HistoryItemDecoration(requireContext().getColorAttr(R.attr.colorPrimaryVariant))
    )

    onEach(viewModel.isLoading) { isLoading ->
      loadingIndicator.isVisible = isLoading
    }

    onEach(viewModel.items) { items ->
      adapter.updateItems(items)
    }
  }

  companion object {

    fun newInstance() = HistoryFragment()
  }
}