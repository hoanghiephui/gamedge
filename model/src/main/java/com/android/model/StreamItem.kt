package com.android.model

data class StreamItem(
    val userName: String,
    val language: String,
    val isMature: Boolean,
    val type: String? = null,
    val title: String,
    val thumbnailUrl: List<String>,
    val tags: List<String>,
    val gameName: String,
    val userId: String,
    val userLogin: String,
    val startedAt: String,
    val id: String,
    val viewerCount: Long,
    val gameId: String
) {
    val isLive: Boolean get() =
        "live" == type
}

data class StreamData(
    val cursorPage: String,
    val items: List<StreamItem>
)
