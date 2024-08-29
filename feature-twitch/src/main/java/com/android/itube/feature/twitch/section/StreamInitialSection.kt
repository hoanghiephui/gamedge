package com.android.itube.feature.twitch.section

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.paulrybitskyi.gamedge.common.ui.widgets.NetworkError
import com.paulrybitskyi.gamedge.common.ui.widgets.PullRefresh
import com.paulrybitskyi.gamedge.common.ui.widgets.ResultUiState

@Composable
internal fun StreamInitialSection(
    sectionState: StreamInitialSectionState,
) {
    PullRefresh(
        modifier = Modifier,
        isRefreshing = sectionState.isRefreshLoading,
        onRefresh = sectionState.refresh,
    ) {
        if (sectionState.finiteUiState == ResultUiState.Error) {
            NetworkError(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                onClickReload = sectionState.onClickReload,
            )
        }
    }
}
