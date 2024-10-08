/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paulrybitskyi.gamedge.v2.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.util.trace
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.android.itube.feature.twitch.navigation.TWITCH_ROUTE
import com.android.itube.feature.twitch.navigation.navigateToTwitch
import com.android.model.UserDataModel
import com.paulrybitskyi.gamedge.common.data.common.NetworkMonitor
import com.paulrybitskyi.gamedge.common.ui.widgets.TrackDisposableJank
import com.paulrybitskyi.gamedge.feature.discovery.navigation.FOR_YOU_ROUTE
import com.paulrybitskyi.gamedge.feature.discovery.navigation.navigateToForYou
import com.paulrybitskyi.gamedge.feature.likes.navigation.BOOKMARKS_ROUTE
import com.paulrybitskyi.gamedge.feature.likes.navigation.navigateToBookmarks
import com.paulrybitskyi.gamedge.feature.news.navigation.NEWS_ROUTE
import com.paulrybitskyi.gamedge.feature.news.navigation.navigateToNews
import com.paulrybitskyi.gamedge.v2.navigation.TopLevelDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Composable
fun rememberNiaAppState(
    networkMonitor: NetworkMonitor,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavHostController = rememberNavController(),
    uiProfileState: StateFlow<UserDataModel?>,
): NiaAppState {
    NavigationTrackingSideEffect(navController)
    return remember(
        navController,
        coroutineScope,
        networkMonitor,
    ) {
        NiaAppState(
            navController = navController,
            coroutineScope = coroutineScope,
            networkMonitor = networkMonitor,
            uiProfileState = uiProfileState
        )
    }
}

@Stable
class NiaAppState(
    val navController: NavHostController,
    coroutineScope: CoroutineScope,
    networkMonitor: NetworkMonitor,
    uiProfileState: StateFlow<UserDataModel?>,
) {
    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() = when (currentDestination?.route) {
            FOR_YOU_ROUTE -> TopLevelDestination.FOR_YOU
            TWITCH_ROUTE -> TopLevelDestination.TWITCH
            BOOKMARKS_ROUTE -> TopLevelDestination.BOOKMARKS
            NEWS_ROUTE -> TopLevelDestination.NEWS
            else -> null
        }

    val isOffline = networkMonitor.isOnline
        .map(Boolean::not)
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    /**
     * Map of top level destinations to be used in the TopBar, BottomBar and NavRail. The key is the
     * route.
     */
    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.entries
    val userModel = uiProfileState

    /**
     * UI logic for navigating to a top level destination in the app. Top level destinations have
     * only one copy of the destination of the back stack, and save and restore state whenever you
     * navigate to and from it.
     *
     * @param topLevelDestination: The destination the app needs to navigate to.
     */
    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        trace("Navigation: ${topLevelDestination.name}") {
            val topLevelNavOptions = navOptions {
                // Pop up to the start destination of the graph to
                // avoid building up a large stack of destinations
                // on the back stack as users select items
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                // Avoid multiple copies of the same destination when
                // reselecting the same item
                launchSingleTop = true
                // Restore state when reselecting a previously selected item
                restoreState = true
            }

            when (topLevelDestination) {
                TopLevelDestination.FOR_YOU -> navController.navigateToForYou(topLevelNavOptions)
                TopLevelDestination.TWITCH -> navController.navigateToTwitch(topLevelNavOptions)
                TopLevelDestination.BOOKMARKS -> navController.navigateToBookmarks(topLevelNavOptions)
                TopLevelDestination.NEWS -> navController.navigateToNews(topLevelNavOptions)
            }
        }
    }

    fun navigateToSearch() {

    }
}

/**
 * Stores information about navigation events to be used with JankStats
 */
@Composable
private fun NavigationTrackingSideEffect(navController: NavHostController) {
    TrackDisposableJank(navController) { metricsHolder ->
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            metricsHolder.state?.putState("Navigation", destination.route.toString())
        }

        navController.addOnDestinationChangedListener(listener)

        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }
}
