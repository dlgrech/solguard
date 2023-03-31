package com.dgsd.solguard.analytics

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class SolguardScreenAnalyticsManager(
  private val activity: AppCompatActivity,
  private val analyticsManager: SolguardAnalyticsManager,
) {

  fun monitorFragmentScreens() {
    activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
      object: FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
          analyticsManager.logScreenOpened(f)
        }

        override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
          analyticsManager.logScreenClosed(f)
        }
      },
      true
    )
  }
}