
package com.paulrybitskyi.gamedge.feature.likes.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.paulrybitskyi.gamedge.common.ui.base.events.Route
import com.paulrybitskyi.gamedge.feature.likes.presentation.LikedGames

const val BOOKMARKS_ROUTE = "bookmarks_route"

fun NavController.navigateToBookmarks(navOptions: NavOptions) = navigate(BOOKMARKS_ROUTE, navOptions)

fun NavGraphBuilder.bookmarksScreen(
    onRoute: (Route) -> Unit,
) {
    composable(
        route = BOOKMARKS_ROUTE,
    ) {
        LikedGames(modifier = Modifier) { route ->
            onRoute(route)
        }
    }
}


