package com.paulrybitskyi.gamedge.feature.category.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.paulrybitskyi.gamedge.common.ui.base.events.Route
import com.paulrybitskyi.gamedge.common.ui.base.events.Screen
import com.paulrybitskyi.gamedge.feature.category.widgets.GamesCategory

fun NavGraphBuilder.categoryScreen(
    onRoute: (Route) -> Unit,
) {
    composable(
        route = Screen.GamesCategory.route,
        arguments = listOf(
            navArgument(Screen.GamesCategory.Parameters.CATEGORY) { type = NavType.StringType },
        ),
    ) {
        GamesCategory { route ->
            onRoute(route)
        }
    }
}
