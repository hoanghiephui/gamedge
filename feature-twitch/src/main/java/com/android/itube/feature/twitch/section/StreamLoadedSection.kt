package com.android.itube.feature.twitch.section

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.paulrybitskyi.gamedge.common.domain.games.entities.StreamItem
import com.paulrybitskyi.gamedge.common.ui.widgets.NewsResourceHeaderImage
import kotlinx.collections.immutable.ImmutableList

@Composable
fun StreamLoadedSection(
    sectionState: StreamLoadedSectionState,
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(300.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp,
        state = sectionState.lazyListState,
    ) {
        exploreFeed(
            itemVideos = sectionState.items,
            onClickVideo = { _, _ -> }
        )
    }
}

private fun LazyStaggeredGridScope.exploreFeed(
    itemVideos: ImmutableList<StreamItem>,
    onClickVideo: (List<StreamItem>, Int) -> Unit
) {
    itemsIndexed(
        items = itemVideos,
        key = { _, t -> t.id },
        contentType = { _, _ -> "exploreFeedItem" },
    ) { index, exploreFeedItem ->
        VideoCard(
            modifier = Modifier.animateItem(),
            videoModel = exploreFeedItem,
            onClickVideo = {
                onClickVideo(itemVideos, index)
            }
        )
    }
}

@Composable
private fun VideoCard(
    modifier: Modifier,
    videoModel: StreamItem,
    onClickVideo: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraSmall)
            .defaultMinSize(minHeight = 200.dp)
            .wrapContentHeight()
            .clickable {
                onClickVideo()
            }
    ) {
        NewsResourceHeaderImage(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(MaterialTheme.shapes.extraSmall),
            headerImageUrl = videoModel.thumbnailUrl
        )
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        0F to Color.Transparent,
                        .5F to Color.Black.copy(alpha = 0.5F),
                        1F to Color.Black.copy(alpha = 0.8F)
                    )
                )
                .align(Alignment.BottomEnd)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = videoModel.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    modifier = Modifier.padding(top = 3.dp),
                    text = videoModel.userName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

}
