package com.dgsd.solguard.tile

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.dgsd.solguard.R
import com.dgsd.solguard.common.clock.Clock
import com.dgsd.solguard.data.AppLaunchRepository
import com.dgsd.solguard.deeplink.SolGuardDeeplinkingFactory
import com.dgsd.solguard.guard.block.ui.AppBlockActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class BlackoutModeTileService : TileService() {

  private val clock by inject<Clock>()
  private val appLaunchRepository by inject<AppLaunchRepository>()

  private var updateStateJob: Job? = null

  override fun onStartListening() {
    super.onStartListening()

    updateStateJob?.cancel()
    updateStateJob = CoroutineScope(Dispatchers.IO).launch {
      val blackoutModeEvent = appLaunchRepository.getBlackoutModeEvent(clock.now().toLocalDate())
      qsTile.state = if (blackoutModeEvent?.isEnabled == true) {
        Tile.STATE_ACTIVE
      } else {
        Tile.STATE_INACTIVE
      }
      qsTile.updateTile()
    }
  }

  override fun onStopListening() {
    updateStateJob?.cancel()
    updateStateJob = null
    super.onStopListening()
  }

  override fun onClick() {
    super.onClick()

    if (qsTile.state == Tile.STATE_INACTIVE) {
      startActivityAndCollapse(
        SolGuardDeeplinkingFactory
          .createEnableBlackoutMode(this)
          .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      )
    } else if (qsTile.state == Tile.STATE_ACTIVE) {
      startActivityAndCollapse(
        AppBlockActivity.getDisableBlackoutModeIntent(this)
      )
    }
  }

  companion object {

    fun requestAddTileService(context: Context) {
      if (Build.VERSION.SDK_INT >= 33) {
        val statusBarManager = context.getSystemService(STATUS_BAR_SERVICE) as StatusBarManager
        statusBarManager.requestAddTileService(
          ComponentName(context, BlackoutModeTileService::class.java),
          context.getString(R.string.blackout_mode_add_tile_label),
          Icon.createWithResource(context, R.drawable.system_tile_blackout_mode_icon),
          { },
          { }
        )
      }
    }
  }
}