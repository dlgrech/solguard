package com.dgsd.solguard.common.fragment

import androidx.fragment.app.Fragment

fun Fragment.generateTag(): String {
    return this::class.java.name
}

fun Fragment.navigateBack() {
    requireActivity().onBackPressedDispatcher.onBackPressed()
}