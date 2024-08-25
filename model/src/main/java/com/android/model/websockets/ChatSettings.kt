package com.android.model.websockets

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class ChatSettings(
    val data: List<ChatSettingsData>
)

/**
 * Represent the data that the Switches in [ChatSettingsContainer][com.example.clicker.presentation.stream.views.ChatSettingsContainer]
 * are manipulating
 * */
@Serializable
data class ChatSettingsData(
    @SerialName("slow_mode")
    val slowMode: Boolean,
    @SerialName("slow_mode_wait_time")
    val slowModeWaitTime: Int?,
    @SerialName("follower_mode")
    val followerMode: Boolean, //
    @SerialName("follower_mode_duration")
    val followerModeDuration: Int?, //
    @SerialName("subscriber_mode")
    val subscriberMode: Boolean,
    @SerialName("emote_mode") //
    val emoteMode: Boolean,
//    @SerialName("unique_chat_mode")
//    val uniqueChatMode: Boolean
)

/**
 * Represent the data that is sent as the body to the endpoint: PATCH https://api.twitch.tv/helix/chat/settings
 * - Read more in the documentation, [HERE](https://dev.twitch.tv/docs/api/reference/#update-chat-settings)
 * */
data class UpdateChatSettings(
    val emote_mode: Boolean,
    val follower_mode: Boolean,
    val slow_mode: Boolean,
    val subscriber_mode: Boolean
)

