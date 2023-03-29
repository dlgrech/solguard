package com.dgsd.solguard.guard.create

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import com.dgsd.ksol.core.model.PublicKey
import com.dgsd.solguard.R
import com.dgsd.solguard.common.actionsheet.extensions.showActionSheet
import com.dgsd.solguard.common.actionsheet.model.ActionSheetItem
import com.dgsd.solguard.common.bottomsheet.BaseBottomSheetFragment
import com.dgsd.solguard.common.flow.onEach
import com.dgsd.solguard.model.TokenAmount
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


private const val ARG_AMOUNT = "amount"
private const val ARG_TOKEN = "token"

class TokenAmountBottomSheetFragment : BaseBottomSheetFragment() {

  private val viewModel by viewModel<TokenAmountViewModel> {
    val amount = arguments?.getLong(ARG_AMOUNT)
    val token = arguments?.getString(ARG_TOKEN)

    if (amount == null || token == null) {
      parametersOf(null as TokenAmount?)
    } else {
      parametersOf(TokenAmount(amount, PublicKey.fromBase58(token)))
    }
  }

  var listener: ((TokenAmount) -> Unit)? = null

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.frag_token_amount_bottom_sheet, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val amountInput = view.requireViewById<TextInputEditText>(R.id.amount_input)
    val amountInputWrapper = view.requireViewById<TextInputLayout>(R.id.amount_input_wrapper)

    view.requireViewById<View>(R.id.done).setOnClickListener {
      dismissAllowingStateLoss()
    }

    amountInput.setOnEditorActionListener { _, actionId, _ ->
      if (actionId == EditorInfo.IME_ACTION_DONE) {
        dismissAllowingStateLoss()
        true
      } else {
        false
      }
    }

    amountInput.doAfterTextChanged {
      viewModel.onAmountInputTextChanged(it?.toString().orEmpty())
    }

//    onEach(viewModel.tokenOptions) { options ->
//      amountInputWrapper.setEndIconOnClickListener {
//        showActionSheet(
//          title = null,
//          items = options.map { option ->
//            ActionSheetItem(
//              title = option.displayName,
//              icon = ContextCompat.getDrawable(requireContext(), option.icon),
//              onClick = {
//                viewModel.onTokenOptionClicked(option)
//              }
//            )
//          }.toTypedArray()
//        )
//      }
//    }

    onEach(viewModel.amountInputHintText) {
      amountInputWrapper.hint = it
    }

    onEach(viewModel.selectedPaymentToken) { token ->
      amountInputWrapper.endIconDrawable = ContextCompat.getDrawable(requireContext(), token.icon)
      amountInputWrapper.suffixText = token.displayName
    }

    onEach(viewModel.rawAmountInput) { value ->
      if (!TextUtils.equals(amountInput.text, value)) {
        amountInput.setText(value)
      }
    }

    onEach(viewModel.amount) {
      listener?.invoke(it)
    }
  }

  override fun onDestroyView() {
    listener = null
    super.onDestroyView()
  }

  companion object {
    fun newInstance(tokenAmount: TokenAmount?): TokenAmountBottomSheetFragment {
      return TokenAmountBottomSheetFragment().apply {
        if (tokenAmount != null) {
          arguments = bundleOf(
            ARG_AMOUNT to tokenAmount.amount,
            ARG_TOKEN to tokenAmount.tokenAddress.toBase58String()
          )
        }
      }
    }
  }
}