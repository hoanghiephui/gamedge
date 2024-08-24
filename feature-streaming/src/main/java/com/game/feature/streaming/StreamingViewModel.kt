package com.game.feature.streaming

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.paulrybitskyi.gamedge.common.domain.live.entities.StreamPlaybackAccessToken
import com.paulrybitskyi.gamedge.common.ui.base.BaseViewModel
import com.paulrybitskyi.gamedge.common.ui.base.events.STREAMING_KEY
import com.paulrybitskyi.gamedge.core.sharers.TextSharer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StreamingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private var textSharer: TextSharer
) : BaseViewModel() {
    val streamPlaybackAccessToken: StreamPlaybackAccessToken =
        checkNotNull(savedStateHandle[STREAMING_KEY])

    fun shareStreaming(
        context: Context,
        content: String
    ) {
        textSharer.share(context, content)
    }
}
