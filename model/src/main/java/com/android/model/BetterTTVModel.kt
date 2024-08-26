package com.android.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * */
@Serializable
data class IndivBetterTTVEmote(
    @SerialName("id")
    val id: String,
    @SerialName("code")
    val code: String,
    @SerialName("imageType")
    val imageType: String,
    @SerialName("animated")
    val animated: Boolean,
    @SerialName("userId")
    val userId: String,
    @SerialName("modifier")
    val modifier: Boolean
)

/**BELOW IS ALL THE INFORMATION FOR THE BETTERTTV CHANNEL EMOTES*/
@Serializable
data class BetterTTVChannelEmotes(
    @SerialName("id")
    val id: String = "",
    @SerialName("bots")
    val bots: List<String> = listOf(),
    @SerialName("avatar")
    val avatar: String = "",
    @SerialName("channelEmotes")
    val channelEmotes: List<BetterTTVChannelEmote> = listOf(),
    @SerialName("sharedEmotes")
    val sharedEmotes: List<BetterTTVSharedEmote> = listOf()
)

@Serializable
data class BetterTTVChannelEmote(
    @SerialName("id")
    val id: String,
    @SerialName("code")
    val code: String,
    @SerialName("imageType")
    val imageType: String,
    @SerialName("animated")
    val animated: Boolean,
    @SerialName("userId")
    val userId: String,
)

@Serializable
data class BetterTTVSharedEmote(
    @SerialName("id")
    val id: String,
    @SerialName("code")
    val code: String,
    @SerialName("imageType")
    val imageType: String,
    @SerialName("animated")
    val animated: Boolean,
    @SerialName("user")
    val user: BetterTTVSharedEmoteUser
)

@Serializable
data class BetterTTVSharedEmoteUser(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("displayName")
    val displayName: String,
    @SerialName("providerId")
    val providerId: String
)
