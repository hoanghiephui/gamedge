package com.android.itube.feature.twitch.section

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.paulrybitskyi.gamedge.common.ui.v2.theme.NiaTheme

@Composable
fun StreamLoadingSection(
    sectionState: StreamLoadingSectionState,
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview
@Composable
private fun Preview() {
    NiaTheme {
        StreamLoadingSection(
            sectionState = StreamLoadingSectionState,
        )
    }
}
