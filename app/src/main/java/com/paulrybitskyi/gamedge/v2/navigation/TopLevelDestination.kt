package com.paulrybitskyi.gamedge.v2.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Stream
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.Stream
import androidx.compose.ui.graphics.vector.ImageVector
import com.paulrybitskyi.gamedge.common.ui.v2.icon.NiaIcons
import com.paulrybitskyi.gamedge.core.R
import com.paulrybitskyi.gamedge.feature.discovery.R as forYouR
import com.paulrybitskyi.gamedge.feature.likes.R as bookmarksR
import com.paulrybitskyi.gamedge.feature.news.R as newsR
import com.android.itube.feature.twitch.R as twitchR

/**
 * Type for the top level destinations in the application. Each of these destinations
 * can contain one or more screens (based on the window size). Navigation from one screen to the
 * next within a single destination will be handled directly in composables.
 */
enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val iconTextId: Int,
    val titleTextId: Int,
) {
    FOR_YOU(
        selectedIcon = NiaIcons.Upcoming,
        unselectedIcon = NiaIcons.UpcomingBorder,
        iconTextId = forYouR.string.games_discovery_toolbar_title,
        titleTextId = R.string.app_name,
    ),
    TWITCH(
        selectedIcon = Icons.Rounded.Stream,
        unselectedIcon = Icons.Outlined.Stream,
        iconTextId = twitchR.string.streamer,
        titleTextId = twitchR.string.streamer,
    ),
    BOOKMARKS(
        selectedIcon = NiaIcons.Bookmarks,
        unselectedIcon = NiaIcons.BookmarksBorder,
        iconTextId = bookmarksR.string.liked_games_toolbar_title,
        titleTextId = bookmarksR.string.liked_games_toolbar_title,
    ),
    NEWS(
        selectedIcon = Icons.Rounded.Newspaper,
        unselectedIcon = Icons.Rounded.Newspaper,
        iconTextId = newsR.string.gaming_news_toolbar_title,
        titleTextId = newsR.string.gaming_news_toolbar_title,
    ),
}
