/*
 * Copyright 2021 Paul Rybitskyi, paul.rybitskyi.work@gmail.com
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

package com.paulrybitskyi.gamedge.feature.category

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import com.paulrybitskyi.gamedge.common.ui.CommandsHandler
import com.paulrybitskyi.gamedge.common.ui.RoutesHandler
import com.paulrybitskyi.gamedge.common.ui.base.events.Route
import com.paulrybitskyi.gamedge.common.ui.theme.GamedgeTheme
import com.paulrybitskyi.gamedge.common.ui.v2.component.NiaTopAppBar
import com.paulrybitskyi.gamedge.common.ui.v2.icon.NiaIcons
import com.paulrybitskyi.gamedge.common.ui.widgets.AnimatedContentContainer
import com.paulrybitskyi.gamedge.common.ui.widgets.FiniteUiState
import com.paulrybitskyi.gamedge.common.ui.widgets.GameCover
import com.paulrybitskyi.gamedge.common.ui.widgets.GamedgeProgressIndicator
import com.paulrybitskyi.gamedge.common.ui.widgets.Info
import com.paulrybitskyi.gamedge.common.ui.widgets.RefreshableContent
import com.paulrybitskyi.gamedge.feature.category.widgets.rememberGamesGridConfig
import com.paulrybitskyi.gamedge.core.R as CoreR

@Composable
fun GamesCategoryScreen(onRoute: (Route) -> Unit) {
    GamesCategoryScreen(
        viewModel = hiltViewModel(),
        onRoute = onRoute,
    )
}

@Composable
private fun GamesCategoryScreen(
    viewModel: GamesCategoryViewModel,
    onRoute: (Route) -> Unit,
) {
    CommandsHandler(viewModel = viewModel)
    RoutesHandler(viewModel = viewModel, onRoute = onRoute)
    GamesCategoryScreen(
        uiState = viewModel.uiState.collectAsState().value,
        onBackButtonClicked = viewModel::onToolbarLeftButtonClicked,
        onGameClicked = viewModel::onGameClicked,
        onBottomReached = viewModel::onBottomReached,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GamesCategoryScreen(
    uiState: GamesCategoryUiState,
    onBackButtonClicked: () -> Unit,
    onGameClicked: (GameCategoryUiModel) -> Unit,
    onBottomReached: () -> Unit,
) {
    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            NiaTopAppBar(
                title = uiState.title,
                navigationIcon = NiaIcons.ArrowBack,
                navigationIconContentDescription = uiState.title,
                actionIconContentDescription = uiState.title,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                ),
                onNavigationClick = onBackButtonClicked,
            )
        },
    ) { paddingValues ->
        AnimatedContentContainer(
            finiteUiState = uiState.finiteUiState,
            modifier = Modifier.padding(paddingValues),
        ) { finiteUiState ->
            when (finiteUiState) {
                FiniteUiState.Empty -> {
                    EmptyState(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .align(Alignment.Center),
                    )
                }
                FiniteUiState.Loading -> {
                    LoadingState(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .align(Alignment.Center),
                    )
                }
                FiniteUiState.Success -> {
                    SuccessState(
                        uiState = uiState,
                        modifier = Modifier,
                        contentPadding = WindowInsets.navigationBars.asPaddingValues(),
                        onGameClicked = onGameClicked,
                        onBottomReached = onBottomReached,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier) {
    GamedgeProgressIndicator(modifier = modifier)
}

@Composable
private fun EmptyState(modifier: Modifier) {
    Info(
        icon = painterResource(CoreR.drawable.gamepad_variant_outline),
        title = stringResource(R.string.games_category_info_view_title),
        modifier = modifier.padding(horizontal = GamedgeTheme.spaces.spacing_7_5),
    )
}

@Composable
private fun SuccessState(
    uiState: GamesCategoryUiState,
    modifier: Modifier,
    contentPadding: PaddingValues,
    onGameClicked: (GameCategoryUiModel) -> Unit,
    onBottomReached: () -> Unit,
) {
    RefreshableContent(
        isRefreshing = uiState.isRefreshing,
        modifier = modifier,
        isSwipeEnabled = false,
    ) {
        VerticalGrid(
            games = uiState.games,
            contentPadding = contentPadding,
            onGameClicked = onGameClicked,
            onBottomReached = onBottomReached,
        )
    }
}

@Composable
private fun VerticalGrid(
    games: List<GameCategoryUiModel>,
    contentPadding: PaddingValues,
    onGameClicked: (GameCategoryUiModel) -> Unit,
    onBottomReached: () -> Unit,
) {
    val gridConfig = rememberGamesGridConfig()
    val gridItemSpacingInPx = gridConfig.itemSpacingInPx
    val gridSpanCount = gridConfig.spanCount
    val lastIndex = games.lastIndex

    LazyVerticalGrid(
        columns = GridCells.Fixed(gridConfig.spanCount),
        contentPadding = contentPadding,
    ) {
        itemsIndexed(items = games) { index, game ->
            if (index == lastIndex) {
                LaunchedEffect(lastIndex) {
                    onBottomReached()
                }
            }

            val column = (index % gridConfig.spanCount)
            val paddingValues = with(LocalDensity.current) {
                PaddingValues(
                    top = (if (index < gridSpanCount) gridItemSpacingInPx else 0f).toDp(),
                    bottom = gridItemSpacingInPx.toDp(),
                    start = (gridItemSpacingInPx - (column * gridItemSpacingInPx / gridSpanCount)).toDp(),
                    end = ((column + 1) * gridItemSpacingInPx / gridSpanCount).toDp(),
                )
            }

            GameCover(
                title = game.title,
                imageUrl = game.coverUrl,
                modifier = Modifier
                    .size(
                        width = gridConfig.itemWidthInDp,
                        height = gridConfig.itemHeightInDp,
                    )
                    .padding(paddingValues),
                hasRoundedShape = false,
                onCoverClicked = { onGameClicked(game) },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun GamesCategoryScreenSuccessStatePreview() {
    val games = buildList {
        repeat(15) { index ->
            add(
                GameCategoryUiModel(
                    id = index + 1,
                    title = "Popular Game",
                    coverUrl = null,
                ),
            )
        }
    }

    GamedgeTheme {
        GamesCategoryScreen(
            uiState = GamesCategoryUiState(
                isLoading = false,
                title = "Popular",
                games = games,
            ),
            onBackButtonClicked = {},
            onGameClicked = {},
            onBottomReached = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun GamesCategoryScreenEmptyStatePreview() {
    GamedgeTheme {
        GamesCategoryScreen(
            uiState = GamesCategoryUiState(
                isLoading = false,
                title = "Popular",
                games = emptyList(),
            ),
            onBackButtonClicked = {},
            onGameClicked = {},
            onBottomReached = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun GamesCategoryScreenLoadingStatePreview() {
    GamedgeTheme {
        GamesCategoryScreen(
            uiState = GamesCategoryUiState(
                isLoading = true,
                title = "Popular",
                games = emptyList(),
            ),
            onBackButtonClicked = {},
            onGameClicked = {},
            onBottomReached = {},
        )
    }
}
