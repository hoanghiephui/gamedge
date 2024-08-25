package com.paulrybitskyi.gamedge.common.domain.games.usecases

import com.android.model.StreamData
import com.android.model.UserModel
import com.paulrybitskyi.gamedge.common.domain.common.DispatcherProvider
import com.paulrybitskyi.gamedge.common.domain.common.DomainResult
import com.paulrybitskyi.gamedge.common.domain.common.extensions.onEachSuccess
import com.paulrybitskyi.gamedge.common.domain.games.ObservableStreamUseCase
import com.paulrybitskyi.gamedge.common.domain.games.datastores.GamesDataStores
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

interface StreamUseCase : ObservableStreamUseCase {
    fun getUserInformation(userId: String): Flow<DomainResult<UserModel>>
    fun getMyProfile(): Flow<DomainResult<UserModel>>
}

@Singleton
internal class StreamUseCaseImpl @Inject constructor(
    private val gamesDataStores: GamesDataStores,
    private val dispatcherProvider: DispatcherProvider,
) : StreamUseCase {
    private var cursorPage: String? = null
    override fun execute(params: Boolean): Flow<DomainResult<StreamData>> {
        if (params) {// chỗ này nếu là refresh
            cursorPage = null
        }
        return flow {
            emit(gamesDataStores.streamRepository.getStreamItems(cursorPage))
        }.onEachSuccess { data ->
            cursorPage = data.cursorPage
        }.flowOn(dispatcherProvider.main)
    }

    override fun getUserInformation(userId: String): Flow<DomainResult<UserModel>> = flow {
        emit(gamesDataStores.streamRepository.getUserInformation(userId))
    }.flowOn(dispatcherProvider.main)

    override fun getMyProfile(): Flow<DomainResult<UserModel>> = flow {
        emit(gamesDataStores.streamRepository.getUserInformation())
    }.flowOn(dispatcherProvider.main)
}
