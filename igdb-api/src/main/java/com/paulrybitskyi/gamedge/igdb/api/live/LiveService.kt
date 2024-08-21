package com.paulrybitskyi.gamedge.igdb.api.live

import com.paulrybitskyi.gamedge.common.api.ApiResult
import com.android.model.GraphQLRequest
import com.paulrybitskyi.gamedge.igdb.api.live.model.GraphQLResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface LiveService {
    @POST("gql")
    suspend fun graphQL(@Body body: GraphQLRequest): ApiResult<GraphQLResponse>
}
