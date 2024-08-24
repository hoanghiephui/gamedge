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

package com.paulrybitskyi.gamedge.v2.navigation

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.compose.NavHost
import com.android.itube.feature.twitch.navigation.twitchScreen
import com.game.feature.streaming.navigation.STREAMING_ROUTE
import com.game.feature.streaming.navigation.twitchStreamingScreen
import com.paulrybitskyi.gamedge.common.ui.base.events.STREAMING_KEY
import com.paulrybitskyi.gamedge.common.ui.base.events.Screen
import com.paulrybitskyi.gamedge.feature.category.GamesCategoryRoute
import com.paulrybitskyi.gamedge.feature.category.navigation.categoryScreen
import com.paulrybitskyi.gamedge.feature.discovery.GamesDiscoveryRoute
import com.paulrybitskyi.gamedge.feature.discovery.navigation.FOR_YOU_ROUTE
import com.paulrybitskyi.gamedge.feature.discovery.navigation.forYouScreen
import com.paulrybitskyi.gamedge.feature.image.viewer.ImageViewerRoute
import com.paulrybitskyi.gamedge.feature.image.viewer.navigation.imageViewerScreen
import com.paulrybitskyi.gamedge.feature.info.navigation.gameInfoScreen
import com.paulrybitskyi.gamedge.feature.info.presentation.GameInfoRoute
import com.paulrybitskyi.gamedge.feature.likes.navigation.bookmarksScreen
import com.paulrybitskyi.gamedge.feature.likes.presentation.LikedGamesRoute
import com.paulrybitskyi.gamedge.feature.news.navigation.newsScreen
import com.paulrybitskyi.gamedge.v2.ui.NiaAppState

/**
 * Top-level navigation graph. Navigation is organized as explained at
 * https://d.android.com/jetpack/compose/nav-adaptive
 *
 * The navigation graph defined in this file defines the different top level routes. Navigation
 * within each route is handled using state and Back Handlers.
 */
@Composable
fun NiaNavHost(
    appState: NiaAppState,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    modifier: Modifier = Modifier,
    startDestination: String = FOR_YOU_ROUTE,
) {
    val navController = appState.navController
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        forYouScreen(onRoute = { route ->
            when (route) {
                is GamesDiscoveryRoute.Search -> {
                    navController.navigate(Screen.GamesSearch.route)
                }

                is GamesDiscoveryRoute.Category -> {
                    navController.navigate(Screen.GamesCategory.createLink(route.category))
                }

                is GamesDiscoveryRoute.Info -> {
                    navController.navigate(Screen.GameInfo.createLink(route.gameId))
                }
            }
        })
        bookmarksScreen(
            onRoute = { route ->
                when (route) {
                    is LikedGamesRoute.Search -> {
                        navController.navigate(Screen.GamesSearch.route)
                    }

                    is LikedGamesRoute.Info -> {
                        navController.navigate(Screen.GameInfo.createLink(route.gameId))
                    }
                }
            },
        )

        twitchScreen(onTopicClick = {
            val bundle = bundleOf(
                STREAMING_KEY to it
            )
            navController.navigate(route = STREAMING_ROUTE, args = bundle)
        })

        newsScreen()

        gameInfoScreen(
            onRoute = { route ->
                when (route) {
                    is GameInfoRoute.ImageViewer -> {
                        navController.navigate(
                            Screen.ImageViewer.createLink(
                                title = route.title,
                                initialPosition = route.initialPosition,
                                imageUrls = route.imageUrls,
                            ),
                        )
                    }

                    is GameInfoRoute.Info -> {
                        navController.navigate(Screen.GameInfo.createLink(route.gameId))
                    }

                    is GameInfoRoute.Back -> {
                        navController.popBackStack()
                    }
                }

            }
        )
        imageViewerScreen(
            onRoute = { route ->
                when (route) {
                    is ImageViewerRoute.Back -> navController.popBackStack()
                }
            }
        )
        categoryScreen(onRoute = { route ->
            when (route) {
                is GamesCategoryRoute.Info -> {
                    navController.navigate(Screen.GameInfo.createLink(route.gameId))
                }

                is GamesCategoryRoute.Back -> {
                    navController.popBackStack()
                }
            }
        })
        twitchStreamingScreen(
            onShowSnackbar = onShowSnackbar,
            onBackScreen = {
                navController.popBackStack()
            }
        )

    }
}

@SuppressLint("RestrictedApi")
fun NavController.navigate(
    route: String,
    args: Bundle,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null
) {
    val routeLink =
        NavDeepLinkRequest.Builder.fromUri(NavDestination.createRoute(route).toUri()).build()

    val deepLinkMatch = graph.matchDeepLink(routeLink)
    if (deepLinkMatch != null) {
        val destination = deepLinkMatch.destination
        val id = destination.id
        navigate(id, args, navOptions, navigatorExtras)
    } else {
        navigate(route, navOptions, navigatorExtras)
    }
}
