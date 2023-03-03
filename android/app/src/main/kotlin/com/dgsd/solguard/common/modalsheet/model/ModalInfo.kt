package com.dgsd.solguard.common.modalsheet.model

data class ModalInfo(
  val title: CharSequence?,
  val message: CharSequence?,
  val positiveButton: ButtonInfo,
  val negativeButton: ButtonInfo? = null,
  val onDismiss: (() -> Unit)? = null
) {

  data class ButtonInfo(
    val text: CharSequence,
    val onClick: (() -> Unit)? = null
  )
}