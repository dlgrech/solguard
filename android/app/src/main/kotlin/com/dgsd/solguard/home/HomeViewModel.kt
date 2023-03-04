package com.dgsd.solguard.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dgsd.solguard.R
import com.dgsd.solguard.common.clock.Clock
import com.dgsd.solguard.common.error.ErrorMessageFactory
import com.dgsd.solguard.common.flow.*
import com.dgsd.solguard.common.resource.ResourceFlowConsumer
import com.dgsd.solguard.common.viewmodel.getContext
import com.dgsd.solguard.common.viewmodel.getString
import com.dgsd.solguard.data.AppConfigRepository
import com.dgsd.solguard.data.AppLaunchRepository
import com.dgsd.solguard.data.InstalledAppInfoRepository
import com.dgsd.solguard.guard.block.AppBlockAccessibilityService
import com.dgsd.solguard.guard.block.manager.AppBlockManager
import com.dgsd.solguard.home.model.HomeItem
import com.dgsd.solguard.model.AppLaunchEvent
import com.dgsd.solguard.model.AppLaunchGuard
import com.dgsd.solguard.model.BlackoutAppEvent
import com.dgsd.solguard.model.InstalledAppInfo
import com.dgsd.solguard.mwa.MobileWalletAdapterAvailabilityManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
  application: Application,
  private val clock: Clock,
  private val errorMessageFactory: ErrorMessageFactory,
  private val installedAppInfoRepository: InstalledAppInfoRepository,
  private val appLaunchRepository: AppLaunchRepository,
  private val mwaAvailabilityManager: MobileWalletAdapterAvailabilityManager,
  private val appBlockManager: AppBlockManager,
  private val appConfigRepository: AppConfigRepository,
) : AndroidViewModel(application) {

  private val _showEnableBlackoutMode = SimpleMutableEventFlow()
  val showEnableBlackoutMode = _showEnableBlackoutMode.asEventFlow()

  private val _showDisableBlackoutMode = SimpleMutableEventFlow()
  val showDisableBlackoutMode = _showDisableBlackoutMode.asEventFlow()

  private val installedAppsResourceConsumer =
    ResourceFlowConsumer<List<InstalledAppInfo>>(viewModelScope)
  private val existingGuardsResourceConsumer =
    ResourceFlowConsumer<List<AppLaunchGuard>>(viewModelScope)
  private val historicalRecordsResourceConsumer =
    ResourceFlowConsumer<List<AppLaunchEvent>>(viewModelScope)
  private val isBlackoutModeEnabledResourceConsumer =
    ResourceFlowConsumer<Boolean>(viewModelScope)
  private val deleteGuardResourceConsumer =
    ResourceFlowConsumer<Unit>(viewModelScope)
  private val appBlackoutEventsResourceConsumer =
    ResourceFlowConsumer<List<BlackoutAppEvent>>(viewModelScope)
  private val enableAppBlackoutModeResourceConsumer =
    ResourceFlowConsumer<BlackoutAppEvent>(viewModelScope)
  private val toggleGuardEnabledResourceConsumer =
    ResourceFlowConsumer<String?>(viewModelScope)

  val isLoading = combine(
    installedAppsResourceConsumer.isLoadingWithNoData,
    existingGuardsResourceConsumer.isLoading,
    historicalRecordsResourceConsumer.isLoading,
    appBlackoutEventsResourceConsumer.isLoading,
  ) { installedAppLoading, existingGuardsLoading, historicalRecordsLoading, appBlackoutLoading ->
    installedAppLoading || existingGuardsLoading || historicalRecordsLoading || appBlackoutLoading
  }

  val errorMessage = combine(
    installedAppsResourceConsumer.error,
    existingGuardsResourceConsumer.error,
    historicalRecordsResourceConsumer.error,
    appBlackoutEventsResourceConsumer.error,
    enableAppBlackoutModeResourceConsumer.error,
  ) { installedAppError, existingGuardsError, historicalRecordsError, appBlackoutError, enableAppBlackoutError ->
    installedAppError
      ?: existingGuardsError
      ?: historicalRecordsError
      ?: appBlackoutError
    ?: enableAppBlackoutError
  }.mapNotNull { error ->
    if (error == null) {
      null
    } else {
      errorMessageFactory.create(error)
    }
  }.asEventFlow(viewModelScope)

  val homeItems = combine(
    installedAppsResourceConsumer.data.filterNotNull(),
    existingGuardsResourceConsumer.data.filterNotNull(),
    historicalRecordsResourceConsumer.data.filterNotNull(),
    appBlackoutEventsResourceConsumer.data.filterNotNull()
  ) { installedApps, existingAppGuard, historicalRecords, appBlackoutRecords ->
    HomeItemFactory.create(
      context = getContext(),
      isAppBlockServiceEnabled = AppBlockAccessibilityService.isEnabled(application),
      installedApps = installedApps,
      appLaunchGuards = existingAppGuard,
      todaysHistory = historicalRecords,
      appBlackoutRecords = appBlackoutRecords,
    )
  }

  val showEmptyState = combine(homeItems, isLoading) { items, loading ->
    items.isEmpty() && !loading
  }

  val isBlackoutModeEnabled = isBlackoutModeEnabledResourceConsumer.data.filterNotNull()

  val showDeletedSuccessMessage = deleteGuardResourceConsumer.data
    .filterNotNull()
    .map { getString(R.string.deleted_guard_success) }
    .asEventFlow(viewModelScope)

  val showAppBlackoutModeSuccess = enableAppBlackoutModeResourceConsumer.data
    .filterNotNull()
    .map { getString(R.string.common_success) }
    .asEventFlow(viewModelScope)

  private val _showDisableAppBlackout = MutableEventFlow<String>()
  val showDisableAppBlackout = _showDisableAppBlackout.asEventFlow()

  private val _showAppBlocked = MutableEventFlow<String>()
  val showAppBlocked = _showAppBlocked.asEventFlow()

  private val _showEditAppLaunchGuard = MutableEventFlow<String>()
  val showEditAppLaunchGuard = _showEditAppLaunchGuard.asEventFlow()

  init {
    toggleGuardEnabledResourceConsumer.data
      .filterNotNull()
      .onEach {
        _showAppBlocked.tryEmit(it)
      }.launchIn(viewModelScope)
  }

  fun onStart() {
    installedAppsResourceConsumer.collectFlow(
      installedAppInfoRepository.getInstalledApps().mapData { installedApps ->
        val walletPackages = mwaAvailabilityManager.getInstalledMwaCompatibleWalletPackages()
        installedApps.filterNot { it.packageName in walletPackages }
      }
    )
    existingGuardsResourceConsumer.collectFlow(
      appLaunchRepository.getAllAppLaunchGuards()
    )
    appBlackoutEventsResourceConsumer.collectFlow(
      appLaunchRepository.getAppBlackoutEvents(clock.now().toLocalDate())
    )
    historicalRecordsResourceConsumer.collectFlow(
      appLaunchRepository
        .getHistoricalRecordsForDate(clock.now().toLocalDate())
        .mapData { it.filterIsInstance<AppLaunchEvent>() }
    )
    isBlackoutModeEnabledResourceConsumer.collectFlow {
      val blackoutModeEvent =
        appLaunchRepository.getBlackoutModeEvent(clock.now().toLocalDate())?.takeIf { it.isEnabled }
      blackoutModeEvent != null
    }
  }

  fun onAppGuardClicked(item: HomeItem.AppLaunchGuardItem) {
    if (item.isBlackedOut) {
      _showDisableAppBlackout.tryEmit(item.packageName)
    } else {
      viewModelScope.launch {
        if (appBlockManager.shouldBlockApp(item.packageName)) {
          _showAppBlocked.tryEmit(item.packageName)
        } else {
          _showEditAppLaunchGuard.tryEmit(item.packageName)
        }
      }
    }
  }

  fun onBlackoutModeClicked() {
    viewModelScope.launch {
      if (isBlackoutModeEnabledResourceConsumer.data.value == true) {
        _showDisableBlackoutMode.call()
      } else {
        _showEnableBlackoutMode.call()
      }
    }
  }

  fun onDeleteExistingAppGuardClicked(packageName: String) {
    viewModelScope.launch {
      if (appBlockManager.shouldBlockApp(packageName)) {
        _showAppBlocked.tryEmit(packageName)
      } else {
        deleteGuardResourceConsumer.collectFlow {
          appLaunchRepository.removeAppLaunchGuard(packageName)
        }
      }
    }
  }

  fun onEnableAppBlackout(packageName: String) {
    enableAppBlackoutModeResourceConsumer.collectFlow {
      val launchGuard = checkNotNull(appLaunchRepository.getAppLaunchGuard(packageName))
      appLaunchRepository.setAppBlackoutMode(
        clock.now().toLocalDate(),
        packageName,
        launchGuard.amount,
      )
    }
  }

  fun onToggleExistingAppGuardEnabledClicked(packageName: String) {
    toggleGuardEnabledResourceConsumer.collectFlow {
      val guard = checkNotNull(appLaunchRepository.getAppLaunchGuard(packageName))
      if (!guard.isEnabled) {
        // No problem, lets enabled
        appLaunchRepository.saveAppLaunchGuard(guard.copy(isEnabled = true))
        null
      } else {
        if (appBlockManager.shouldBlockApp(packageName)) {
          packageName
        } else {
          appLaunchRepository.saveAppLaunchGuard(guard.copy(isEnabled = false))
          null
        }
      }
    }
  }
}