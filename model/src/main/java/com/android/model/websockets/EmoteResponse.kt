package com.android.model.websockets

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Emote(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("images")
    val images: Images,
    @SerialName("format")
    val format: List<String>,
    @SerialName("scale")
    val scale: List<String>,
    @SerialName("theme_mode")
    val theme_mode: List<String>
)

@Serializable
data class Images(
    @SerialName("url_1x")
    val url_1x: String,
    @SerialName("url_2x")
    val url_2x: String,
    @SerialName("url_4x")
    val url_4x: String
)

@Serializable
data class EmoteData(
    @SerialName("data")
    val data: List<Emote>
)

@Serializable
data class ChannelEmoteResponse(
    @SerialName("data")
    val data: List<ChannelEmote>
)

@Serializable
data class ChannelEmote(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("images")
    val images: ChannelImages,
    @SerialName("format")
    val format: List<String>,
    @SerialName("scale")
    val scale: List<String>,
    @SerialName("theme_mode")
    val theme_mode: List<String>,
    @SerialName("emote_type")
    val emote_type: String
)

@Serializable
data class ChannelImages(
    @SerialName("url_1x")
    val url_1x: String,
    @SerialName("url_2x")
    val url_2x: String,
    @SerialName("url_4x")
    val url_4x: String
)

@Serializable
data class GlobalChatBadgesData(
    @SerialName("data")
    val data: List<GlobalBadgesSet>
)

@Serializable
data class GlobalBadgesSet(
    @SerialName("set_id")
    val set_id: String,
    @SerialName("versions")
    val versions: List<GlobalBadgesVersion> = emptyList()
)

@Serializable
data class GlobalBadgesVersion(
    @SerialName("id")
    val id: String,
    @SerialName("image_url_1x")
    val image_url_1x: String,
    @SerialName("image_url_2x")
    val image_url_2x: String,
    @SerialName("image_url_4x")
    val image_url_4x: String,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String,
    @SerialName("click_action")
    val click_action: String? = null,
    @SerialName("click_url")
    val click_url: String? = null
)
