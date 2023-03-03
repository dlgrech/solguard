package com.dgsd.solguard.settings

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.dgsd.solguard.R
import com.dgsd.solguard.common.actionsheet.extensions.showActionSheet
import com.dgsd.solguard.common.actionsheet.model.ActionSheetItem
import com.dgsd.solguard.common.flow.onEach
import com.dgsd.solguard.common.fragment.navigateBack
import com.dgsd.solguard.common.intent.IntentFactory
import com.dgsd.solguard.common.modalsheet.extensions.showModal
import com.dgsd.solguard.common.modalsheet.model.ModalInfo
import com.google.android.material.appbar.MaterialToolbar
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment(R.layout.frag_settings) {

  private val viewModel by viewModel<SettingsViewModel>()

  private val intentFactory by inject<IntentFactory>()

  private val notificationPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) {
    // No-op
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val loadingIndicator = view.requireViewById<View>(R.id.loading_indicator)
    val settingsContent = view.requireViewById<View>(R.id.settings_content)

    val rowDefaultTokenValue =
      view.requireViewById<ViewGroup>(R.id.settings_row_default_payment_token)
    val rowIncreasePaymentOnEachUnlock =
      view.requireViewById<ViewGroup>(R.id.settings_row_increase_payment_on_each_unlock)
    val rowShowWarningNotification =
      view.requireViewById<ViewGroup>(R.id.settings_row_show_warning_notification)
    val rowShowTimeRemainingNotification =
      view.requireViewById<ViewGroup>(R.id.settings_row_show_time_remaining_notification)
    val rowShowSuggestionsNotification =
      view.requireViewById<ViewGroup>(R.id.settings_row_show_suggestions_notification)
    val rowDonationToSolguard =
      view.requireViewById<ViewGroup>(R.id.settings_row_donation_to_solguard)

    val descriptionIncreasePaymentOnEachUnlock =
      view.requireViewById<TextView>(R.id.settings_description_increase_payment_on_each_unlock)

    val valueDefaultTokenValue =
      view.requireViewById<TextView>(R.id.settings_value_default_payment_token)
    val valueIncreasePaymentOnEachUnlock =
      view.requireViewById<TextView>(R.id.settings_value_increase_payment_on_each_unlock)
    val valueShowWarningNotification =
      view.requireViewById<TextView>(R.id.settings_value_show_warning_notification)
    val valueShowTimeRemainingNotification =
      view.requireViewById<TextView>(R.id.settings_value_show_time_remaining_notification)
    val valueShowSuggestedGuardsNotification =
      view.requireViewById<TextView>(R.id.settings_value_show_suggestions_notification)
    val valueDonationToSolguard =
      view.requireViewById<TextView>(R.id.settings_value_donation_to_solguard)

    arrayOf(
      rowDefaultTokenValue,
      rowIncreasePaymentOnEachUnlock,
      rowShowWarningNotification,
      rowShowTimeRemainingNotification,
      rowShowSuggestionsNotification,
      rowDonationToSolguard,
    ).forEach {
      it.clipToOutline = true
    }

    view.requireViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
      navigateBack()
    }

    rowDefaultTokenValue.setOnClickListener {
      viewModel.onDefaultTokenRowClicked()
    }

    rowIncreasePaymentOnEachUnlock.setOnClickListener {
      viewModel.onIncreasePaymentOnEachUnlockClicked()
    }

    rowShowWarningNotification.setOnClickListener {
      viewModel.onShowWarningNotificationClicked()
    }

    rowShowTimeRemainingNotification.setOnClickListener {
      viewModel.onShowTimeRemainingNotificationClicked()
    }

    rowShowSuggestionsNotification.setOnClickListener {
      viewModel.onShowSuggestionsNotificationClicked()
    }

    rowDonationToSolguard.setOnClickListener {
      viewModel.onDonationToSolguardClicked()
    }

    onEach(viewModel.increasingFeeDescriptionText) {
      descriptionIncreasePaymentOnEachUnlock.text = it
    }

    onEach(viewModel.defaultTokenText) {
      valueDefaultTokenValue.text = it
    }

    onEach(viewModel.increasingFeeText) {
      valueIncreasePaymentOnEachUnlock.text = it
    }

    onEach(viewModel.showWarningText) {
      valueShowWarningNotification.text = it
    }

    onEach(viewModel.showTimeRemainingText) {
      valueShowTimeRemainingNotification.text = it
    }

    onEach(viewModel.showSuggestedGuardsText) {
      valueShowSuggestedGuardsNotification.text = it
    }

    onEach(viewModel.donationToSolguardText) {
      valueDonationToSolguard.text = it
    }

    onEach(viewModel.isLoading) {
      loadingIndicator.isVisible = it
      settingsContent.isInvisible = it
    }

    onEach(viewModel.showDonateToSolGuardOptions) { options ->
      showActionSheet(
        title = getString(R.string.settings_title_donation_to_solguard),
        items = options.map { option ->
          ActionSheetItem(
            title = option.displayText,
            onClick = {
              viewModel.onDonationOptionClicked(option)
            }
          )
        }.toTypedArray()
      )
    }

    onEach(viewModel.showTokenOptions) { options ->
      showActionSheet(
        title = getString(R.string.settings_title_default_payment_token),
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

    onEach(viewModel.showNotificationPermissionWarning) { permission ->
      showModal(
        ModalInfo(
          title = getString(R.string.settings_notification_permission_warning_title),
          message = getString(R.string.settings_notification_permission_warning_message),
          positiveButton = ModalInfo.ButtonInfo(
            text = getString(R.string.settings_notification_permission_warning_positive_buttons),
            onClick = {
              if (shouldShowRequestPermissionRationale(permission)) {
                startActivity(intentFactory.createAppNotificationSettingsIntent())
              } else {
                notificationPermissionLauncher.launch(permission)
              }
            }
          ),
          negativeButton = ModalInfo.ButtonInfo(
            text = getString(R.string.common_nevermind)
          )
        )
      )
    }
  }

  companion object {

    fun newInstance() = SettingsFragment()
  }
}