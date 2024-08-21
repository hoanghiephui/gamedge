package com.paulrybitskyi.gamedge.common.data.maper

import com.android.model.GraphQLRequestItem
import com.paulrybitskyi.gamedge.common.domain.live.entities.StreamPlaybackAccessToken
import com.paulrybitskyi.gamedge.igdb.api.live.model.GraphQLResponseItem
import javax.inject.Inject

internal class LiveMapper @Inject constructor() {
    fun mapToDomainLive(
        graphQLResponse: GraphQLResponseItem,
        body: GraphQLRequestItem
    ): StreamPlaybackAccessToken {
        return StreamPlaybackAccessToken(
            token = graphQLResponse.data.streamPlaybackAccessToken.value,
            signature = graphQLResponse.data.streamPlaybackAccessToken.signature,
            streamerName = body.variables.login,
            playerType = body.variables.playerType
        )
    }

}
