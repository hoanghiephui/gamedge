package com.paulrybitskyi.gamedge.igdb.api.stream

import com.paulrybitskyi.gamedge.common.api.ApiResult
import com.paulrybitskyi.gamedge.igdb.api.stream.model.StreamsResponse

interface StreamEndpoint {
    suspend fun getStreamItems(cursorPage: String?): ApiResult<StreamsResponse>
}

internal class StreamEndpointImpl(
    private val streamService: StreamService,
) : StreamEndpoint {

    override suspend fun getStreamItems(cursorPage: String?): ApiResult<StreamsResponse> {
        val queryParams = buildMap {
            put("first", "20")
            if (cursorPage != null) {
                put("after", cursorPage)
            }
        }
        return streamService.getStreamItems(
            queryParams = queryParams
        )
    }
}
