package com.paulrybitskyi.gamedge.igdb.api.stream

import com.paulrybitskyi.gamedge.common.api.ApiResult
import com.paulrybitskyi.gamedge.igdb.api.stream.model.StreamsResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface StreamService {
    @GET("helix/streams")
    suspend fun getStreamItems(
        @QueryMap queryParams: Map<String, String>,
    ): ApiResult<StreamsResponse>
}
