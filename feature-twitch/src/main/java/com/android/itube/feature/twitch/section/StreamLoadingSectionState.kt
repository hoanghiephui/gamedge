package com.android.itube.feature.twitch.section

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

data object StreamLoadingSectionState: StreamContentSectionState

@Composable
fun rememberStreamLoadingSectionState(): StreamLoadingSectionState {
    return remember {
        StreamLoadingSectionState
    }
}
