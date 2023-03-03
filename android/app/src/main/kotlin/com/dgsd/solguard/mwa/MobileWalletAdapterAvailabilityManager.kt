package com.dgsd.solguard.mwa

import com.dgsd.solguard.common.resource.model.Resource
import com.dgsd.solguard.model.InstalledAppInfo
import kotlinx.coroutines.flow.Flow

interface MobileWalletAdapterAvailabilityManager {

  fun getInstalledMwaCompatibleWallets(): Flow<Resource<List<InstalledAppInfo>>>

  suspend fun getInstalledMwaCompatibleWalletPackages(): Set<String>

  companion object {
    const val DOWNLOAD_A_WALLET_URL = "https://play.google.com/store/search?q=solana%20wallet&c=apps"
  }
}