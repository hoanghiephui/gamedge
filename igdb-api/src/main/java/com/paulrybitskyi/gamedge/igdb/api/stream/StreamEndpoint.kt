package com.paulrybitskyi.gamedge.igdb.api.stream

import com.android.model.websockets.ChatSettings
import com.paulrybitskyi.gamedge.common.api.ApiResult
import com.android.model.websockets.ChannelEmoteResponse
import com.android.model.websockets.EmoteData
import com.android.model.websockets.GlobalChatBadgesData
import com.paulrybitskyi.gamedge.igdb.api.stream.model.StreamsResponse
import com.paulrybitskyi.gamedge.igdb.api.stream.model.UserDataResponse

interface StreamEndpoint {
    suspend fun getStreamItems(cursorPage: String?): ApiResult<StreamsResponse>
    suspend fun getUserInformation(
        userId: String?
    ): ApiResult<UserDataResponse>

    suspend fun getChatSettings(
        broadcasterId: String
    ): ApiResult<ChatSettings>

    suspend fun getGlobalEmotes(): ApiResult<EmoteData>
    suspend fun getChannelEmotes(broadcasterId: String): ApiResult<ChannelEmoteResponse>
    suspend fun getGlobalChatBadges(): ApiResult<GlobalChatBadgesData>
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

    override suspend fun getUserInformation(userId: String?): ApiResult<UserDataResponse> {
        val queryParams = buildMap {
            if (userId != null) {
                put("id", userId)
            }
        }
        return streamService.getUserInformation(queryParams)
    }

    override suspend fun getChatSettings(broadcasterId: String): ApiResult<ChatSettings> {
        return streamService.getChatSettings(broadcasterId)
    }

    override suspend fun getGlobalEmotes(): ApiResult<EmoteData> {
        return streamService.getGlobalEmotes()
    }

    override suspend fun getChannelEmotes(broadcasterId: String): ApiResult<ChannelEmoteResponse> {
        return streamService.getChannelEmotes(broadcasterId)
    }

    override suspend fun getGlobalChatBadges(): ApiResult<GlobalChatBadgesData> {
        return streamService.getGlobalChatBadges()
    }
}
