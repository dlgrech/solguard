package com.dgsd.solguard.charity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import coil.load
import com.dgsd.solguard.R
import com.dgsd.solguard.common.bottomsheet.BaseBottomSheetFragment
import com.dgsd.solguard.common.flow.onEach
import com.dgsd.solguard.common.intent.IntentFactory
import com.dgsd.solguard.common.modalsheet.extensions.showModalFromErrorMessage
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

private const val ARG_CHARITY_ID = "charity_id"

class CharityInfoBottomSheet : BaseBottomSheetFragment() {

  private val intentFactory by inject<IntentFactory>()

  private val viewModel by viewModel<CharityInfoViewModel> {
    parametersOf(checkNotNull(requireArguments().getString(ARG_CHARITY_ID)))
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.frag_charity_info, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val screenTitle = view.requireViewById<TextView>(R.id.screen_title)
    val description = view.requireViewById<TextView>(R.id.description)
    val logo = view.requireViewById<ImageView>(R.id.logo)
    val learnMore = view.requireViewById<View>(R.id.learn_more)

    onEach(viewModel.isLoading) {

    }

    onEach(viewModel.charity) { charity ->
      screenTitle.text = charity.name
      description.text = charity.longDescription
      logo.load(charity.imageUrl) { crossfade(true) }

      learnMore.setOnClickListener {
        startActivity(
          intentFactory.createUrlIntent(charity.url)
        )
      }
    }

    onEach(viewModel.errorMessage) {
      showModalFromErrorMessage(it)
    }
  }

  companion object {

    fun newInstance(charityId: String): CharityInfoBottomSheet {
      return CharityInfoBottomSheet().apply {
        arguments = bundleOf(ARG_CHARITY_ID to charityId)
      }
    }
  }
}