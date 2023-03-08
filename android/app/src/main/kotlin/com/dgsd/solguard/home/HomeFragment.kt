package com.dgsd.solguard.home

import android.graphics.Canvas
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ClearableItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.Callback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dgsd.solguard.AppCoordinator
import com.dgsd.solguard.R
import com.dgsd.solguard.common.flow.onEach
import com.dgsd.solguard.common.intent.IntentFactory
import com.dgsd.solguard.common.modalsheet.extensions.showModal
import com.dgsd.solguard.common.modalsheet.extensions.showModalFromErrorMessage
import com.dgsd.solguard.common.modalsheet.model.ModalInfo
import com.dgsd.solguard.common.ui.bold
import com.dgsd.solguard.common.view.ToggleableItemTouchListenerRecyclerView
import com.dgsd.solguard.home.model.HomeItem
import com.dgsd.solguard.home.viewholder.HomeAppLaunchGuardViewHolder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import me.saket.cascade.CascadePopupMenu
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment(R.layout.frag_home) {

  private val appCoordinator by activityViewModel<AppCoordinator>()
  private val viewModel by viewModel<HomeViewModel>()
  private val intentFactory by inject<IntentFactory>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val recyclerView =
      view.requireViewById<ToggleableItemTouchListenerRecyclerView>(R.id.recyclerview)
    val emptyMessage = view.requireViewById<View>(R.id.empty_message)
    val loadingIndicator = view.requireViewById<View>(R.id.loading_indicator)
    val blackoutModeButton = view.requireViewById<ImageView>(R.id.blackout_mode)

    view.requireViewById<ExtendedFloatingActionButton>(R.id.create_new).setOnClickListener {
      appCoordinator.navigateToNewGuard()
    }

    blackoutModeButton.setOnClickListener {
      viewModel.onBlackoutModeClicked()
    }

    view.requireViewById<ImageView>(R.id.overflow).apply{
      if (true) {
        setImageResource(R.drawable.ic_baseline_settings_24)
        setOnClickListener {
          appCoordinator.navigateToSettings()
        }
      } else {
        // TODO: Re-enable history
        setOnClickListener {
          CascadePopupMenu(requireContext(), it).apply {
            inflate(R.menu.home_overflow_menu)
            setOnMenuItemClickListener {
              when (it?.itemId) {
                R.id.settings -> appCoordinator.navigateToSettings()
                R.id.history -> appCoordinator.navigateToHistory()
              }
              true
            }
            show()
          }
        }
      }
    }

    val itemTouchHelper = ClearableItemTouchHelper(
      recyclerView,
      object : Callback() {

        override fun onMove(
          recyclerView: RecyclerView,
          viewHolder: RecyclerView.ViewHolder,
          target: RecyclerView.ViewHolder
        ): Boolean = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

        override fun getMovementFlags(
          recyclerView: RecyclerView,
          viewHolder: RecyclerView.ViewHolder
        ): Int {
          return if (viewHolder is HomeAppLaunchGuardViewHolder && viewHolder.isDraggingEnabled()) {
            makeMovementFlags(0, ItemTouchHelper.START or ItemTouchHelper.END)
          } else {
            0
          }
        }

        override fun clearView(rv: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
          getDefaultUIUtil()
          if (viewHolder is HomeAppLaunchGuardViewHolder) {
            viewHolder.onDrag(0f)
          }
        }

        override fun onChildDraw(
          c: Canvas,
          recyclerView: RecyclerView,
          viewHolder: RecyclerView.ViewHolder,
          dX: Float,
          dY: Float,
          actionState: Int,
          isCurrentlyActive: Boolean
        ) {
          if (viewHolder is HomeAppLaunchGuardViewHolder) {
            viewHolder.onDrag(dX)
          }
        }
      }
    )
    itemTouchHelper.attachToRecyclerView(recyclerView)

    val adapter = HomeTabAdapter(
      onExistingAppGuardClicked = { item ->
        itemTouchHelper.clearSelection()
        viewModel.onAppGuardClicked(item)
      },
      onEnableAccessibilityServiceClicked = {
        itemTouchHelper.clearSelection()
        startActivity(intentFactory.createAccessibilitySettingsIntent())
      },
      onExistingAppGuardDeleteClicked = { packageName ->
        viewModel.onDeleteExistingAppGuardClicked(packageName)
      },
      onExistingAppGuardBlackoutClicked = { item ->
        showConfirmAppBlackoutMode(item, itemTouchHelper)
      },
      onExistingAppGuardEnableDisableClicked = { packageName ->
        viewModel.onToggleExistingAppGuardEnabledClicked(packageName)
      }
    )

    recyclerView.layoutManager = LinearLayoutManager(requireContext())
    recyclerView.adapter = adapter

    onEach(viewModel.homeItems) {
      adapter.updateItems(it)
    }

    onEach(viewModel.showEmptyState) {
      emptyMessage.isVisible = it
      recyclerView.isInvisible = it
    }

    onEach(viewModel.isBlackoutModeEnabled) {
      if (it) {
        blackoutModeButton.setImageResource(R.drawable.ic_baseline_shield_remove_outline_24)
      } else {
        blackoutModeButton.setImageResource(R.drawable.ic_baseline_shield_outline_24)
      }
    }

    onEach(viewModel.errorMessage) {
      showModalFromErrorMessage(it)
    }

    onEach(viewModel.showEnableBlackoutMode) {
      appCoordinator.navigateToEnableBlackoutMode()
    }

    onEach(viewModel.showDisableBlackoutMode) {
      appCoordinator.navigateToDisableBlackoutMode()
    }

    onEach(viewModel.showAppBlocked) {
      appCoordinator.navigateToAppBlock(it)
    }

    onEach(viewModel.showDisableAppBlackout) {
      appCoordinator.navigateToDisableAppBlackoutMode(it)
    }

    onEach(viewModel.showEditAppLaunchGuard) {
      appCoordinator.navigateToEditGuard(it)
    }

    onEach(viewModel.isLoading) {
      loadingIndicator.isVisible = it
    }

    onEach(viewModel.showDeletedSuccessMessage) {
      Snackbar.make(requireView(), it, Snackbar.LENGTH_SHORT).show()
    }

    onEach(viewModel.showAppBlackoutModeSuccess) {
      Snackbar.make(requireView(), it, Snackbar.LENGTH_SHORT).show()
    }
  }

  override fun onStart() {
    super.onStart()
    viewModel.onStart()
  }

  private fun showConfirmAppBlackoutMode(
    item: HomeItem.AppLaunchGuardItem,
    itemTouchHelper: ClearableItemTouchHelper
  ) {
    showModal(
      modalInfo = ModalInfo(
        title = getString(R.string.common_are_you_sure),
        message = TextUtils.expandTemplate(
          getString(R.string.enable_app_blackout_confirmation_message_template),
          item.displayName.bold()
        ),
        negativeButton = ModalInfo.ButtonInfo(getString(R.string.common_cancel)),
        positiveButton = ModalInfo.ButtonInfo(getString(R.string.enable_app_blackout_confirmation_positive_button)) {
          itemTouchHelper.clearSelection()
          viewModel.onEnableAppBlackout(item.packageName)
        }
      )
    )
  }

  companion object {

    fun newInstance() = HomeFragment()
  }
}