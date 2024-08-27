package com.paulrybitskyi.gamedge.common.domain.live.usecases

import androidx.compose.runtime.State
import com.android.model.IndivBetterTTVEmote
import com.android.model.Response
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteListMap
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteNameUrl
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteNameUrlEmoteTypeList
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteNameUrlList
import com.paulrybitskyi.gamedge.common.domain.chat.IndivBetterTTVEmoteList
import com.paulrybitskyi.gamedge.common.domain.common.DomainResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface TwitchEmoteUseCase {
    val emoteList: State<EmoteListMap>
    val emoteBoardGlobalList: State<EmoteNameUrlList>
    val emoteBoardChannelList: State<EmoteNameUrlEmoteTypeList>
    val globalBetterTTVEmotes: State<IndivBetterTTVEmoteList>
    val channelBetterTTVEmotes: State<IndivBetterTTVEmoteList>
    val sharedBetterTTVEmotes: State<IndivBetterTTVEmoteList>
    val globalChatBadges: State<EmoteListMap>
    val combinedEmoteList: StateFlow<ImmutableList<EmoteNameUrl>>
    val channelEmoteList: StateFlow<ImmutableList<EmoteNameUrl>>
    val globalBetterTTVEmoteList: StateFlow<ImmutableList<EmoteNameUrl>>
    val channelBetterTTVEmoteList: StateFlow<ImmutableList<EmoteNameUrl>>
    val sharedBetterTTVEmoteList: StateFlow<ImmutableList<EmoteNameUrl>>
    fun getGlobalEmotes(): Flow<Response<Boolean>>

    /**
     * getChannelEmotes a function used to make a request to the Twitch servers to access the channel specific emotes
     * @param broadcasterId a String object used to represent the id of the channel that we are requesting the emotes from
     *
     * @return a [Flow] object containing a [Response] object that is used to determine if the request was a success or not
     * */
    fun getChannelEmotes(
        broadcasterId: String
    ): Flow<Response<Boolean>>

    fun getBetterTTVGlobalEmotes(): Flow<DomainResult<List<IndivBetterTTVEmote>>>
}


