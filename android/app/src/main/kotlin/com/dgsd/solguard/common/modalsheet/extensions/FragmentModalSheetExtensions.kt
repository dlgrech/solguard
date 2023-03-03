package com.dgsd.solguard.common.modalsheet.extensions

import androidx.fragment.app.Fragment
import com.dgsd.solguard.R
import com.dgsd.solguard.common.modalsheet.ModalSheetFragment
import com.dgsd.solguard.common.modalsheet.model.ModalInfo

fun Fragment.showModal(modalInfo: ModalInfo) {
  val fragment = ModalSheetFragment()
  fragment.modalInfo = modalInfo
  fragment.show(childFragmentManager, null)
}

fun Fragment.showModalFromErrorMessage(message: CharSequence) {
  showModal(
    modalInfo = ModalInfo(
      title = getString(R.string.error_modal_default_title),
      message = message,
      positiveButton = ModalInfo.ButtonInfo(
        getString(android.R.string.ok)
      )
    )
  )
}