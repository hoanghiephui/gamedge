package com.android.itube.feature.twitch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    val isFetchLiveStreamURLLoading = pageViewModel.isFetchLiveStreamURLLoading
    val isRefreshLoading = pageViewModel.isRefreshLoading
    val contentSectionState = when (uiState.finiteUiState) {
        ResultUiState.Loading -> rememberStreamLoadingSectionState()
        ResultUiState.Success -> rememberStreamLoadedSectionState(
            isRefreshLoading = isRefreshLoading,
            refresh = {
                pageViewModel.refreshGames(true)
            },
            navigation = navigation,
            uiState = uiState,
            onBottomReached = { pageViewModel.onBottomReached() },
            onClickVideo = { items, index ->
                pageViewModel.getLiveStreamURL(items[index])
            },
            isFetchLiveStreamURLLoading = isFetchLiveStreamURLLoading
        )

        else -> rememberStreamInitialSectionState(
            isRefreshLoading = isRefreshLoading,
            finiteUiState = uiState.finiteUiState,
            onClickReload = pageViewModel::initialLoadIfNeeded,
            refresh = {
                pageViewModel.refreshGames(true)
            }
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
