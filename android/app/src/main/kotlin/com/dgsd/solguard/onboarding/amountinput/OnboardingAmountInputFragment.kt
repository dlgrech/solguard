package com.dgsd.solguard.onboarding.amountinput

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.dgsd.solguard.R
import com.dgsd.solguard.common.actionsheet.extensions.showActionSheet
import com.dgsd.solguard.common.actionsheet.model.ActionSheetItem
import com.dgsd.solguard.common.flow.onEach
import com.dgsd.solguard.common.fragment.navigateBack
import com.dgsd.solguard.common.modalsheet.extensions.showModalFromErrorMessage
import com.dgsd.solguard.di.util.parentViewModel
import com.dgsd.solguard.onboarding.OnboardingCoordinator
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class OnboardingAmountInputFragment : Fragment(R.layout.frag_onboarding_amount_input) {

  private val coordinator by parentViewModel<OnboardingCoordinator>()
  private val viewModel by viewModel<OnboardingAmountInputViewModel> {
    parametersOf(
      checkNotNull(coordinator.createGuardAppInfo),
      checkNotNull(coordinator.appLaunchLimit),
    )
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val message = view.requireViewById<TextView>(R.id.message)
    val amountInput = view.requireViewById<TextInputEditText>(R.id.amount_input)
    val amountInputWrapper = view.requireViewById<TextInputLayout>(R.id.amount_input_wrapper)

    view.requireViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
      navigateBack()
    }

    amountInput.doAfterTextChanged {
      viewModel.onAmountInputTextChanged(it?.toString().orEmpty())
    }

    amountInput.setOnEditorActionListener { _, actionId, _ ->
      if (actionId == EditorInfo.IME_ACTION_DONE) {
        viewModel.onNextClicked()
        true
      } else {
        false
      }
    }


    view.requireViewById<View>(R.id.next_button).setOnClickListener {
      viewModel.onNextClicked()
    }

    onEach(viewModel.screenMessageText) {
      message.text = it
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

    onEach(viewModel.amountInput) { value ->
      if (!TextUtils.equals(amountInput.text, value)) {
        amountInput.setText(value)
      }
    }

    onEach(viewModel.errorMessage) {
      showModalFromErrorMessage(it)
    }

    onEach(viewModel.selectedPaymentToken) { token ->
      amountInputWrapper.endIconDrawable = ContextCompat.getDrawable(requireContext(), token.icon)
      amountInputWrapper.suffixText = token.displayName
    }

    onEach(viewModel.continueWithFlow) {
      coordinator.navigateWithAmount(it)
    }
  }

  companion object {

    fun newInstance() = OnboardingAmountInputFragment()
  }
}