package com.paulrybitskyi.gamedge.igdb.api.live

import com.paulrybitskyi.gamedge.common.api.ApiResult
import com.android.model.GraphQLRequest
import com.paulrybitskyi.gamedge.igdb.api.live.model.GraphQLResponse

interface LiveEndpoint {
    suspend fun graphQL(body: GraphQLRequest): ApiResult<GraphQLResponse>
}

internal class LiveEndpointImpl(
    private val liveService: LiveService,
) : LiveEndpoint {

    override suspend fun graphQL(body: GraphQLRequest): ApiResult<GraphQLResponse> {
        return liveService.graphQL(body)
    }
}
