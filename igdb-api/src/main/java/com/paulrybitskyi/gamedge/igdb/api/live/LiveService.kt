package com.paulrybitskyi.gamedge.igdb.api.live

import com.android.model.GraphQLRequestItem
import com.paulrybitskyi.gamedge.common.api.ApiResult
import com.paulrybitskyi.gamedge.igdb.api.live.model.GraphQLResponseItem
import retrofit2.http.Body
import retrofit2.http.POST

interface LiveService {
    @POST("gql")
    suspend fun graphQL(@Body body: List<GraphQLRequestItem>): ApiResult<List<GraphQLResponseItem>>
}
