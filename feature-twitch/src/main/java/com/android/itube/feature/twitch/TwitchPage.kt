package com.android.itube.feature.twitch

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.android.itube.feature.twitch.section.StreamInitialSection
import com.android.itube.feature.twitch.section.StreamInitialSectionState
import com.android.itube.feature.twitch.section.StreamLoadedSection
import com.android.itube.feature.twitch.section.StreamLoadedSectionState
import com.android.itube.feature.twitch.section.StreamLoadingSection
import com.android.itube.feature.twitch.section.StreamLoadingSectionState

@Composable
fun TwitchBoard(
    viewModel: TwitchViewModel,
    navigation: NavHostController,
) {
    val pageState = rememberStreamPageState(
        pageViewModel = viewModel,
        navigation = navigation
    )

    when (val sectionState = pageState.contentSectionState) {
        is StreamInitialSectionState -> StreamInitialSection(
            sectionState = sectionState
        )
        is StreamLoadingSectionState -> StreamLoadingSection(
            sectionState = sectionState,
        )
        is StreamLoadedSectionState -> StreamLoadedSection(
            sectionState = sectionState,
        )
    }
}
