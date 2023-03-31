package com.dgsd.solguard.guard.block.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.dgsd.solguard.analytics.SolguardScreenAnalyticsManager
import com.dgsd.solguard.common.intent.IntentFactory
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope

private const val EXTRA_PACKAGE_BEING_BLOCKED = "EXTRA_PACKAGE_BEING_BLOCKED"
private const val EXTRA_IS_FOR_DISABLE = "EXTRA_IS_FOR_DISABLE"
private const val EXTRA_HOME_ON_DISMISS = "EXTRA_HOME_ON_DISMISS"

class AppBlockActivity : AppCompatActivity(), AndroidScopeComponent {

  override val scope by activityScope()

  private val analytics by scope.inject<SolguardScreenAnalyticsManager>()
  private val intentFactory by inject<IntentFactory>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    analytics.monitorFragmentScreens()

    val isForDisableOnly = intent.getBooleanExtra(EXTRA_IS_FOR_DISABLE, false)
    val packageName = intent.getStringExtra(EXTRA_PACKAGE_BEING_BLOCKED)
    val homeOnDismiss = intent.getBooleanExtra(EXTRA_HOME_ON_DISMISS, true)
    val fragment =
      when {
        isForDisableOnly && packageName != null -> {
          AppBlockBottomSheetFragment.newInstanceForDisable(packageName)
        }
        isForDisableOnly -> {
          AppBlockBottomSheetFragment.newInstanceForBlackoutDisable(this)
        }
        else -> {
          AppBlockBottomSheetFragment.newInstance(checkNotNull(packageName), !homeOnDismiss)
        }
      }

    show(fragment, navigateHomeOnDismiss = homeOnDismiss)
  }

  private fun show(
    appBlockFragment: AppBlockBottomSheetFragment,
    navigateHomeOnDismiss: Boolean,
  ) {
    var didSucceed = false
    supportFragmentManager.setFragmentResultListener(
      AppBlockBottomSheetFragment.RESULT_KEY_UNBLOCK_RESULT,
      this
    ) { _, results ->
      didSucceed =
        results.getBoolean(AppBlockBottomSheetFragment.RESULT_ARGUMENT_DID_SUCCEED, false)
    }

    supportFragmentManager.registerFragmentLifecycleCallbacks(
      object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
          if (f == appBlockFragment) {
            fm.unregisterFragmentLifecycleCallbacks(this)

            if (!didSucceed && navigateHomeOnDismiss) {
              startActivity(intentFactory.createHomeIntent())
            }

            finish()
          }
        }
      },
      false
    )

    appBlockFragment.show(supportFragmentManager, null)
  }

  companion object {

    fun getLaunchIntent(
      context: Context,
      packageNameBeingBlocked: String,
      homeOnDismiss: Boolean,
    ): Intent {
      return Intent(context, AppBlockActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .putExtra(EXTRA_PACKAGE_BEING_BLOCKED, packageNameBeingBlocked)
        .putExtra(EXTRA_HOME_ON_DISMISS, homeOnDismiss)
    }

    fun getDisableBlackoutModeIntent(context: Context): Intent {
      return Intent(context, AppBlockActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .putExtra(EXTRA_IS_FOR_DISABLE, true)
        .putExtra(EXTRA_HOME_ON_DISMISS, false)
    }

    fun getDisableAppBlockIntent(context: Context, packageName: String): Intent {
      return Intent(context, AppBlockActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .putExtra(EXTRA_IS_FOR_DISABLE, true)
        .putExtra(EXTRA_HOME_ON_DISMISS, false)
        .putExtra(EXTRA_PACKAGE_BEING_BLOCKED, packageName)
    }
  }
}
