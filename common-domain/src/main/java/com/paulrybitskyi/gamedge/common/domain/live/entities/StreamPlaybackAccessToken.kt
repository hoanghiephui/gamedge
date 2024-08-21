package com.paulrybitskyi.gamedge.common.domain.live.entities

data class StreamPlaybackAccessToken(
    val token: String,
    val signature: String,
    val streamerName: String,
    val playerType: String
)
