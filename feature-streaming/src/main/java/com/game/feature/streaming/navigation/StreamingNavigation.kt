package com.game.feature.streaming.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.game.feature.streaming.StreamingScreen

const val STREAMING_ROUTE = "streaming_route"

fun NavController.navigateToStreaming() = navigate(STREAMING_ROUTE)

fun NavGraphBuilder.twitchStreamingScreen(
    onShowSnackbar: suspend (String, String?) -> Boolean,
    onBackScreen: () -> Unit,
) {
    composable(
        route = STREAMING_ROUTE,
    ) {
        StreamingScreen(
            onShowSnackbar,
            onBackScreen = onBackScreen
        )
    }
}
