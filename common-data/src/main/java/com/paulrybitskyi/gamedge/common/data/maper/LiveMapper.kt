package com.paulrybitskyi.gamedge.common.data.maper

import com.android.model.GraphQLRequestItem
import com.android.model.StreamItem
import com.android.model.UserDataModel
import com.paulrybitskyi.gamedge.common.domain.live.entities.StreamPlaybackAccessToken
import com.paulrybitskyi.gamedge.core.Constants.STREAM_URL
import com.paulrybitskyi.gamedge.igdb.api.common.TwitchConstantsProvider
import com.paulrybitskyi.gamedge.igdb.api.live.model.GraphQLResponseItem
import java.net.URLEncoder
import java.time.Instant
import java.util.Random
import javax.inject.Inject

internal class LiveMapper @Inject constructor(
    private val twitchConstantsProvider: TwitchConstantsProvider
) {
    fun mapToDomainLive(
        graphQLResponse: GraphQLResponseItem,
        body: GraphQLRequestItem,
        data: StreamItem,
        userDataModel: UserDataModel?
    ): StreamPlaybackAccessToken {
        val token = graphQLResponse.data.streamPlaybackAccessToken.value
        val streamerName = body.variables.login
        val signature = graphQLResponse.data.streamPlaybackAccessToken.signature
        return StreamPlaybackAccessToken(
            token = token,
            signature = signature,
            streamerName = streamerName,
            playerType = body.variables.playerType,
            streamUrl = String.format(
                STREAM_URL,
                streamerName,
                URLEncoder.encode(token, "utf-8"),
                signature,
                Random().nextInt(6)
            ),
            title = data.title,
            startTime = Instant.parse(data.startedAt).toEpochMilli(),
            viewerCount = data.viewerCount,
            thumbnailVideo = data.largePreview,
            clientId = twitchConstantsProvider.clientId,
            broadcasterId = data.userId,//data của người live
            userId = userDataModel?.id ?: "",//data của người login app
            appLogin = userDataModel?.login ?: ""
        )
    }

}
