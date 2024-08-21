package com.android.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class GraphQLRequest(
    @SerialName("GraphQLRequest")
    val graphQLRequest: List<GraphQLRequestItem>
)

@Serializable
data class PersistedQuery(
    @SerialName("sha256Hash")
    val sha256Hash: String,

    @SerialName("version")
    val version: Int
)

@Serializable
data class GraphQLRequestItem(
    @SerialName("variables")
    val variables: Variables,

    @SerialName("extensions")
    val extensions: ExtensionsRequest,

    @SerialName("operationName")
    val operationName: String
)

@Serializable
data class Variables(
    @SerialName("isLive")
    val isLive: Boolean,

    @SerialName("vodID")
    val vodID: String,

    @SerialName("playerType")
    val playerType: String,

    @SerialName("isVod")
    val isVod: Boolean,

    @SerialName("login")
    val login: String
)

@Serializable
data class ExtensionsRequest(
    @SerialName("persistedQuery")
    val persistedQuery: PersistedQuery
)
