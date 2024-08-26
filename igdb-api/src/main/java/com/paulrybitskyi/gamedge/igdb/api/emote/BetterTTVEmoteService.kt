package com.paulrybitskyi.gamedge.igdb.api.emote

import com.android.model.BetterTTVChannelEmotes
import com.android.model.IndivBetterTTVEmote
import com.paulrybitskyi.gamedge.common.api.ApiResult
import retrofit2.http.GET
import retrofit2.http.Path

interface BetterTTVEmoteService {
    @GET("cached/emotes/global")
    suspend fun getGlobalEmotes(): ApiResult<List<IndivBetterTTVEmote>>


    @GET("cached/users/twitch/{broadcasterId}")
    suspend fun getChannelEmotes(
        @Path("broadcasterId") broadcasterId: String
    ): ApiResult<BetterTTVChannelEmotes>
}
