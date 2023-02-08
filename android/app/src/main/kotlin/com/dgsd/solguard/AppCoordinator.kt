package com.dgsd.solguard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.dgsd.solguard.common.flow.MutableEventFlow
import com.dgsd.solguard.common.flow.asEventFlow

class AppCoordinator(
  application: Application,
) : AndroidViewModel(application) {

  sealed interface Destination {

    sealed interface InlineDestination : Destination {
      object Onboarding: InlineDestination
    }

    sealed interface BottomSheetDestination : Destination {

    }
  }

  private val _destination = MutableEventFlow<Destination>()
  val destination = _destination.asEventFlow()

  fun onCreate() {
    _destination.tryEmit(Destination.InlineDestination.Onboarding)
  }
}
