package com.dgsd.solguard.analytics

import androidx.fragment.app.Fragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent

class SolguardAnalyticsManager(
  private val firebaseAnalytics: FirebaseAnalytics
) {

  fun logScreenOpened(fragment: Fragment) {
    logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
      put(FirebaseAnalytics.Param.SCREEN_NAME, fragment.javaClass.simpleName)
      put(FirebaseAnalytics.Param.SCREEN_CLASS, fragment.javaClass.name)
    }
  }

  fun logScreenClosed(fragment: Fragment) {
    logEvent("screen_closed") {
      put(FirebaseAnalytics.Param.SCREEN_NAME, fragment.javaClass.simpleName)
      put(FirebaseAnalytics.Param.SCREEN_CLASS, fragment.javaClass.name)
    }
  }

  fun logClick(
    itemName: String,
    addParams: MutableMap<String, String>.() -> Unit = {}
  ) {
    logEvent("click_$itemName", addParams)
  }

  fun logEvent(
    eventName: String,
    addParams: MutableMap<String, String>.() -> Unit = {}
  ) {
    firebaseAnalytics.logEvent(eventName) {
      mutableMapOf<String, String>()
        .apply { addParams() }
        .forEach { (key, value) -> param(key, value) }
    }
  }
}