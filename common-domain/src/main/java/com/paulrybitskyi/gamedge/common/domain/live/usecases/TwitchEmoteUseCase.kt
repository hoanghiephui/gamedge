package com.paulrybitskyi.gamedge.common.domain.live.usecases

import androidx.compose.runtime.State
import com.android.model.Response
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteListMap
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteNameUrl
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteNameUrlEmoteTypeList
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteNameUrlList
import com.paulrybitskyi.gamedge.common.domain.chat.IndivBetterTTVEmoteList
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
    val combinedEmoteList: StateFlow<List<EmoteNameUrl>>
    val channelEmoteList: StateFlow<List<EmoteNameUrl>>
    val globalBetterTTVEmoteList: StateFlow<List<EmoteNameUrl>>
    val channelBetterTTVEmoteList: StateFlow<List<EmoteNameUrl>>
    val sharedBetterTTVEmoteList: StateFlow<List<EmoteNameUrl>>
    fun getGlobalEmotes(): Flow<Response<Boolean>>
}


