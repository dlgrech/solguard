package com.dgsd.solguard.common.viewmodel

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import java.lang.ref.WeakReference

object ViewModelActivityHolder : ActivityLifecycleCallbacks {

  private var activityReference: WeakReference<Activity>? = null

  override fun onActivityResumed(activity: Activity) {
    activityReference = WeakReference(activity)
  }
  override fun onActivityPaused(activity: Activity) {
    if (activityReference?.get() == activity) {
      activityReference = null
    }
  }

  override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
  override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
  override fun onActivityDestroyed(activity: Activity) = Unit
  override fun onActivityStarted(activity: Activity) = Unit
  override fun onActivityStopped(activity: Activity) = Unit

  fun getCurrentActivity(): Activity? {
    return activityReference?.get()
  }
}