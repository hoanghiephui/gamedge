package com.paulrybitskyi.gamedge.common.domain.live.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StreamPlaybackAccessToken(
    val token: String,
    val signature: String,
    val streamerName: String,
    val playerType: String,
    val streamUrl: String,
    val title: String,
    val startTime: Long,
    val viewerCount: Long,
    val thumbnailVideo: String
) : Parcelable
