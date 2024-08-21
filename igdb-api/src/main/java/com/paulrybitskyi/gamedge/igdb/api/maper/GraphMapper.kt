package com.paulrybitskyi.gamedge.igdb.api.maper

import com.android.model.ExtensionsRequest
import com.android.model.GraphQLRequestItem
import com.android.model.PersistedQuery
import com.android.model.StreamItem
import com.android.model.Variables
import com.paulrybitskyi.gamedge.igdb.api.common.TwitchConstantsProvider
import javax.inject.Inject

class GraphMapper @Inject constructor(
    private val twitchConstantsProvider: TwitchConstantsProvider
) {
    fun mapToGraphQLRequest(
        data: StreamItem
    ): GraphQLRequestItem {
        return GraphQLRequestItem(
            variables = Variables(
                isLive = data.isLive,
                vodID = "",
                playerType = "embed",
                isVod = !data.isLive,
                login = data.userLogin
            ),
            extensions = ExtensionsRequest(
                persistedQuery = PersistedQuery(
                    sha256Hash = twitchConstantsProvider.twitchHashVideo,
                    version = 1
                )
            ),
            operationName = "PlaybackAccessToken"
        )
    }
}
