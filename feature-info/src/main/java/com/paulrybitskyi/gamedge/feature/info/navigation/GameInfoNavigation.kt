package com.paulrybitskyi.gamedge.feature.info.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.paulrybitskyi.gamedge.common.ui.base.events.Route
import com.paulrybitskyi.gamedge.common.ui.base.events.Screen
import com.paulrybitskyi.gamedge.feature.info.presentation.widgets.main.GameInfo

fun NavGraphBuilder.gameInfoScreen(
    onRoute: (Route) -> Unit,
) {
    composable(
        route = Screen.GameInfo.route,
        arguments = listOf(
            navArgument(Screen.GameInfo.Parameters.GAME_ID) { type = NavType.IntType },
        ),
    ) {
        GameInfo { route ->
            onRoute(route)
        }
    }
}
