package com.dgsd.solguard.common.bottomsheet

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.dgsd.solguard.R
import com.dgsd.solguard.common.ui.enableBackgroundBlur
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class BaseBottomSheetFragment : BottomSheetDialogFragment() {

  @SuppressLint("RestrictedApi")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val window = dialog?.window
    if (window != null) {
      window.enableBackgroundBlur()
      if (activity?.window?.isFloating == true) {
        window.setWindowAnimations(R.style.FadeInWindowAnimationStyle)
      }
    }
    (dialog as? BottomSheetDialog)?.behavior?.disableShapeAnimations()
  }
}