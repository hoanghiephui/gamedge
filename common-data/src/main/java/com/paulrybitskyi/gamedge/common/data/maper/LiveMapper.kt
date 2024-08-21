package com.paulrybitskyi.gamedge.common.data.maper

import com.android.model.GraphQLRequest
import com.paulrybitskyi.gamedge.common.domain.live.entities.StreamPlaybackAccessToken
import com.paulrybitskyi.gamedge.igdb.api.live.model.GraphQLResponse
import javax.inject.Inject

internal class LiveMapper @Inject constructor() {
    fun mapToDomainLive(
        graphQLResponse: GraphQLResponse,
        body: GraphQLRequest
    ): StreamPlaybackAccessToken {
        return StreamPlaybackAccessToken(
            token = graphQLResponse.graphQLResponse.first().data.streamPlaybackAccessToken.value,
            signature = graphQLResponse.graphQLResponse.first().data.streamPlaybackAccessToken.signature,
            streamerName = body.graphQLRequest.first().variables.login,
            playerType = body.graphQLRequest.first().variables.playerType
        )
    }

}
