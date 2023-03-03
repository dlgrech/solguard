package com.dgsd.solguard.common.fragment

import androidx.annotation.IdRes
import androidx.fragment.app.*
import com.dgsd.solguard.R
import com.dgsd.solguard.common.fragment.model.ScreenTransitionType

/**
 * Similar to [FragmentManager.findFragmentById], but ensures the fragment found is actually visible
 */
fun FragmentManager.findActiveFragmentById(id: Int): Fragment? {
    val visibleFragment = this.fragments.firstOrNull {
        it.id == id && it.isVisible
    }

    return visibleFragment ?: this.fragments.firstOrNull { it.id == id }
}

fun FragmentManager.navigate(
    @IdRes containerId: Int,
    fragment: Fragment,
    screenTransitionType: ScreenTransitionType = ScreenTransitionType.DEFAULT,
    fragmentTag: String = fragment.generateTag(),
    resetBackStack: Boolean = false,
    commitNow: Boolean = false,
    addOnTop: Boolean = false
) {
    val transaction: (FragmentTransaction.() -> Unit) = {
        if (resetBackStack) {
            fragments.forEach { remove(it) }
            setScreenTransitionType(screenTransitionType)
        } else if (findActiveFragmentById(containerId) != null) {
            setScreenTransitionType(screenTransitionType)
            addToBackStack(fragment.generateTag())
        }

        if (addOnTop) {
            add(containerId, fragment, fragmentTag)
        } else {
            replace(containerId, fragment, fragmentTag)
        }
        setPrimaryNavigationFragment(fragment)
    }

    if (commitNow) {
        commitNow(allowStateLoss = true, transaction)
    } else {
        commit(allowStateLoss = true, transaction)
    }
}

private fun FragmentTransaction.setScreenTransitionType(
    screenTransitionType: ScreenTransitionType
) {
    when (screenTransitionType) {
        ScreenTransitionType.DEFAULT -> {
            setCustomAnimations(
                R.anim.default_fragment_entry,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.fade_out,
            )
        }

        ScreenTransitionType.FADE -> {
            setCustomAnimations(
                R.anim.fade_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.fade_out
            )
        }

        ScreenTransitionType.SLIDE_FROM_BOTTOM -> {
            setCustomAnimations(
                R.anim.slide_in_from_bottom,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out_from_bottom,
            )
        }

        ScreenTransitionType.SLIDE_FROM_RIGHT -> {
            setCustomAnimations(
                R.anim.slide_in_from_right,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out_to_right,
            )
        }
    }
}