/*
 * Copyright 2020 Paul Rybitskyi, paul.rybitskyi.work@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paulrybitskyi.gamedge.common.ui.base.events

import androidx.compose.runtime.saveable.Saver
import com.paulrybitskyi.gamedge.core.utils.toCsv
import java.net.URLEncoder

interface Route

sealed class Screen(val route: String) {
    data object Discover : Screen("discover")
    data object Likes : Screen("likes")
    data object News : Screen("news")
    data object Settings : Screen("settings")
    data object GamesSearch : Screen("games-search")

    data object GamesCategory : Screen("games-category/{${Parameters.CATEGORY}}") {
        object Parameters {
            const val CATEGORY = "category"
        }

        fun createLink(category: String): String {
            return "games-category/$category"
        }
    }

    data object GameInfo : Screen("game-info/{${Parameters.GAME_ID}}") {
        object Parameters {
            const val GAME_ID = "game-id"
        }

        fun createLink(gameId: Int): String {
            return "game-info/$gameId"
        }
    }

    data object ImageViewer : Screen(
        "image-viewer?" +
                "${Parameters.TITLE}={${Parameters.TITLE}}&" +
                "${Parameters.INITIAL_POSITION}={${Parameters.INITIAL_POSITION}}&" +
                "${Parameters.IMAGE_URLS}={${Parameters.IMAGE_URLS}}",
    ) {
        object Parameters {
            const val TITLE = "title"
            const val INITIAL_POSITION = "initial-position"
            const val IMAGE_URLS = "image-urls"
        }

        fun createLink(
            title: String?,
            initialPosition: Int,
            imageUrls: List<String>,
        ): String {
            val modifiedImageUrls = imageUrls
                .map { imageUrl -> URLEncoder.encode(imageUrl, "UTF-8") }
                .toCsv()

            return buildString {
                append("image-viewer?")

                if (title != null) {
                    append("${Parameters.TITLE}=$title&")
                }

                append("${Parameters.INITIAL_POSITION}=$initialPosition&")
                append("${Parameters.IMAGE_URLS}=$modifiedImageUrls")
            }
        }
    }

    internal companion object {

        val Saver = Saver(
            save = { it.route },
            restore = ::forRoute,
        )

        fun forRoute(route: String): Screen {
            return when (route) {
                Discover.route -> Discover
                Likes.route -> Likes
                News.route -> News
                Settings.route -> Settings
                GamesSearch.route -> GamesSearch
                GamesCategory.route -> GamesCategory
                GameInfo.route -> GameInfo
                ImageViewer.route -> ImageViewer
                else -> error("Cannot find screen for the route: $route.")
            }
        }
    }
}

const val STREAMING_KEY ="streaming_data"
