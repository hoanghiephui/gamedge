package com.paulrybitskyi.gamedge.feature.image.viewer.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.paulrybitskyi.gamedge.common.ui.base.events.Route
import com.paulrybitskyi.gamedge.common.ui.base.events.Screen
import com.paulrybitskyi.gamedge.feature.image.viewer.ImageViewer

fun NavGraphBuilder.imageViewerScreen(
    onRoute: (Route) -> Unit,
) {
    composable(
        route = Screen.ImageViewer.route,
        arguments = listOf(
            navArgument(Screen.ImageViewer.Parameters.TITLE) {
                type = NavType.StringType
                nullable = true
            },
            navArgument(Screen.ImageViewer.Parameters.INITIAL_POSITION) {
                type = NavType.IntType
                defaultValue = 0
            },
            navArgument(Screen.ImageViewer.Parameters.IMAGE_URLS) {
                type = NavType.StringType
                nullable = true
            },
        ),
    ) {
        ImageViewer { route ->
            onRoute(route)
        }
    }
}
