
package com.paulrybitskyi.gamedge.feature.likes.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

const val BOOKMARKS_ROUTE = "bookmarks_route"

fun NavController.navigateToBookmarks(navOptions: NavOptions) = navigate(BOOKMARKS_ROUTE, navOptions)

fun NavGraphBuilder.bookmarksScreen(
    onTopicClick: (String) -> Unit,
    onShowSnackbar: suspend (String, String?) -> Boolean,
) {
    composable(route = BOOKMARKS_ROUTE) {

    }
}

