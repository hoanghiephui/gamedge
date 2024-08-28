package com.paulrybitskyi.gamedge.common.domain.repository

import com.android.model.StreamData
import com.android.model.UserModel
import com.android.model.websockets.ChannelEmoteResponse
import com.android.model.websockets.ChatBadgePair
import com.android.model.websockets.ChatSettingsData
import com.android.model.websockets.EmoteData
import com.paulrybitskyi.gamedge.common.domain.common.DomainResult

interface StreamRepository {
    suspend fun getStreamItems(cursorPage: String?): DomainResult<StreamData>
    suspend fun getUserInformation(userId: String? = null): DomainResult<UserModel>
    suspend fun getChatSettings(broadcasterId: String): DomainResult<List<ChatSettingsData>>
    suspend fun getGlobalEmotes(): DomainResult<EmoteData>
    suspend fun getChannelEmotes(broadcasterId: String): DomainResult<ChannelEmoteResponse>
    suspend fun getGlobalChatBadges(): DomainResult<List<ChatBadgePair>>
}
