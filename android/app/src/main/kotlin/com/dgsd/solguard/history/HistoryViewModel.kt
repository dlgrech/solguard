package com.dgsd.solguard.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dgsd.solguard.common.clock.Clock
import com.dgsd.solguard.common.resource.ResourceFlowConsumer
import com.dgsd.solguard.common.viewmodel.getContext
import com.dgsd.solguard.data.AppLaunchRepository
import com.dgsd.solguard.data.InstalledAppInfoRepository
import com.dgsd.solguard.model.HistoricalRecord
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

class HistoryViewModel(
  application: Application,
  clock: Clock,
  appLaunchRepository: AppLaunchRepository,
  installedAppInfoRepository: InstalledAppInfoRepository,
) : AndroidViewModel(application) {

  private val historyRecordsResourceConsumer =
    ResourceFlowConsumer<List<HistoricalRecord>>(viewModelScope)

  val isLoading = historyRecordsResourceConsumer.isLoading

  val items = historyRecordsResourceConsumer.data.filterNotNull().map { records ->
    HistoryItemFactory.create(
      getContext(),
      clock,
      installedAppInfoRepository,
      records
    )
  }

  init {
    historyRecordsResourceConsumer.collectFlow(
      appLaunchRepository.getHistoricalRecords()
    )
  }
}