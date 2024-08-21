package com.paulrybitskyi.gamedge.common.domain.live

import com.android.model.GraphQLRequestItem
import com.paulrybitskyi.gamedge.common.domain.common.DomainResult
import com.paulrybitskyi.gamedge.common.domain.live.entities.StreamPlaybackAccessToken

interface LiveRepository {
    suspend fun getGraphQL(body: GraphQLRequestItem): DomainResult<StreamPlaybackAccessToken>
}
