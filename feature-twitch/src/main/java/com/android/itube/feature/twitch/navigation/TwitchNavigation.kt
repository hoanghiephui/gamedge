package com.android.itube.feature.twitch.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.android.itube.feature.twitch.TwitchRoute
import com.paulrybitskyi.gamedge.common.domain.live.entities.StreamPlaybackAccessToken

const val TWITCH_ROUTE = "twitch_route"

fun NavController.navigateToTwitch(navOptions: NavOptions) = navigate(TWITCH_ROUTE, navOptions)

fun NavGraphBuilder.twitchScreen(onTopicClick: (StreamPlaybackAccessToken) -> Unit) {
    composable(
        route = TWITCH_ROUTE,
    ) {
        TwitchRoute(
            onTopicClick
        )
    }
}
