package com.game.feature.streaming

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.C
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import coil.imageLoader
import com.game.feature.streaming.component.QualitySelectionDialog
import com.game.feature.streaming.component.StreamingPlayer
import com.game.feature.streaming.component.rememberStreamingPlayer
import com.paulrybitskyi.gamedge.common.domain.live.entities.StreamPlaybackAccessToken
import com.paulrybitskyi.gamedge.common.ui.KeepScreenOn
import com.paulrybitskyi.gamedge.common.ui.theme.GamedgeTheme
import com.paulrybitskyi.gamedge.common.ui.theme.lightScrim
import com.paulrybitskyi.gamedge.core.utils.OnlineSince
import com.paulrybitskyi.gamedge.core.utils.formattedCount
import com.paulrybitskyi.gamedge.core.utils.setScreenOrientation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.paulrybitskyi.gamedge.core.R as coreR

@OptIn(UnstableApi::class)
@Composable
fun StreamingScreen(
    onShowSnackbar: suspend (String, String?) -> Boolean,
    onBackScreen: () -> Unit,
    viewModel: StreamingViewModel = hiltViewModel()
) {
    var showLoading by remember {
        mutableStateOf(true)
    }
    var pauseButtonVisibility by remember { mutableStateOf(false) }
    val rememberCoroutineScope = rememberCoroutineScope()
    val playErrorMessage = stringResource(id = R.string.player_error)
    var isFullscreen by rememberSaveable { mutableStateOf(false) }
    var isVolumeOff by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    val configuration = LocalConfiguration.current
    val orientation = configuration.orientation
    var videoWidthFraction by remember { mutableFloatStateOf(0.67f) } // Initial weight for the video player
    var isPlaying by remember { mutableStateOf(false) }
    var isPlayWhenReadyChanged by remember { mutableStateOf(false) }
    var showDialogSetting by remember { mutableStateOf(false) }
    var selectedGroupIndex by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val startTime = viewModel.streamPlaybackAccessToken.startTime
    BackHandler {
        if (isFullscreen) {
            context.setScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }
        onBackScreen()
    }
    val streamingPlayer = rememberStreamingPlayer(
        streamPlaybackAccessToken = viewModel.streamPlaybackAccessToken,
        onShowProcess = {
            showLoading = it
        },
        onVideoGoBackground = {
            pauseButtonVisibility = false
        },
        onVideoDispose = {
            pauseButtonVisibility = false
        },
        onPlayError = { error ->
            rememberCoroutineScope.launch {
                onShowSnackbar(playErrorMessage, null)
            }
            onBackScreen()
        },
        onPlayWhenReadyChanged = {
            isPlayWhenReadyChanged = it
        },
        onIsPlayingChanged = {
            isPlaying = it
        }
    )
    LaunchedEffect(Unit) {

    }
    // Function to show the dialog
    val onQualitySelected: (Tracks.Group?, Int) -> Unit = { selectedGroup, selectedTrackIndex ->
        if (selectedGroup == null && selectedTrackIndex == -1) {
            // Reset to Auto mode
            val parametersBuilder = streamingPlayer.exoPlayer.trackSelectionParameters
                .buildUpon()
                .clearOverridesOfType(C.TRACK_TYPE_VIDEO)

            streamingPlayer.exoPlayer.trackSelectionParameters = parametersBuilder.build()
        } else {
            // Set specific quality
            val override = TrackSelectionOverride(
                selectedGroup!!.mediaTrackGroup,
                listOf(selectedTrackIndex)
            )

            val parametersBuilder = streamingPlayer.exoPlayer.trackSelectionParameters
                .buildUpon()
                .setOverrideForType(override)

            streamingPlayer.exoPlayer.trackSelectionParameters = parametersBuilder.build()
        }

        // Update selected quality index
        selectedGroupIndex = if (selectedGroup == null || selectedTrackIndex == -1) null
        else Pair(streamingPlayer.availableQualities.indexOf(selectedGroup), selectedTrackIndex)
    }


    LaunchedEffect(isPlayWhenReadyChanged) {
        if (isPlayWhenReadyChanged) {
            streamingPlayer.playerView.player?.seekToDefaultPosition()
        }
    }

    // Show the QualitySelectionDialog if showDialogSetting is true
    if (showDialogSetting) {
        QualitySelectionDialog(
            modifier = Modifier,
            availableQualities = streamingPlayer.availableQualities,
            selectedGroupIndex = selectedGroupIndex,
            onQualitySelected = { selectedGroup, selectedTrackIndex ->
                onQualitySelected(selectedGroup, selectedTrackIndex)
                showDialogSetting = false
            },
            onDismissRequest = {
                showDialogSetting = false
            }
        )
    }
    val content = stringResource(
        id = coreR.string.share_streaming,
        viewModel.streamPlaybackAccessToken.title,
        viewModel.streamPlaybackAccessToken.streamerName
    )
    val shareStream = remember {
        {
            viewModel.shareStreaming(context, content)
        }
    }

    KeepScreenOn()
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        when (orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    ChatView(
                        modifier = Modifier
                            .fillMaxHeight()
                            .statusBarsPadding()
                            .fillMaxWidth(0.33f) // Chiếm phần còn lại của chiều rộng màn hình
                            .align(Alignment.CenterEnd)
                            .padding(start = 20.dp)
                    )
                    Box(modifier = Modifier
                        .background(color = Color.Black)
                        .fillMaxHeight()
                        .fillMaxWidth(videoWidthFraction) // Điều chỉnh chiều rộng của Video dựa trên thao tác kéo
                        .align(Alignment.CenterStart)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragStart = {
                                    // Handle drag start if needed
                                },
                                onDragEnd = {
                                    // Handle drag end if needed
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    // Cập nhật videoWidthFraction dựa trên dragAmount
                                    videoWidthFraction =
                                        (videoWidthFraction + (dragAmount / 1000f)).coerceIn(0.67f, 1f)
                                    change.consume()
                                }
                            )
                        }
                    ) {
                        AndroidView(
                            factory = {
                                streamingPlayer.playerView
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(28.dp)
                                .align(Alignment.Center)
                        )
                        MenuControl(
                            isFullscreen = isFullscreen,
                            isVolumeOff = isVolumeOff,
                            isPlaying = isPlaying,
                            context = context,
                            onBackScreen = onBackScreen,
                            streamingPlayer = streamingPlayer,
                            onClickFull = {
                                isFullscreen = !isFullscreen
                            },
                            onShareStreaming = {
                                shareStream()
                            },
                            onVolumeClick = {
                                isVolumeOff = !isVolumeOff
                            },
                            onSetting = {
                                showDialogSetting = true
                            },
                            onClickPlayer = {
                                if (isPlaying) {
                                    streamingPlayer.exoPlayer.pause()
                                } else {
                                    streamingPlayer.exoPlayer.play()
                                }
                            },
                            startTime = startTime,
                            viewerCount = viewModel.streamPlaybackAccessToken.viewerCount
                        )
                    }

                }
            }

            Configuration.ORIENTATION_PORTRAIT -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16 / 9f, true)
                    ) {
                        AsyncImage(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16 / 9f, true),
                            model = viewModel.streamPlaybackAccessToken.thumbnailVideo,
                            contentScale = ContentScale.Crop,
                            contentDescription = "",
                            imageLoader = LocalContext.current.imageLoader
                        )
                        AndroidView(
                            factory = {
                                streamingPlayer.playerView
                            }, modifier = Modifier
                                .pointerInput(Unit) {
                                    detectTapGestures(onTap = {
                                        //onSingleTap(exoPlayer)
                                    }, onDoubleTap = { offset ->
                                        //onDoubleTap(exoPlayer, offset)
                                    })
                                }
                                .fillMaxWidth()
                                .aspectRatio(16 / 9f, true)
                        )
                        MenuControl(
                            isFullscreen = isFullscreen,
                            isVolumeOff = isVolumeOff,
                            isPlaying = isPlaying,
                            context = context,
                            onBackScreen = onBackScreen,
                            streamingPlayer = streamingPlayer,
                            onClickFull = {
                                isFullscreen = !isFullscreen
                            },
                            onShareStreaming = {
                                shareStream()
                            },
                            onVolumeClick = {
                                isVolumeOff = !isVolumeOff
                                streamingPlayer.toggleMute(isVolumeOff)
                            },
                            onSetting = {
                                showDialogSetting = true
                            },
                            onClickPlayer = {
                                if (isPlaying) {
                                    streamingPlayer.exoPlayer.pause()
                                } else {
                                    streamingPlayer.exoPlayer.play()
                                }
                            },
                            startTime = startTime,
                            viewerCount = viewModel.streamPlaybackAccessToken.viewerCount
                        )
                    }

                    TitleAndProfile(viewModel.streamPlaybackAccessToken)
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            top = 8.dp,
                            bottom = 8.dp
                        )
                    )
                    ChatView(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TitleAndProfile(
    data: StreamPlaybackAccessToken
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        AsyncImage(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(50.dp)
                .clip(CircleShape)                       // clip to the circle shape
                .border(2.dp, Color.Gray, CircleShape),
            model = "https://static-cdn.jtvnw.net/jtv_user_pictures/34c5c8da-19b6-47b2-85ba-cc7ae87f1413-profile_image-50x50.png",
            contentScale = ContentScale.Crop,
            contentDescription = "",
            imageLoader = LocalContext.current.imageLoader
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = data.streamerName,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = data.title,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun MenuControl(
    isFullscreen: Boolean,
    isVolumeOff: Boolean,
    isPlaying: Boolean,
    context: Context,
    onBackScreen: () -> Unit,
    onSetting: () -> Unit,
    onShareStreaming: () -> Unit,
    onVolumeClick: () -> Unit,
    streamingPlayer: StreamingPlayer,
    onClickFull: () -> Unit,
    onClickPlayer: () -> Unit,
    startTime: Long,
    viewerCount: Long
) {
    var isVisible by remember { mutableStateOf(true) }  // State to track visibility of controls
    // State for rotation animation
    val rotation by animateFloatAsState(
        targetValue = if (isPlaying) 0f else 360f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "rotation player"
    )
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        delay(5000)
        isVisible = false
    }
    var startTimeString by remember {
        mutableStateOf(OnlineSince.getOnlineSince(startTime))
    }
    // Launch an effect that continuously updates the time
    DisposableEffect(startTime) {
        // Create a coroutine scope for the effect
        val scope = CoroutineScope(Dispatchers.Main + Job())
        // Launch a coroutine that updates the time string every second
        val job = scope.launch {
            while (true) {
                startTimeString = OnlineSince.getOnlineSince(startTime)
                delay(1000L)  // Update every second
            }
        }
        // Clean up when the composable is removed from the composition
        onDispose {
            job.cancel()  // Cancel the coroutine when the composable leaves the composition
        }
    }

    Box(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    isVisible = true
                    coroutineScope.launch {
                        delay(5000)
                        isVisible = false
                    }
                })
            }
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                IconButton(
                    modifier = Modifier
                        .padding(8.dp)
                        .statusBarsPadding()
                        .background(
                            color = GamedgeTheme.colors.lightScrim,
                            shape = CircleShape,
                        ),
                    onClick = onBackScreen
                ) {
                    Icon(
                        painter = painterResource(coreR.drawable.arrow_left),
                        contentDescription = "back screen",
                        tint = Color.White,
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    val iconVolume = if (isVolumeOff) Icons.AutoMirrored.Filled.VolumeOff else
                        Icons.AutoMirrored.Filled.VolumeUp
                    IconButton(
                        modifier = Modifier,
                        onClick = onVolumeClick
                    ) {
                        Icon(
                            imageVector = iconVolume,
                            contentDescription = "volume",
                            tint = Color.White,
                        )
                    }
                    val iconFullScreen = if (isFullscreen) Icons.Default.FullscreenExit else
                        Icons.Default.Fullscreen
                    IconButton(
                        modifier = Modifier,
                        onClick = {
                            onClickFull()
                            if (isFullscreen) {
                                context.setScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                            } else {
                                context.setScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = iconFullScreen,
                            contentDescription = "fullscreen toggle",
                            tint = Color.White
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                ) {
                    IconButton(
                        modifier = Modifier,
                        onClick = onShareStreaming
                    ) {
                        Icon(
                            imageVector = Icons.Default.IosShare,
                            contentDescription = "setting",
                            tint = Color.White,
                        )
                    }
                    IconButton(
                        modifier = Modifier,
                        onClick = onSetting
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "setting",
                            tint = Color.White,
                        )
                    }
                }

                val iconPlayer = if (isPlaying) Icons.Outlined.Pause else Icons.Default.PlayArrow
                IconButton(
                    modifier = Modifier
                        .size(52.dp)
                        .rotate(rotation)
                        .align(Alignment.Center),
                    onClick = {
                        onClickPlayer()
                    }
                ) {
                    Icon(
                        modifier = Modifier.fillMaxSize(),
                        imageVector = iconPlayer,
                        contentDescription = if (isPlaying) "pause" else "play",
                        tint = Color.White,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(12.dp)
                        .wrapContentSize()
                        .align(Alignment.BottomStart)
                        .background(
                            color = GamedgeTheme.colors.lightScrim,
                            shape = GamedgeTheme.shapes.medium,
                        )
                        .padding(8.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = Icons.Default.FiberManualRecord,
                        tint = Color.Red,
                        contentDescription = null
                    )
                    Text(
                        text = startTimeString,
                        modifier = Modifier,
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )

                    Text(
                        text = stringResource(id = coreR.string.count_view, viewerCount.formattedCount()),
                        modifier = Modifier.padding(start = 8.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

            }
        }
    }
}

@Composable
private fun ChatView(modifier: Modifier) {
    LazyColumn(modifier) {
        items(listOf("xinchao", "xinchao", "xinchao")) {
            Text(text = it)
        }
    }
}
