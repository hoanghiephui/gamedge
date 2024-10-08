package com.paulrybitskyi.gamedge.common.data.repository

import com.android.model.GraphQLRequestItem
import com.android.model.StreamItem
import com.github.michaelbull.result.mapEither
import com.paulrybitskyi.gamedge.common.api.ApiResult
import com.paulrybitskyi.gamedge.common.data.common.ApiErrorMapper
import com.paulrybitskyi.gamedge.common.data.maper.LiveMapper
import com.paulrybitskyi.gamedge.common.domain.auth.datastores.AuthLocalDataStore
import com.paulrybitskyi.gamedge.common.domain.common.DispatcherProvider
import com.paulrybitskyi.gamedge.common.domain.common.DomainResult
import com.paulrybitskyi.gamedge.common.domain.live.LiveRepository
import com.paulrybitskyi.gamedge.common.domain.live.entities.StreamPlaybackAccessToken
import com.paulrybitskyi.gamedge.igdb.api.live.LiveEndpoint
import com.paulrybitskyi.gamedge.igdb.api.live.model.GraphQLResponseItem
import com.paulrybitskyi.hiltbinder.BindType
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@BindType
internal class LiveRepositoryImpl @Inject constructor(
    private val liveEndpoint: LiveEndpoint,
    private val dispatcherProvider: DispatcherProvider,
    private val apiErrorMapper: ApiErrorMapper,
    private val liveMapper: LiveMapper,
    private val authLocalDataStore: AuthLocalDataStore,
) : LiveRepository {
    override suspend fun getGraphQL(body: GraphQLRequestItem, data: StreamItem): DomainResult<StreamPlaybackAccessToken> {
        return liveEndpoint.graphQL(body).toDataResult(body, data)
    }

    private suspend fun ApiResult<List<GraphQLResponseItem>>.toDataResult(body: GraphQLRequestItem, data: StreamItem): DomainResult<StreamPlaybackAccessToken> {
        return withContext(dispatcherProvider.io) {
            val userDataModel = authLocalDataStore.getMyProfile()
            mapEither(
                { response -> liveMapper.mapToDomainLive(response.first(), body, data, userDataModel) },
                apiErrorMapper::mapToDomainError
            )
        }
    }
}
