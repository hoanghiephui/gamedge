package com.paulrybitskyi.gamedge.common.domain.live.usecases

import com.android.model.GraphQLRequestItem
import com.android.model.StreamItem
import com.paulrybitskyi.gamedge.common.domain.common.DispatcherProvider
import com.paulrybitskyi.gamedge.common.domain.common.DomainResult
import com.paulrybitskyi.gamedge.common.domain.games.datastores.GamesDataStores
import com.paulrybitskyi.gamedge.common.domain.live.entities.StreamPlaybackAccessToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

interface LiveUseCase {
    fun getGraphQL(
        body: GraphQLRequestItem,
        data: StreamItem
    ): Flow<DomainResult<StreamPlaybackAccessToken>>
}

@Singleton
internal class LiveUseCaseImpl @Inject constructor(
    private val gamesDataStores: GamesDataStores,
    private val dispatcherProvider: DispatcherProvider,
) : LiveUseCase {
    override fun getGraphQL(
        body: GraphQLRequestItem,
        data: StreamItem
    ): Flow<DomainResult<StreamPlaybackAccessToken>> {
        return flow {
            emit(gamesDataStores.liveRepository.getGraphQL(body, data))
        }.flowOn(dispatcherProvider.main)
    }
}
