package com.paulrybitskyi.gamedge.common.ui.widgets

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.paulrybitskyi.gamedge.core.R as coreR

@Composable
fun TwitchLogo(modifier: Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(coreR.raw.twitch))
    LottieAnimation(
        composition,
        modifier = modifier.size(300.dp),
        iterations = LottieConstants.IterateForever
    )
}
