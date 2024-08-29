package com.android.itube.feature.twitch.state

import androidx.compose.runtime.Immutable
import com.android.model.StreamData
import com.android.model.StreamItem
import com.paulrybitskyi.gamedge.common.ui.widgets.ResultUiState

@Immutable
data class StreamUiState(
    val isLoading: Boolean,
    val isError: Boolean,
    val title: String,
    val items: List<StreamItem>, val countSize: Int
)

internal val StreamUiState.finiteUiState: ResultUiState
    get() = when {
        isInEmptyState -> ResultUiState.Empty
        isInLoadingState -> ResultUiState.Loading
        isInSuccessState -> ResultUiState.Success
        isInErrorState -> ResultUiState.Error
        else -> error("Unknown games category UI state.")
    }
private val StreamUiState.isInEmptyState: Boolean
    get() = (!isLoading && items.isEmpty() && !isError)

private val StreamUiState.isInLoadingState: Boolean
    get() = (isLoading && items.isEmpty())

private val StreamUiState.isInSuccessState: Boolean
    get() = items.isNotEmpty()

private val StreamUiState.isInErrorState: Boolean
    get() = isError

internal val StreamUiState.isRefreshing: Boolean
    get() = (isLoading && items.isNotEmpty())

internal fun StreamUiState.toEmptyState(): StreamUiState {
    return copy(isLoading = false, items = emptyList())
}

internal fun StreamUiState.toErrorState(): StreamUiState {
    return copy(
        isLoading = false,
        items = emptyList(),
        isError = true
    )
}

internal fun StreamUiState.enableLoading(): StreamUiState {
    return copy(isLoading = true)
}

internal fun StreamUiState.disableLoading(): StreamUiState {
    return copy(isLoading = false)
}

internal fun StreamUiState.toSuccessState(
    data: StreamData,
): StreamUiState {
    return copy(isLoading = false, items = data.items, countSize = data.items.size)
}

internal fun StreamUiState.toLoadMoreSuccessState(
    data: StreamData,
): StreamUiState {
    return copy(isLoading = false, items = items + data.items, countSize = data.items.size)
}
