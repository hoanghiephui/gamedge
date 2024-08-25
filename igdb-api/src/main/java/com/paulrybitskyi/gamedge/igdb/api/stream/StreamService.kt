package com.paulrybitskyi.gamedge.igdb.api.stream

import com.paulrybitskyi.gamedge.common.api.ApiResult
import com.paulrybitskyi.gamedge.igdb.api.stream.model.StreamsResponse
import com.paulrybitskyi.gamedge.igdb.api.stream.model.UserDataResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface StreamService {
    @GET("streams")
    suspend fun getStreamItems(
        @QueryMap queryParams: Map<String, String>,
    ): ApiResult<StreamsResponse>

    @GET("users")
    suspend fun getUserInformation(
        @QueryMap queryParams: Map<String, String>
    ): ApiResult<UserDataResponse>
}
