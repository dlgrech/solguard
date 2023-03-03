package com.dgsd.solguard.data

import com.dgsd.solguard.common.flow.resourceFlowOf
import com.dgsd.solguard.common.resource.model.Resource
import kotlinx.coroutines.flow.Flow

class SuggestedAppsRepositoryImpl : SuggestedAppsRepository {

  override fun getSuggestedAppsToGuard(): Flow<Resource<Set<String>>> {
    // Using advanced algorithms & AI technology, come up with a suggested
    // list of app packages that we should suggest to create blocks for
    return resourceFlowOf {
      setOf(
        "com.zhiliaoapp.musically",
        "com.instagram.android",
        "com.snapchat.android",
        "com.facebook.katana",
        "com.netflix.mediaclient",
        "com.twitter.android",
        "com.google.android.youtube",
        "com.reddit.frontpage",
      )
    }
  }
}