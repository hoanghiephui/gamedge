package com.paulrybitskyi.gamedge.feature.discovery.v2

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.paulrybitskyi.gamedge.common.ui.CommandsHandler
import com.paulrybitskyi.gamedge.common.ui.RoutesHandler
import com.paulrybitskyi.gamedge.common.ui.base.events.Route
import com.paulrybitskyi.gamedge.common.ui.theme.GamedgeTheme
import com.paulrybitskyi.gamedge.common.ui.widgets.GameCover
import com.paulrybitskyi.gamedge.common.ui.widgets.GameCoverPopular
import com.paulrybitskyi.gamedge.common.ui.widgets.RefreshableContent
import com.paulrybitskyi.gamedge.feature.discovery.GamesDiscoveryViewModel
import com.paulrybitskyi.gamedge.feature.discovery.widgets.GamesDiscoveryItemGameUiModel
import com.paulrybitskyi.gamedge.feature.discovery.widgets.GamesDiscoveryItemUiModel
import com.paulrybitskyi.gamedge.feature.discovery.widgets.SwipeRefreshIntentionalDelay
import com.paulrybitskyi.gamedge.feature.discovery.widgets.mapToCategoryUiModels
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun GamesDiscoveryRoute(
    modifier: Modifier = Modifier,
    viewModel: GamesDiscoveryViewModel = hiltViewModel(),
    onRoute: (Route) -> Unit
) {
    CommandsHandler(viewModel = viewModel)
    RoutesHandler(viewModel = viewModel, onRoute = onRoute)
    GamesDiscoveryScreen(
        items = viewModel.items.collectAsState().value,
        onCategoryMoreButtonClicked = viewModel::onCategoryMoreButtonClicked,
        onSearchButtonClicked = viewModel::onSearchButtonClicked,
        onCategoryGameClicked = viewModel::onCategoryGameClicked,
        onRefreshRequested = viewModel::onRefreshRequested,
        modifier = modifier,
    )
}

@Composable
internal fun GamesDiscoveryScreen(
    items: List<GamesDiscoveryItemUiModel>,
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
        modifier = modifier
            .fillMaxSize(),
        onRefreshRequested = {
            isRefreshing = true

            coroutineScope.launch {
                delay(SwipeRefreshIntentionalDelay)
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
    items: List<GamesDiscoveryItemUiModel>,
    onCategoryMoreButtonClicked: (category: String) -> Unit,
    onCategoryGameClicked: (GamesDiscoveryItemGameUiModel) -> Unit,
) {
    /*HorizontalMultiBrowseCarousel(
        state = rememberCarouselState { 6 },
        modifier = Modifier
            .width(412.dp)
            .height(221.dp),
        preferredItemWidth = 186.dp,
        itemSpacing = 8.dp,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) { i ->
        Box(
            modifier = Modifier
                .height(205.dp)
                .maskClip(MaterialTheme.shapes.extraLarge)
        ) {
            Image(
                modifier = Modifier.height(205.dp).maskClip(MaterialTheme.shapes.extraLarge),
                painter = painterResource(id = com.paulrybitskyi.gamedge.core.R.drawable.core_designsystem_ic_placeholder_default),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )

        }

    }*/
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(GamedgeTheme.spaces.spacing_3_5),
    ) {
        itemsIndexed(
            items = items,
            key = { _, game -> game.id },
        ) { index, item ->
            val categoryGames = remember(item.games) {
                item.games.mapToCategoryUiModels()
            }
            if (index == 0 && categoryGames.isNotEmpty()) {
                //val size = categoryGames.subList(0, 5).count()
                val carouselState = rememberCarouselState { categoryGames.size }
                HorizontalMultiBrowseCarousel(
                    state = carouselState,
                    modifier = Modifier
                        .width(450.dp)
                        .height(221.dp),
                    preferredItemWidth = 186.dp,
                    itemSpacing = 8.dp,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) { i ->
                    val model = rememberAsyncImagePainter(model = categoryGames[i].coverUrl)
                    Image(
                        modifier = Modifier
                            .height(205.dp)
                            .maskClip(MaterialTheme.shapes.large),
                        painter = model,
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                    /*categoryGames.getOrNull(i)?.let { m ->
                        GameCoverPopular(
                            modifier = Modifier
                                .height(205.dp)
                                .maskClip(MaterialTheme.shapes.extraLarge),
                            title = item.title,
                            imageUrl = m.coverUrl
                        )
                    }*/

                }

            }


            /*GamesCategoryPreview(
                title = item.title,
                isProgressBarVisible = item.isProgressBarVisible,
                games = categoryGames,
                onCategoryGameClicked = { onCategoryGameClicked(it.mapToDiscoveryUiModel()) },
                onCategoryMoreButtonClicked = { onCategoryMoreButtonClicked(item.categoryName) },
            )*/
        }
    }
}
