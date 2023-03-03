package com.dgsd.solguard.guard.blackout

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.dgsd.solguard.R
import com.dgsd.solguard.common.actionsheet.extensions.showActionSheet
import com.dgsd.solguard.common.actionsheet.model.ActionSheetItem
import com.dgsd.solguard.common.bottomsheet.BaseBottomSheetFragment
import com.dgsd.solguard.common.flow.onEach
import com.dgsd.solguard.common.modalsheet.extensions.showModalFromErrorMessage
import com.dgsd.solguard.tile.BlackoutModeTileService
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class EnableBlackoutModeBottomSheetFragment : BaseBottomSheetFragment() {

  private val viewModel by viewModel<EnableBlackoutModeViewModel>()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.frag_enable_blackout_mode, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val amountInput = view.requireViewById<TextInputEditText>(R.id.amount_input)
    val amountInputWrapper = view.requireViewById<TextInputLayout>(R.id.amount_input_wrapper)

    view.requireViewById<View>(R.id.enabled).setOnClickListener {
      viewModel.onEnableConfirmed()
    }

    amountInput.doAfterTextChanged {
      viewModel.onAmountInputTextChanged(it?.toString().orEmpty())
    }

    onEach(viewModel.tokenOptions) { options ->
      amountInputWrapper.setEndIconOnClickListener {
        showActionSheet(
          title = null,
          items = options.map { option ->
            ActionSheetItem(
              title = option.displayName,
              icon = ContextCompat.getDrawable(requireContext(), option.icon),
              onClick = {
                viewModel.onTokenOptionClicked(option)
              }
            )
          }.toTypedArray()
        )
      }
    }


    onEach(viewModel.amountInputHintText) {
      amountInputWrapper.hint = it
    }

    onEach(viewModel.selectedPaymentToken) { token ->
      amountInputWrapper.endIconDrawable = ContextCompat.getDrawable(requireContext(), token.icon)
      amountInputWrapper.suffixText = token.displayName
    }

    onEach(viewModel.amountInput) { value ->
      if (!TextUtils.equals(amountInput.text, value)) {
        amountInput.setText(value)
      }
    }

    onEach(viewModel.errorMessage) {
      showModalFromErrorMessage(it)
    }

    onEach(viewModel.showAddQsTileUpsell) {
      BlackoutModeTileService.requestAddTileService(requireContext())
    }

    onEach(viewModel.closeOnSuccess) { message ->
      Snackbar.make(
        checkNotNull(activity?.supportFragmentManager?.primaryNavigationFragment?.requireView()),
        message,
        Snackbar.LENGTH_SHORT
      ).show()
      dismissAllowingStateLoss()
    }
  }

  companion object {

    fun newInstance() = EnableBlackoutModeBottomSheetFragment()
  }
}