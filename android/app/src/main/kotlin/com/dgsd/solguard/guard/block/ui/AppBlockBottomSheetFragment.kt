package com.dgsd.solguard.guard.block.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import coil.load
import com.airbnb.lottie.LottieAnimationView
import com.dgsd.solguard.R
import com.dgsd.solguard.charity.CharityInfoBottomSheet
import com.dgsd.solguard.common.bottomsheet.BaseBottomSheetFragment
import com.dgsd.solguard.common.flow.onEach
import com.dgsd.solguard.common.intent.IntentFactory
import com.dgsd.solguard.common.ui.getColorAttr
import com.google.android.material.snackbar.Snackbar
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.RpcCluster
import com.solana.mobilewalletadapter.clientlib.TransactionResult
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

private const val ARG_PACKAGE_NAME = "package_name"
private const val ARG_BLACKOUT_DISABLE_ONLY = "blacklist_disable_only"
private const val ARG_IS_FOR_IN_APP = "is_for_in_app"

class AppBlockBottomSheetFragment : BaseBottomSheetFragment() {

  private val intentFactory by inject<IntentFactory>()

  private val mobileWalletAdapter by inject<MobileWalletAdapter>()

  private lateinit var activityResultSender: ActivityResultSender

  private val viewModel by viewModel<AppBlockViewModel> {
    parametersOf(
      checkNotNull(requireArguments().getString(ARG_PACKAGE_NAME)),
      requireArguments().getBoolean(ARG_BLACKOUT_DISABLE_ONLY, false),
      requireArguments().getBoolean(ARG_IS_FOR_IN_APP, false),
    )
  }

  private val windowBackground by lazy {
    ColorDrawable(
      requireContext().getColorAttr(R.attr.colorError)
    ).apply {
      alpha = 0
    }
  }

  private val backgroundColorAnimator = ValueAnimator.ofInt(0, 80).apply {
    startDelay = 200
    duration = 250
    interpolator = DecelerateInterpolator()
    addUpdateListener {
      windowBackground.alpha = it.animatedValue as Int
    }
  }

  private val successColorAnimator by lazy {
    ValueAnimator.ofArgb(
      windowBackground.color,
      ContextCompat.getColor(requireContext(), R.color.success_color)
    ).apply {
      duration = 250
      interpolator = LinearInterpolator()
      addUpdateListener {
        windowBackground.color = it.animatedValue as Int
      }
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    activityResultSender = ActivityResultSender(requireActivity())
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.frag_app_block, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    dialog?.window?.setBackgroundDrawable(windowBackground)

    val loadingIndicator = view.requireViewById<View>(R.id.loading_indicator)
    val blockContent = view.requireViewById<ViewGroup>(R.id.block_content)
    val successContent = view.requireViewById<ViewGroup>(R.id.success)
    val successAnimation = view.requireViewById<LottieAnimationView>(R.id.success_animation)
    val successText = view.requireViewById<TextView>(R.id.success_message)
    val screenTitle = view.requireViewById<TextView>(R.id.title)
    val viewTransaction = view.requireViewById<View>(R.id.view_transaction)
    val message = view.requireViewById<TextView>(R.id.message)
    val closeButton = view.requireViewById<View>(R.id.close_button)
    val payButton = view.requireViewById<View>(R.id.pay_button)
    val doneButton = view.requireViewById<View>(R.id.done_button)
    val charityCard = view.requireViewById<View>(R.id.charity_card)
    val charityCardLogo = view.requireViewById<ImageView>(R.id.charity_logo)
    val charityCardTitle = view.requireViewById<TextView>(R.id.charity_card_title)
    val charityCardMessage = view.requireViewById<TextView>(R.id.charity_card_message)

    closeButton.setOnClickListener {
      dismissAllowingStateLoss()
    }

    doneButton.setOnClickListener {
      dismissAllowingStateLoss()
    }

    charityCard.setOnClickListener {
      viewModel.onCharityCardClicked()
    }

    payButton.setOnClickListener {
      viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        val result = mobileWalletAdapter.transact(activityResultSender) {
          viewModel.onPayButtonClicked(this)
        }

        val errorMessage = when (result) {
          is TransactionResult.Failure -> result.message
          is TransactionResult.NoWalletFound -> result.message
          else -> null
        }

        if (errorMessage != null) {
          showError(errorMessage)
        }
      }
    }

    onEach(viewModel.screenTitle) {
      screenTitle.text = it
    }

    onEach(viewModel.messageText) {
      message.text = it
    }

    onEach(viewModel.showErrorMessage) {
      showError(it)
    }

    onEach(viewModel.charityLogoUrl) {
      charityCardLogo.load(it) { crossfade(true) }
    }

    onEach(viewModel.charityCardTitle) {
      charityCardTitle.text = it
    }

    onEach(viewModel.charityCardMessage) {
      charityCardMessage.text = it
    }

    onEach(viewModel.showCharityInfo) {
      CharityInfoBottomSheet.newInstance(it.id).show(childFragmentManager, null)
    }

    onEach(viewModel.showSuccess) { (message, transactionSignature, rpcCluster) ->
      successText.text = message
      successContent.isVisible = true
      blockContent.isInvisible = true
      successAnimation.playAnimation()

      viewTransaction.setOnClickListener {
        startActivity(
          intentFactory.createViewTransactionIntent(transactionSignature, rpcCluster.name)
        )
      }

      if (viewModel.isBackgroundAnimationEnabled.value) {
        successColorAnimator.start()
      }

      setFragmentResult(
        RESULT_KEY_UNBLOCK_RESULT,
        bundleOf(RESULT_ARGUMENT_DID_SUCCEED to true)
      )
    }

    onEach(viewModel.showLoadingIndicator) {
      loadingIndicator.isVisible = it
      if (!successContent.isVisible) {
        blockContent.isInvisible = it
      }
    }
  }

  override fun onResume() {
    super.onResume()

    if (viewModel.isBackgroundAnimationEnabled.value) {
      backgroundColorAnimator.start()
    }
  }

  override fun onPause() {
    backgroundColorAnimator.pause()
    super.onPause()
  }

  private fun showError(errorMessage: CharSequence) {
    Snackbar.make(requireView(), errorMessage, Snackbar.LENGTH_SHORT).show()
  }

  companion object {

    const val RESULT_KEY_UNBLOCK_RESULT = "did_unblock"
    const val RESULT_ARGUMENT_DID_SUCCEED = "did_succeed"

    fun newInstance(packageName: String, isForInApp: Boolean): AppBlockBottomSheetFragment {
      return AppBlockBottomSheetFragment().apply {
        arguments = bundleOf(
          ARG_PACKAGE_NAME to packageName,
          ARG_IS_FOR_IN_APP to isForInApp,
        )
      }
    }

    fun newInstanceForDisable(packageName: String): AppBlockBottomSheetFragment {
      return AppBlockBottomSheetFragment().apply {
        arguments = bundleOf(
          ARG_PACKAGE_NAME to packageName,
          ARG_BLACKOUT_DISABLE_ONLY to true,
          ARG_IS_FOR_IN_APP to true
        )
      }
    }

    fun newInstanceForBlackoutDisable(context: Context): AppBlockBottomSheetFragment {
      return AppBlockBottomSheetFragment().apply {
        arguments = bundleOf(
          ARG_PACKAGE_NAME to context.packageName,
          ARG_BLACKOUT_DISABLE_ONLY to true,
          ARG_IS_FOR_IN_APP to true
        )
      }
    }
  }
}