package com.android.itube.feature.twitch.section

import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.android.itube.feature.twitch.state.StreamUiState
import com.paulrybitskyi.gamedge.common.domain.games.entities.StreamItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class StreamLoadedSectionState(
    val isRefreshLoading: Boolean,
    val refresh: () -> Unit,
    val lazyListState: LazyStaggeredGridState,
    val navigation: NavHostController,
    val items: ImmutableList<StreamItem>
) : StreamContentSectionState

@Composable
fun rememberStreamLoadedSectionState(
    isRefreshLoading: Boolean,
    refresh: () -> Unit,
    navigation: NavHostController,
    uiState: StreamUiState
): StreamLoadedSectionState {
    val context = LocalContext.current
    val lazyListState: LazyStaggeredGridState = rememberLazyStaggeredGridState()
    val items = uiState.items.toImmutableList()
    return remember(
        context,
        lazyListState,
        navigation,
        items
    ) {
        StreamLoadedSectionState(
            isRefreshLoading = isRefreshLoading,
            refresh = refresh,
            lazyListState = lazyListState,
            navigation = navigation,
            items = items
        )
    }
}
