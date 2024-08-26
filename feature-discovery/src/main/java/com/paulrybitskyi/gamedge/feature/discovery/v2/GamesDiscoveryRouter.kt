package com.paulrybitskyi.gamedge.feature.discovery.v2

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paulrybitskyi.gamedge.common.ui.CommandsHandler
import com.paulrybitskyi.gamedge.common.ui.RoutesHandler
import com.paulrybitskyi.gamedge.common.ui.base.events.Route
import com.paulrybitskyi.gamedge.common.ui.theme.GamedgeTheme
import com.paulrybitskyi.gamedge.common.ui.widgets.NewsResourceHeaderImage
import com.paulrybitskyi.gamedge.common.ui.widgets.RefreshableContent
import com.paulrybitskyi.gamedge.common.ui.widgets.categorypreview.GamesCategoryPreview
import com.paulrybitskyi.gamedge.feature.discovery.GamesDiscoveryCategory
import com.paulrybitskyi.gamedge.feature.discovery.GamesDiscoveryItemUiModel
import com.paulrybitskyi.gamedge.feature.discovery.GamesDiscoveryViewModel
import com.paulrybitskyi.gamedge.feature.discovery.PullRefreshIntentionalDelay
import com.paulrybitskyi.gamedge.feature.discovery.widgets.GamesDiscoveryItemGameUiModel
import com.paulrybitskyi.gamedge.feature.discovery.widgets.mapToCategoryUiModels
import com.paulrybitskyi.gamedge.feature.discovery.widgets.mapToDiscoveryUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun GamesDiscoveryRoute(
    modifier: Modifier = Modifier,
    viewModel: GamesDiscoveryViewModel = hiltViewModel(),
    onRoute: (Route) -> Unit
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    CommandsHandler(viewModel = viewModel)
    RoutesHandler(viewModel = viewModel, onRoute = onRoute)
    GamesDiscoveryScreen(
        items = items.toImmutableList(),
        onCategoryMoreButtonClicked = viewModel::onCategoryMoreButtonClicked,
        onSearchButtonClicked = viewModel::onSearchButtonClicked,
        onCategoryGameClicked = viewModel::onCategoryGameClicked,
        onRefreshRequested = viewModel::onRefreshRequested,
        modifier = modifier,
    )
}

@Composable
internal fun GamesDiscoveryScreen(
    items: ImmutableList<GamesDiscoveryItemUiModel>,
    onSearchButtonClicked: () -> Unit,
    onCategoryMoreButtonClicked: (category: String) -> Unit,
    onCategoryGameClicked: (GamesDiscoveryItemGameUiModel) -> Unit,
    onRefreshRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    RefreshableContent(
        isRefreshing = isRefreshing,
        modifier = modifier.windowInsetsPadding(
            WindowInsets.safeDrawing.only(
                WindowInsetsSides.Horizontal,
            ),
        ),
        onRefreshRequested = {
            isRefreshing = true

            coroutineScope.launch {
                delay(PullRefreshIntentionalDelay)
                onRefreshRequested()
                isRefreshing = false
            }
        },
    ) {
        CategoryPreviewItems(
            items = items,
            onCategoryMoreButtonClicked = onCategoryMoreButtonClicked,
            onCategoryGameClicked = onCategoryGameClicked,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryPreviewItems(
    items: ImmutableList<GamesDiscoveryItemUiModel>,
    onCategoryMoreButtonClicked: (category: String) -> Unit,
    onCategoryGameClicked: (GamesDiscoveryItemGameUiModel) -> Unit,
) {
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(GamedgeTheme.spaces.spacing_3_5),
    ) {
        items(items = items, key = GamesDiscoveryItemUiModel::id) { item ->
            val categoryGames = remember(item.games) {
                item.games.mapToCategoryUiModels().toImmutableList()
            }
            if (categoryGames.isNotEmpty() && GamesDiscoveryCategory.POPULAR.id == item.id) {
                val carouselState = rememberCarouselState { categoryGames.size }
                HorizontalMultiBrowseCarousel(
                    state = carouselState,
                    modifier = Modifier
                        .width(412.dp)
                        .height(221.dp),
                    preferredItemWidth = 186.dp,
                    itemSpacing = 8.dp,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) { i ->
                    NewsResourceHeaderImage(
                        modifier = Modifier
                            .height(205.dp)
                            .clickable {
                                onCategoryGameClicked(categoryGames[i].mapToDiscoveryUiModel())
                            }
                            .maskClip(MaterialTheme.shapes.large),
                        headerImageUrl = categoryGames[i].coverUrl
                    )
                }

            } else {
                GamesCategoryPreview(
                    title = item.title,
                    isProgressBarVisible = item.isProgressBarVisible,
                    games = categoryGames,
                    onCategoryGameClicked = { onCategoryGameClicked(it.mapToDiscoveryUiModel()) },
                    onCategoryMoreButtonClicked = { onCategoryMoreButtonClicked(item.categoryName) },
                )
            }
        }
    }
}
