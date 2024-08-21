package com.paulrybitskyi.gamedge.common.data.repository

import com.android.model.GraphQLRequestItem
import com.github.michaelbull.result.mapEither
import com.paulrybitskyi.gamedge.common.api.ApiResult
import com.paulrybitskyi.gamedge.common.data.common.ApiErrorMapper
import com.paulrybitskyi.gamedge.common.data.maper.LiveMapper
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
    private val liveMapper: LiveMapper
) : LiveRepository {
    override suspend fun getGraphQL(body: GraphQLRequestItem): DomainResult<StreamPlaybackAccessToken> {
        return liveEndpoint.graphQL(body).toDataResult(body)
    }

    private suspend fun ApiResult<List<GraphQLResponseItem>>.toDataResult(body: GraphQLRequestItem): DomainResult<StreamPlaybackAccessToken> {
        return withContext(dispatcherProvider.io) {
            mapEither(
                { response -> liveMapper.mapToDomainLive(response.first(), body) },
                apiErrorMapper::mapToDomainError
            )
        }
    }
}
