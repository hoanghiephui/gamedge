package com.android.itube.feature.twitch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.android.itube.feature.twitch.section.StreamContentSectionState
import com.android.itube.feature.twitch.section.rememberStreamInitialSectionState
import com.android.itube.feature.twitch.section.rememberStreamLoadedSectionState
import com.android.itube.feature.twitch.section.rememberStreamLoadingSectionState
import com.android.itube.feature.twitch.state.finiteUiState
import com.paulrybitskyi.gamedge.common.ui.widgets.ResultUiState

data class StreamPageState(
    val contentSectionState: StreamContentSectionState,
    val onFinish: () -> Unit,
)

@Composable
fun rememberStreamPageState(
    pageViewModel: TwitchViewModel,
    navigation: NavHostController,
): StreamPageState {
    val uiState by pageViewModel.uiState.collectAsStateWithLifecycle()
    val isInitialLoading = pageViewModel.isInitialLoading
    val isRefreshLoading = pageViewModel.isRefreshLoading
    val contentSectionState = when {
        isInitialLoading -> rememberStreamLoadingSectionState()
        uiState.finiteUiState == ResultUiState.Success -> rememberStreamLoadedSectionState(
            isRefreshLoading = isRefreshLoading,
            refresh = {},
            navigation = navigation,
            uiState = uiState
        )

        else -> rememberStreamInitialSectionState(
            isRefreshLoading = isRefreshLoading,
            finiteUiState = uiState.finiteUiState,
            onClickReload = pageViewModel::initialLoadIfNeeded,
            refresh = {}
        )
    }
    return remember(
        contentSectionState,
        navigation
    ) {
        StreamPageState(
            contentSectionState = contentSectionState,
            onFinish = {

            }
        )
    }
}
