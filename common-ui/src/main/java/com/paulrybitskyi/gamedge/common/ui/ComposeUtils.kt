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

package com.paulrybitskyi.gamedge.common.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.Window
import android.annotation.SuppressLint
import android.content.Context
import android.view.OrientationEventListener
import androidx.annotation.DimenRes
import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun textSizeResource(@DimenRes id: Int): TextUnit {
    return with(LocalDensity.current) {
        dimensionResource(id).toSp()
    }
}

@Composable
fun OnLifecycleEvent(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onCreate: (() -> Unit)? = null,
    onStart: (() -> Unit)? = null,
    onResume: (() -> Unit)? = null,
    onPause: (() -> Unit)? = null,
    onStop: (() -> Unit)? = null,
    onDestroy: (() -> Unit)? = null,
    onAny: (() -> Unit)? = null,
) {
    val currentOnCreate by rememberUpdatedState(onCreate)
    val currentOnStart by rememberUpdatedState(onStart)
    val currentOnResume by rememberUpdatedState(onResume)
    val currentOnPause by rememberUpdatedState(onPause)
    val currentOnStop by rememberUpdatedState(onStop)
    val currentOnDestroy by rememberUpdatedState(onDestroy)
    val currentOnAny by rememberUpdatedState(onAny)

    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> currentOnCreate?.invoke()
                Lifecycle.Event.ON_START -> currentOnStart?.invoke()
                Lifecycle.Event.ON_RESUME -> currentOnResume?.invoke()
                Lifecycle.Event.ON_PAUSE -> currentOnPause?.invoke()
                Lifecycle.Event.ON_STOP -> currentOnStop?.invoke()
                Lifecycle.Event.ON_DESTROY -> currentOnDestroy?.invoke()
                Lifecycle.Event.ON_ANY -> currentOnAny?.invoke()
            }
        }

        lifecycle.addObserver(lifecycleObserver)
        onDispose { lifecycle.removeObserver(lifecycleObserver) }
    }
}

@Composable
fun Modifier.clickable(
    indication: Indication?,
    onClick: () -> Unit,
) = this.composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = indication,
        onClick = onClick,
    )
}

@Composable
fun findWindow(): Window? {
    return (LocalView.current.parent as? DialogWindowProvider)?.window
        ?: LocalView.current.context.findWindow()
}

private tailrec fun Context.findWindow(): Window? {
    return when (this) {
        is Activity -> window
        is ContextWrapper -> baseContext.findWindow()
        else -> null
    }
}

@Composable
fun rememberWindowInsetsController(
    window: Window? = findWindow(),
): WindowInsetsControllerCompat? {
    val view = LocalView.current

    return remember(view, window) {
        if (window != null) {
            WindowCompat.getInsetsController(window, view)
        } else {
            null
        }
    }
}

fun LazyStaggeredGridState.reachedBottom(buffer: Int = 1): Boolean {
    val lastVisibleItem = this.layoutInfo.visibleItemsInfo.lastOrNull()
    return lastVisibleItem?.index != 0 && lastVisibleItem?.index == this.layoutInfo.totalItemsCount - buffer
}

@Composable
fun KeepScreenOn() {
    val currentView = LocalView.current
    DisposableEffect(Unit) {
        currentView.keepScreenOn = true
        onDispose {
            currentView.keepScreenOn = false
        }
    }
}

@Composable
fun DeviceOrientationListener(
    applicationContext: Context,
    onOrientationChange: (orientation: DeviceOrientation) -> Unit
) {

    DisposableEffect(key1 = Unit) {
        val orientationListener =
            object : OrientationEventListener(applicationContext) {
                override fun onOrientationChanged(orientation: Int) {
                    if (orientation >= 350 || orientation < 10) {
                        onOrientationChange(DeviceOrientation.Portrait(orientation))
                    } else if (orientation in 80..159) {
                        onOrientationChange(DeviceOrientation.ReverseLandscape(orientation))
                    } else if (orientation in 200..289) {
                        onOrientationChange(DeviceOrientation.Landscape(orientation))
                    }
                }
            }
        orientationListener.enable()

        onDispose {
            orientationListener.disable()
        }
    }
}

fun Modifier.notificationDot(): Modifier =
    composed {
        val tertiaryColor = Color.Red
        drawWithContent {
            drawContent()
            drawCircle(
                tertiaryColor,
                radius = 5.dp.toPx(),
                // This is based on the dimensions of the NavigationBar's "indicator pill";
                // however, its parameters are private, so we must depend on them implicitly
                // (NavigationBarTokens.ActiveIndicatorWidth = 64.dp)
                center = center + Offset(
                    64.dp.toPx() * .45f,
                    32.dp.toPx() * -.45f - 6.dp.toPx(),
                ),
            )
        }
    }

fun Modifier.fadingEdge(brush: Brush) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }
