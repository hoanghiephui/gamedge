package com.paulrybitskyi.gamedge.igdb.api.stream.model

import android.os.Parcelable
import com.paulrybitskyi.gamedge.igdb.apicalypse.serialization.annotations.Apicalypse
import com.paulrybitskyi.gamedge.igdb.apicalypse.serialization.annotations.ApicalypseClass
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@ApicalypseClass
@Serializable
data class StreamsResponse(

    @Apicalypse("pagination")
    @SerialName("pagination")
    val pagination: Pagination,

    @Apicalypse("data")
    @SerialName("data")
    val data: List<DataItem>
) : Parcelable

@Parcelize
@ApicalypseClass
@Serializable
data class Pagination(

    @Apicalypse("cursor")
    @SerialName("cursor")
    val cursor: String
) : Parcelable

@Parcelize
@ApicalypseClass
@Serializable
data class DataItem(

    @Apicalypse("user_name")
    @SerialName("user_name")
    val userName: String,

    @Apicalypse("language")
    @SerialName("language")
    val language: String,

    @Apicalypse("is_mature")
    @SerialName("is_mature")
    val isMature: Boolean,

    @Apicalypse("type")
    @SerialName("type")
    val type: String? = null,

    @Apicalypse("title")
    @SerialName("title")
    val title: String,

    @SerialName("thumbnail_url")
    @Apicalypse("thumbnail_url")
    val thumbnailUrl: String,

    @SerialName("tags")
    @Apicalypse("tags")
    val tags: List<String> = emptyList(),

    @SerialName("game_name")
    @Apicalypse("game_name")
    val gameName: String,

    @SerialName("user_id")
    @Apicalypse("user_id")
    val userId: String,

    @SerialName("user_login")
    @Apicalypse("user_login")
    val userLogin: String,

    @Apicalypse("started_at")
    @SerialName("started_at")
    val startedAt: String,

    @Apicalypse("id")
    @SerialName("id")
    val id: String,

    @Apicalypse("viewer_count")
    @SerialName("viewer_count")
    val viewerCount: Long,

    @Apicalypse("game_id")
    @SerialName("game_id")
    val gameId: String
) : Parcelable
