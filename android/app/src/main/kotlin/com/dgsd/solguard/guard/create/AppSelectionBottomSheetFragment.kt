package com.dgsd.solguard.guard.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dgsd.solguard.R
import com.dgsd.solguard.common.bottomsheet.BaseBottomSheetFragment
import com.dgsd.solguard.common.flow.onEach
import com.dgsd.solguard.common.modalsheet.extensions.showModalFromErrorMessage
import com.dgsd.solguard.common.ui.getColorAttr
import com.dgsd.solguard.model.InstalledAppInfo
import com.dgsd.solguard.onboarding.appselection.OnboardingAppSelectionItemDecoration
import org.koin.androidx.viewmodel.ext.android.viewModel


class AppSelectionBottomSheetFragment : BaseBottomSheetFragment() {

  private val viewModel by viewModel<AppSelectionViewModel>()

  var listener: ((InstalledAppInfo) -> Unit)? = null

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.frag_app_selection_bottom_sheet, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val recyclerView = view.requireViewById<RecyclerView>(R.id.recyclerview)
    val loadingIndicator = view.requireViewById<View>(R.id.loading_indicator)

    val adapter = AppSelectionAdapter(
      onAppClicked = {
        listener?.invoke(it)
        dismissAllowingStateLoss()
      }
    )

    recyclerView.layoutManager = LinearLayoutManager(requireContext())
    recyclerView.adapter = adapter
    recyclerView.addItemDecoration(
      OnboardingAppSelectionItemDecoration(
        requireContext().getColorAttr(R.attr.colorPrimaryVariant),
        resources.getDimensionPixelSize(R.dimen.app_selection_item_divider_padding)
      )
    )

    onEach(viewModel.appSelectionItems) {
      adapter.updateItems(it)
    }

    onEach(viewModel.errorMessage) {
      showModalFromErrorMessage(it)
    }

    onEach(viewModel.isLoading) {
      loadingIndicator.isVisible = it
      recyclerView.isInvisible = it
    }
  }

  override fun onStart() {
    super.onStart()
    viewModel.onStart()
  }

  override fun onDestroyView() {
    listener = null
    super.onDestroyView()
  }

  companion object {

    fun newInstance() = AppSelectionBottomSheetFragment()
  }
}