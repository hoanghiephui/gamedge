package com.paulrybitskyi.gamedge.common.ui.widgets.categorypreview.v2

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.util.fastAll
import com.paulrybitskyi.gamedge.common.ui.theme.GamedgeTheme
import com.paulrybitskyi.gamedge.common.ui.widgets.categorypreview.GamesCategoryPreviewItemUiModel

@Composable
fun GamesCategoryPreview(
    title: String,
    isProgressBarVisible: Boolean,
    games: List<GamesCategoryPreviewItemUiModel>,
    onCategoryGameClicked: (GamesCategoryPreviewItemUiModel) -> Unit,
    topBarMargin: Dp = GamedgeTheme.spaces.spacing_2_0,
    isMoreButtonVisible: Boolean = true,
    onCategoryMoreButtonClicked: (() -> Unit)? = null,
) {
    Box(modifier = Modifier.fillMaxWidth()) {

    }
}
