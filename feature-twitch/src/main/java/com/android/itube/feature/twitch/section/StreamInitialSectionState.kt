package com.android.itube.feature.twitch.section

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.paulrybitskyi.gamedge.common.ui.widgets.ResultUiState

internal data class StreamInitialSectionState(
    val isRefreshLoading: Boolean,
    val finiteUiState: ResultUiState,
    val onClickReload: () -> Unit,
    val refresh: () -> Unit,
) : StreamContentSectionState

@Composable
internal fun rememberStreamInitialSectionState(
    isRefreshLoading: Boolean,
    finiteUiState: ResultUiState,
    onClickReload: () -> Unit,
    refresh: () -> Unit
): StreamInitialSectionState {
    return remember(
        isRefreshLoading, onClickReload, refresh,
        finiteUiState
    ) {
        StreamInitialSectionState(
            isRefreshLoading = isRefreshLoading,
            onClickReload = onClickReload,
            refresh = refresh,
            finiteUiState = finiteUiState
        )
    }
}
