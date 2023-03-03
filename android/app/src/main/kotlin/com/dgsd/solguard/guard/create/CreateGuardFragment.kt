package com.dgsd.solguard.guard.create

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.dgsd.solguard.R
import com.dgsd.solguard.common.flow.onEach
import com.dgsd.solguard.common.fragment.navigateBack
import com.dgsd.solguard.common.modalsheet.extensions.showModalFromErrorMessage
import com.dgsd.solguard.common.ui.blur
import com.dgsd.solguard.common.ui.unblur
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

private const val ARGS_PACKAGE_TO_EDIT = "package_to_edit"

class CreateGuardFragment : Fragment(R.layout.frag_create_guard) {

  private val viewModel by viewModel<CreateGuardViewModel> {
    parametersOf(arguments?.getString(ARGS_PACKAGE_TO_EDIT))
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val inputText = view.requireViewById<TextView>(R.id.input_text)
    val saveButton = view.requireViewById<Button>(R.id.save_button)
    val deleteButton = view.requireViewById<View>(R.id.delete)

    view.requireViewById<View>(R.id.close).setOnClickListener {
      navigateBack()
    }

    saveButton.setOnClickListener {
      viewModel.onSaveClicked()
    }

    deleteButton.setOnClickListener {
      viewModel.onDeleteClicked()
    }

    onEach(viewModel.inputText) {
      inputText.text = it
    }

    onEach(viewModel.isDeleteGuardEnabled) {
      deleteButton.isVisible = it
    }

    onEach(viewModel.showAppSelection) {
      AppSelectionBottomSheetFragment
        .newInstance()
        .show(childFragmentManager, null)
    }

    onEach(viewModel.showLaunchCountSelection) {
      LaunchCountBottomSheetFragment
        .newInstance(it)
        .show(childFragmentManager, null)
    }

    onEach(viewModel.showPaymentAmountSelection) {
      TokenAmountBottomSheetFragment
        .newInstance(it)
        .show(childFragmentManager, null)
    }

    onEach(viewModel.errorMessage) {
      showModalFromErrorMessage(it)
    }

    onEach(viewModel.closeOnSuccess) { message ->
      Snackbar.make(
        view.parent as View,
        message,
        Snackbar.LENGTH_SHORT
      ).show()
      navigateBack()
    }
  }

  @Suppress("DEPRECATION")
  override fun onAttachFragment(childFragment: Fragment) {
    super.onAttachFragment(childFragment)
    when (childFragment) {
      is LaunchCountBottomSheetFragment -> {
        childFragment.listener = {
          viewModel.onLaunchCountChanged(it)
        }
      }

      is AppSelectionBottomSheetFragment -> {
        childFragment.listener = {
          viewModel.onAppSelected(it)
        }
      }

      is TokenAmountBottomSheetFragment -> {
        childFragment.listener = {
          viewModel.onPaymentAmountChanged(it)
        }
      }
    }
  }

  override fun onStart() {
    super.onStart()
    findSiblingFragmentViews().forEach(View::blur)
  }

  override fun onStop() {
    findSiblingFragmentViews().forEach(View::unblur)
    super.onStop()
  }

  private fun findSiblingFragmentViews(): List<View> {
    val thisFragmentView = view
    return (thisFragmentView?.parent as? ViewGroup)
      ?.children
      ?.filter { it != thisFragmentView }
      ?.toList()
      .orEmpty()
  }


  companion object {
    fun newInstance() = CreateGuardFragment()

    fun newEditInstance(packageToEdit: String): CreateGuardFragment {
      return CreateGuardFragment().apply {
        arguments = bundleOf(ARGS_PACKAGE_TO_EDIT to packageToEdit)
      }
    }
  }
}