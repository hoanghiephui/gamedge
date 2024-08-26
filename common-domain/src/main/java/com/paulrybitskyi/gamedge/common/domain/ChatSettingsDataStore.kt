package com.paulrybitskyi.gamedge.common.domain

import kotlinx.coroutines.flow.Flow

interface ChatSettingsDataStore {
    suspend fun setBadgeSize(badgeSize: Float)

    fun getBadgeSize(): Flow<Float>

    suspend fun setUsernameSize(usernameSize: Float)

    fun getUsernameSize(): Flow<Float>

    suspend fun setMessageSize(messageSize: Float)

    fun getMessageSize(): Flow<Float>

    suspend fun setEmoteSize(emoteSize: Float)

    fun getEmoteSize(): Flow<Float>

    suspend fun setLineHeight(lineHeight: Float)

    fun getLineHeight(): Flow<Float>

    suspend fun setCustomUsernameColor(showCustomUsernameColor: Boolean)

    fun getCustomUsernameColor(): Flow<Boolean>
}
