package com.dgsd.solguard.charity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dgsd.solguard.common.error.ErrorMessageFactory
import com.dgsd.solguard.common.flow.asEventFlow
import com.dgsd.solguard.common.flow.mapData
import com.dgsd.solguard.common.resource.ResourceFlowConsumer
import com.dgsd.solguard.data.AppConfigRepository
import com.dgsd.solguard.model.Charity
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapNotNull

class CharityInfoViewModel(
  application: Application,
  appConfigRepository: AppConfigRepository,
  errorMessageFactory: ErrorMessageFactory,
  private val charityId: String,
) : AndroidViewModel(application) {

  private val charityResourceConsumer = ResourceFlowConsumer<Charity>(viewModelScope)

  val isLoading = charityResourceConsumer.isLoadingOrError
  val charity = charityResourceConsumer.data.filterNotNull()

  val errorMessage = charityResourceConsumer.error.mapNotNull { error ->
    if (error == null) {
      null
    } else {
      errorMessageFactory.create(error)
    }
  }.asEventFlow(viewModelScope)

  init {
    charityResourceConsumer.collectFlow(
      appConfigRepository.getAppConfig()
        .mapData { appConfig -> appConfig.charities.single { it.id == charityId } }
    )
  }
}