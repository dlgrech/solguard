package com.dgsd.solguard.onboarding.appselection

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dgsd.solguard.R
import com.dgsd.solguard.common.flow.onEach
import com.dgsd.solguard.common.fragment.navigateBack
import com.dgsd.solguard.common.modalsheet.extensions.showModalFromErrorMessage
import com.dgsd.solguard.common.ui.getColorAttr
import com.dgsd.solguard.di.util.parentViewModel
import com.dgsd.solguard.onboarding.OnboardingCoordinator
import com.google.android.material.appbar.MaterialToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class OnboardingAppSelectionFragment : Fragment(R.layout.frag_onboarding_app_selection) {

  private val coordinator by parentViewModel<OnboardingCoordinator>()
  private val viewModel by viewModel<OnboardingAppSelectionViewModel>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val recyclerView = view.requireViewById<RecyclerView>(R.id.recyclerview)
    val loadingIndicator = view.requireViewById<View>(R.id.loading_indicator)

    view.requireViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
      navigateBack()
    }

    val adapter = OnboardingAppSelectionAdapter(
      onAppClicked = { appInfo ->
        coordinator.navigateWithApp(appInfo)
      },
    )

    val layoutManager = LinearLayoutManager(requireContext())

    recyclerView.layoutManager = layoutManager
    recyclerView.adapter = adapter
    recyclerView.addItemDecoration(
      OnboardingAppSelectionItemDecoration(
        requireContext().getColorAttr(R.attr.colorPrimaryVariant),
        resources.getDimensionPixelSize(R.dimen.home_tab_item_launch_guard_divider_padding)
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
      recyclerView.isVisible = !it
    }
  }

  override fun onStart() {
    super.onStart()
    viewModel.onStart()
  }

  companion object {

    fun newInstance() = OnboardingAppSelectionFragment()
  }
}