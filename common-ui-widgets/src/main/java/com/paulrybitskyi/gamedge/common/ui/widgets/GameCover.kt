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

package com.paulrybitskyi.gamedge.common.ui.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.AsyncImagePainter.State
import coil.compose.rememberAsyncImagePainter
import com.paulrybitskyi.gamedge.common.ui.images.defaultImageRequest
import com.paulrybitskyi.gamedge.common.ui.images.secondaryImage
import com.paulrybitskyi.gamedge.common.ui.theme.GamedgeTheme

val DefaultCoverWidth = 112.dp
val DefaultCoverHeight = 153.dp

@Composable
fun GameCover(
    title: String?,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    hasRoundedShape: Boolean = true,
    onCoverClicked: (() -> Unit)? = null,
) {
    val cardModifier = modifier.size(width = DefaultCoverWidth, height = DefaultCoverHeight)
    val shape = if (hasRoundedShape) GamedgeTheme.shapes.medium else RectangleShape
    val backgroundColor = Color.Transparent
    val content: @Composable () -> Unit = {
        Box(modifier = modifier.fillMaxSize()) {
            var imageState by remember { mutableStateOf<State>(State.Empty) }
            // Not using derivedStateOf here because rememberSaveable does not support derivedStateOf?
            // https://stackoverflow.com/questions/71986944/custom-saver-remembersaveable-using-derivedstateof
            val shouldDisplayTitle = rememberSaveable(title, imageState) {
                (title != null) &&
                        (imageState !is State.Success)
            }

            AsyncImage(
                model = defaultImageRequest(imageUrl) {
                    secondaryImage(R.drawable.game_cover_placeholder)
                },
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                onState = { state ->
                    imageState = state
                },
                contentScale = ContentScale.Crop,
            )

            if (shouldDisplayTitle) {
                Text(
                    text = checkNotNull(title),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = GamedgeTheme.spaces.spacing_4_0),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = GamedgeTheme.typography.caption,
                )
            }
        }
    }

    if (onCoverClicked != null) {
        ElevatedCard(
            modifier = cardModifier,
            shape = shape,
            onClick = onCoverClicked,
        ) {
            content()
        }
    } else {
        ElevatedCard(
            modifier = cardModifier,
            shape = shape,
        ) {
            content()
        }
    }
}

@Composable
fun NewsResourceHeaderImage(
    modifier: Modifier = Modifier,
    headerImageUrl: String?,
) {
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    val imageLoader = rememberAsyncImagePainter(
        model = headerImageUrl,
        onState = { state ->
            isLoading = state is State.Loading
            isError = state is State.Error
        },
    )
    val isLocalInspection = LocalInspectionMode.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            // Display a progress bar while loading
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(50.dp),
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Image(
            modifier = Modifier
                .fillMaxWidth(),
            contentScale = ContentScale.Crop,
            painter = if (isError.not() && !isLocalInspection) {
                imageLoader
            } else {
                painterResource(com.paulrybitskyi.gamedge.core.R.drawable.core_designsystem_ic_placeholder_default)
            },
            contentDescription = null,
        )
    }
}

@PreviewLightDark
@Composable
private fun GameCoverWithTitlePreview() {
    GamedgeTheme {
        GameCover(
            title = "Ghost of Tsushima: Director's Cut",
            imageUrl = null,
            onCoverClicked = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun GameCoverWithoutTitlePreview() {
    GamedgeTheme {
        GameCover(
            title = null,
            imageUrl = null,
            onCoverClicked = {},
        )
    }
}
