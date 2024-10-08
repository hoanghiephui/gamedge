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

package com.paulrybitskyi.gamedge.feature.discovery.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.paulrybitskyi.gamedge.common.ui.base.events.Route
import com.paulrybitskyi.gamedge.feature.discovery.v2.GamesDiscoveryRoute

const val FOR_YOU_ROUTE = "for_you_route"

fun NavController.navigateToForYou(navOptions: NavOptions) = navigate(FOR_YOU_ROUTE, navOptions)

fun NavGraphBuilder.forYouScreen(onRoute: (Route) -> Unit) {
    composable(
        route = FOR_YOU_ROUTE,
    ) {
        GamesDiscoveryRoute(
            modifier = Modifier,
            onRoute = onRoute
        )
    }
}
