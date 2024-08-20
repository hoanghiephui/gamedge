package com.paulrybitskyi.gamedge.common.domain.games.entities

data class StreamItem(
    val userName: String,
    val language: String,
    val isMature: Boolean,
    val type: String,
    val title: String,
    val thumbnailUrl: String,
    val tags: List<String>,
    val gameName: String,
    val userId: String,
    val userLogin: String,
    val startedAt: String,
    val id: String,
    val viewerCount: Int,
    val gameId: String
)

data class StreamData(
    val cursorPage: String,
    val items: List<StreamItem>
)
