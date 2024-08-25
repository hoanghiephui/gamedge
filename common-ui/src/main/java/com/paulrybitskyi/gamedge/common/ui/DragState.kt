package com.paulrybitskyi.gamedge.common.ui

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember

@Composable
fun rememberDraggableActions(): ModViewDragState {
    return remember { ModViewDragState() }
}

@Stable
class ModViewDragState {
    val offset: State<Float> get() = offsetX
    private var offsetX = mutableFloatStateOf(0f)

    val width: State<Int> get() = swipeWidth
    private var swipeWidth = mutableIntStateOf(100)

    val halfWidth get() = (swipeWidth.intValue / 2.5).toInt()
    val quarterWidth get() = swipeWidth.intValue / 4

    fun setWidth(width: Int) {
        swipeWidth.intValue = width
    }

    val draggableState = DraggableState { delta ->
        when {
            offsetX.floatValue >= halfWidth -> offsetX.floatValue += delta / 5
            offsetX.floatValue <= -halfWidth -> offsetX.floatValue += delta / 5
            else -> offsetX.floatValue += delta
        }
    }

    suspend fun resetOffset() {
        Log.d("resetOffset", "offsetX --> ${offsetX.floatValue}")
        draggableState.drag(MutatePriority.PreventUserInput) {
            Animatable(offsetX.floatValue).animateTo(
                targetValue = 0f,
                tween(durationMillis = 300)
            ) {
                dragBy(value - offsetX.floatValue)
            }
        }
    }

    fun checkDragThresholdCrossed(
        deleteMessageSwipe: () -> Unit,
        timeoutUserSwipe: () -> Unit,
        banUserSwipe: () -> Unit,
    ) {

        when {
            offset.value >= halfWidth -> {
                deleteMessageSwipe()
            }

            offset.value <= -halfWidth -> {
                deleteMessageSwipe()
            }

            offset.value >= quarterWidth -> {
                Log.d("checkDragThresholdCrossed", "banUserSwipe")
                banUserSwipe()
            }

            offset.value <= -quarterWidth -> {
                Log.d("checkDragThresholdCrossed", "timeoutUserSwipe")
                timeoutUserSwipe()
            }
        }
    }

    fun checkQuarterSwipeThresholds(
        leftSwipeAction: () -> Unit,
        rightSwipeAction: () -> Unit,
    ) {
        when {

            offset.value >= quarterWidth -> {
                Log.d("checkDragThresholdCrossed", "banUserSwipe")
                rightSwipeAction()
            }

            offset.value <= -quarterWidth -> {
                Log.d("checkDragThresholdCrossed", "timeoutUserSwipe")
                leftSwipeAction()
            }
        }

    }
}

