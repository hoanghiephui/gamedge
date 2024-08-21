package com.paulrybitskyi.gamedge.igdb.api.live

import com.paulrybitskyi.gamedge.common.api.ApiResult
import com.android.model.GraphQLRequestItem
import com.paulrybitskyi.gamedge.igdb.api.live.model.GraphQLResponseItem

interface LiveEndpoint {
    suspend fun graphQL(body: GraphQLRequestItem): ApiResult<List<GraphQLResponseItem>>
}

internal class LiveEndpointImpl(
    private val liveService: LiveService,
) : LiveEndpoint {

    override suspend fun graphQL(body: GraphQLRequestItem): ApiResult<List<GraphQLResponseItem>> {
        return liveService.graphQL(listOf(body))
    }
}
