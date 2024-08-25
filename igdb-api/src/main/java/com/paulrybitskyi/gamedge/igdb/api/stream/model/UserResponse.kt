package com.paulrybitskyi.gamedge.igdb.api.stream.model

import com.paulrybitskyi.gamedge.igdb.apicalypse.serialization.annotations.Apicalypse
import com.paulrybitskyi.gamedge.igdb.apicalypse.serialization.annotations.ApicalypseClass
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@ApicalypseClass
@Serializable
data class UserDataResponse(
    @Apicalypse("data")
    @SerialName("data")
    val data: List<UserData>
)

@ApicalypseClass
@Serializable
data class UserData(
    @Apicalypse("id")
    @SerialName("id")
    val id: String,
    @Apicalypse("login")
    @SerialName("login")
    val login: String,
    @Apicalypse("display_name")
    @SerialName("display_name")
    val display_name: String,
    @Apicalypse("type")
    @SerialName("type")
    val type: String,
    @Apicalypse("broadcaster_type")
    @SerialName("broadcaster_type")
    val broadcaster_type: String,
    @Apicalypse("description")
    @SerialName("description")
    val description: String,
    @Apicalypse("profile_image_url")
    @SerialName("profile_image_url")
    val profile_image_url: String,
    @Apicalypse("offline_image_url")
    @SerialName("offline_image_url")
    val offline_image_url: String,
    @Apicalypse("view_count")
    @SerialName("view_count")
    val view_count: Int,
    @Apicalypse("created_at")
    @SerialName("created_at")
    val created_at: String
)
