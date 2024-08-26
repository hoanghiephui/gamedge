package com.paulrybitskyi.gamedge.igdb.api.stream

import com.android.model.websockets.ChatSettings
import com.paulrybitskyi.gamedge.common.api.ApiResult
import com.android.model.websockets.ChannelEmoteResponse
import com.android.model.websockets.EmoteData
import com.android.model.websockets.GlobalChatBadgesData
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

    /**
     * - getChatSettings represents a GET method. A function meant to get the chat settings of the stream currently views
     *
     * @param broadcasterId a String used to represent the unique identifier of the streamer being currently viewed
     * */
    @GET("chat/settings")
    suspend fun getChatSettings(
        @Query("broadcaster_id") broadcasterId: String
    ): ApiResult<ChatSettings>

    /**
     * - represented as a GET method. This function is used to get the available Global Twitch Emotes
     * */
    @GET("chat/emotes/global")
    suspend fun getGlobalEmotes(): ApiResult<EmoteData>

    @GET("chat/emotes")
    suspend fun getChannelEmotes(
        @Query("broadcaster_id") broadcasterId: String
    ): ApiResult<ChannelEmoteResponse>

    /**
     * getGlobalChatBadges is a function meant to get the all the global chat badges that Twitch has available. You can read more about
     * getting global chat badges [HERE](https://dev.twitch.tv/docs/api/reference/#get-global-chat-badges)
     * */
    @GET("chat/badges/global")
    suspend fun getGlobalChatBadges(): ApiResult<GlobalChatBadgesData>
}
