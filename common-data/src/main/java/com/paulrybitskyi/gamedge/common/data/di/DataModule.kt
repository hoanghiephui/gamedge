package com.paulrybitskyi.gamedge.common.data.di

import com.paulrybitskyi.gamedge.common.data.common.ConnectivityManagerNetworkMonitor
import com.paulrybitskyi.gamedge.common.data.common.NetworkMonitor
import com.paulrybitskyi.gamedge.common.data.repository.OfflineFirstUserDataRepository
import com.paulrybitskyi.gamedge.common.data.repository.UserDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    internal abstract fun bindsUserDataRepository(
        userDataRepository: OfflineFirstUserDataRepository,
    ): UserDataRepository

    @Binds
    internal abstract fun bindsNetworkMonitor(
        networkMonitor: ConnectivityManagerNetworkMonitor,
    ): NetworkMonitor
}
