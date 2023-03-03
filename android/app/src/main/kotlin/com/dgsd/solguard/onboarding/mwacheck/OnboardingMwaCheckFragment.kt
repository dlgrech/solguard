package com.dgsd.solguard.onboarding.mwacheck

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.dgsd.solguard.R
import com.dgsd.solguard.common.flow.onEach
import com.dgsd.solguard.common.fragment.navigateBack
import com.dgsd.solguard.common.intent.IntentFactory
import com.dgsd.solguard.common.ui.bold
import com.dgsd.solguard.common.ui.underlined
import com.dgsd.solguard.di.util.parentViewModel
import com.dgsd.solguard.mwa.MobileWalletAdapterAvailabilityManager
import com.dgsd.solguard.onboarding.OnboardingCoordinator
import com.google.android.material.appbar.MaterialToolbar
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class OnboardingMwaCheckFragment : Fragment(R.layout.frag_onboarding_mwa) {

  private val intentFactory by inject<IntentFactory>()

  private val coordinator by parentViewModel<OnboardingCoordinator>()
  private val viewModel by viewModel<OnboardingMwaCheckViewModel>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val loadingIndicator = view.requireViewById<View>(R.id.loading_indicator)
    val message = view.requireViewById<TextView>(R.id.message)
    val nextButton = view.requireViewById<View>(R.id.next_button)

    view.requireViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
      navigateBack()
    }

    nextButton.setOnClickListener {
      coordinator.navigateToNextStep()
    }

    onEach(viewModel.showLoadingState) {
      loadingIndicator.isVisible = it
    }

    onEach(viewModel.installedWallets) { wallets ->
      nextButton.isVisible = wallets.isNotEmpty()

      if (wallets.isEmpty()) {
        message.text = TextUtils.expandTemplate(
          getString(R.string.onboarding_mwa_check_message_no_wallets_template),
          getString(R.string.onboarding_mwa_check_message_no_wallets_find_a_wallet).bold().underlined()
        )
        message.setOnClickListener {
          startActivity(
            intentFactory.createUrlIntent(
              MobileWalletAdapterAvailabilityManager.DOWNLOAD_A_WALLET_URL
            )
          )
        }
      } else {
        message.text = TextUtils.expandTemplate(
          getString(R.string.onboarding_mwa_check_message_template),
          wallets.first().displayName.bold()
        )
        message.setOnClickListener(null)
      }
    }
  }

  companion object {

    fun newInstance() = OnboardingMwaCheckFragment()
  }
}