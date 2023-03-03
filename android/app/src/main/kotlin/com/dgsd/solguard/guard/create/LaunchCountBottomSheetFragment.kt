package com.dgsd.solguard.guard.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.dgsd.solguard.R
import com.dgsd.solguard.common.bottomsheet.BaseBottomSheetFragment
import com.dgsd.solguard.common.view.NumberPickerView

private const val ARG_DEFAULT_VALUE = "default_value"

class LaunchCountBottomSheetFragment : BaseBottomSheetFragment() {

  var listener: ((Int) -> Unit)? = null

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.frag_launch_count_bottom_sheet, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val limitPerDayPicker = view.requireViewById<NumberPickerView>(R.id.limit_per_day)

    limitPerDayPicker.setSelected(requireArguments().getInt(ARG_DEFAULT_VALUE))
    limitPerDayPicker.setOnPositionChangedListener {
      listener?.invoke(it)
    }

    view.requireViewById<View>(R.id.done).setOnClickListener {
      dismissAllowingStateLoss()
    }
  }

  override fun onDestroyView() {
    listener = null
    super.onDestroyView()
  }

  companion object {

    fun newInstance(defaultValue: Int): LaunchCountBottomSheetFragment {
      return LaunchCountBottomSheetFragment().apply {
        arguments = bundleOf(ARG_DEFAULT_VALUE to defaultValue)
      }
    }
  }
}