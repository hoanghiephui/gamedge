package com.paulrybitskyi.gamedge.common.domain.live

import com.android.model.BetterTTVChannelEmotes
import com.android.model.IndivBetterTTVEmote
import com.paulrybitskyi.gamedge.common.domain.common.DomainResult

interface TvEmoteRepository {
    suspend fun getGlobalEmotes(): DomainResult<List<IndivBetterTTVEmote>>
    suspend fun getChannelEmotes(broadcasterId: String): DomainResult<BetterTTVChannelEmotes>
}
