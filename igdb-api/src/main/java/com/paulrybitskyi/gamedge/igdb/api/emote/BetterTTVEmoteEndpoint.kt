package com.paulrybitskyi.gamedge.igdb.api.emote

import com.android.model.BetterTTVChannelEmotes
import com.android.model.IndivBetterTTVEmote
import com.paulrybitskyi.gamedge.common.api.ApiResult

interface BetterTTVEmoteEndpoint {
    suspend fun getGlobalEmotes(): ApiResult<List<IndivBetterTTVEmote>>
    suspend fun getChannelEmotes(broadcasterId: String): ApiResult<BetterTTVChannelEmotes>
}

internal class BetterTTVEmoteEndpointImpl(
    private val betterTTVEmoteService: BetterTTVEmoteService
) : BetterTTVEmoteEndpoint {

    override suspend fun getGlobalEmotes(): ApiResult<List<IndivBetterTTVEmote>> {
        return betterTTVEmoteService.getGlobalEmotes()
    }

    override suspend fun getChannelEmotes(broadcasterId: String): ApiResult<BetterTTVChannelEmotes> {
        return betterTTVEmoteService.getChannelEmotes(broadcasterId)
    }
}
