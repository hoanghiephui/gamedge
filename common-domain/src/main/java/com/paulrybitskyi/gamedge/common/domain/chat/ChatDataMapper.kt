package com.paulrybitskyi.gamedge.common.domain.chat

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Immutable
import com.android.model.IndivBetterTTVEmote
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * EmoteNameUrl represents a single Twitch Emote from the Twitch servers. Each instance of this class is a unique Emote
 *
 * @param name the name of the Twitch emote
 * @param url the url that is hosted on the twitch servers and is what we use to load the image
 * */
data class EmoteNameUrl(
    val id: String,
    val name: String,
    val url: String,
)

/**
 * EmoteNameEmoteType represents a single Twitch Emote from the Twitch servers, when calling get channel emotes
 * - you can read more about getting channel emotes, [HERE](https://dev.twitch.tv/docs/api/reference/#get-channel-emotes)
 *
 * @param name the name of the Twitch emote
 * @param url the url that is hosted on the twitch servers and is what we use to load the image,
 * @param emoteType a [EmoteTypes] used to represent the type of emote that it is
 * */
data class EmoteNameUrlEmoteType(
    val id: String,
    val name: String,
    val url: String,
    val emoteType: EmoteTypes
)

/**
 * EmoteTypes represents the two types of emotes, subscribers and followers
 * */
enum class EmoteTypes {
    SUBS, FOLLOWERS,
}

@Immutable
data class EmoteNameUrlList(
    val list: ImmutableList<EmoteNameUrl> = persistentListOf()
)

@Immutable
data class EmoteListMap(
    val map: Map<String, InlineTextContent>
)

@Immutable
data class EmoteNameUrlEmoteTypeList(
    val list: ImmutableList<EmoteNameUrlEmoteType> = persistentListOf()
)

/**
 * EmoteNameUrlNumberList
 * */
@Immutable
data class EmoteNameUrlNumberList(
    val list: ImmutableList<EmoteNameUrlNumber> = persistentListOf()
)

/**
 * class to show the list of individual channel emotes of BetterTTV
 * */
@Immutable
data class IndivBetterTTVEmoteList(
    val list: ImmutableList<IndivBetterTTVEmote> = persistentListOf()
)

/**
 * EmoteNameUrlNumber represents a single Twitch Emote from the Twitch servers and the number of times it was clicked.
 * This data class is used soley for the purpose of the most frequently clicked emotes
 *
 * @param name the name of the Twitch emote
 * @param url the url that is hosted on the twitch servers and is what we use to load the image
 * @param timesClicked the number of times this emote was clicked inside of the Twitch emote board
 * */
data class EmoteNameUrlNumber(
    val name: String,
    val url: String,
    val timesClicked: Int
)
