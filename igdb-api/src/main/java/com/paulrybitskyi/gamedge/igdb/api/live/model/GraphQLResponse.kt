package com.paulrybitskyi.gamedge.igdb.api.live.model

import com.paulrybitskyi.gamedge.igdb.apicalypse.serialization.annotations.Apicalypse
import com.paulrybitskyi.gamedge.igdb.apicalypse.serialization.annotations.ApicalypseClass
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@ApicalypseClass
@Serializable
data class GraphQLResponseItem(

    @Apicalypse("extensions")
    @SerialName("extensions")
    val extensions: Extensions,

    @Apicalypse("data")
    @SerialName("data")
    val data: Data
)

@ApicalypseClass
@Serializable
data class StreamPlaybackAccessToken(

    @Apicalypse("signature")
    @SerialName("signature")
    val signature: String,

    @Apicalypse("__typename")
    @SerialName("__typename")
    val typename: String,

    @Apicalypse("value")
    @SerialName("value")
    val value: String
)

@ApicalypseClass
@Serializable
data class Data(

    @Apicalypse("streamPlaybackAccessToken")
    @SerialName("streamPlaybackAccessToken")
    val streamPlaybackAccessToken: StreamPlaybackAccessToken
)

@ApicalypseClass
@Serializable
data class Extensions(

    @Apicalypse("durationMilliseconds")
    @SerialName("durationMilliseconds")
    val durationMilliseconds: Int,

    @Apicalypse("requestID")
    @SerialName("requestID")
    val requestID: String,

    @Apicalypse("operationName")
    @SerialName("operationName")
    val operationName: String
)
