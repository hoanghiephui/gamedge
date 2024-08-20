package com.android.itube.feature.twitch

sealed class UiState {
    data object Init : UiState()
    data object Loading : UiState()
    data class Loaded(val token: String) : UiState()
    data object Error : UiState()
}
