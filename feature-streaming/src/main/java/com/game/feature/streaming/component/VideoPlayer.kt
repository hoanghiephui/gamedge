package com.game.feature.streaming.component


import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.paulrybitskyi.gamedge.common.domain.live.entities.StreamPlaybackAccessToken
import com.paulrybitskyi.gamedge.common.ui.theme.GamedgeTheme

@OptIn(UnstableApi::class)
@Composable
fun rememberStreamingPlayer(
    streamPlaybackAccessToken: StreamPlaybackAccessToken,
    onVideoDispose: () -> Unit = {},
    onVideoGoBackground: () -> Unit = {},
    onShowProcess: (Boolean) -> Unit,
    onPlayError: (PlaybackException) -> Unit,
    onPlayWhenReadyChanged: (Boolean) -> Unit,
    onIsPlayingChanged: (Boolean) -> Unit,
): StreamingPlayer {
    val context = LocalContext.current
    var thumbnail by remember {
        mutableStateOf<Pair<Bitmap?, Boolean>>(Pair(null, true))  //bitmap, isShow
    }
    var isMuted by remember { mutableStateOf(false) }  // State to track mute status
    val exoPlayer = remember(context) {
        val tracSelector = DefaultTrackSelector(context).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }
        val dataSourceFactory: DataSource.Factory = DefaultHttpDataSource.Factory()
        // Create a HLS media source pointing to a playlist uri.
        val hlsMediaSource =
            HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(streamPlaybackAccessToken.streamUrl))
        ExoPlayer.Builder(context).setTrackSelector(tracSelector).build().also { exoPlayer ->
            exoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_DEFAULT
            exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
            exoPlayer.setMediaSource(hlsMediaSource)
            exoPlayer.playWhenReady = true
            exoPlayer.prepare()
            exoPlayer.addListener(object : Player.Listener {
                override fun onRenderedFirstFrame() {
                    super.onRenderedFirstFrame()
                    thumbnail = thumbnail.copy(second = false)
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    when (playbackState) {
                        Player.STATE_IDLE -> {
                        }

                        Player.STATE_BUFFERING -> {
                            onShowProcess(true)
                        }

                        Player.STATE_READY -> onShowProcess(false)
                        Player.STATE_ENDED -> {
                        }
                    }
                }

                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    super.onPlayWhenReadyChanged(playWhenReady, reason)
                    onPlayWhenReadyChanged(playWhenReady)
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    onIsPlayingChanged(isPlaying)
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    onPlayError(error)
                }
            })
        }
    }
    // Mute/Unmute logic
    DisposableEffect(isMuted) {
        exoPlayer.volume = if (isMuted) 0f else 1f
        onDispose { /* Clean up if needed */ }
    }

    // Get the available quality options and allow user to select
    var availableQualities by remember { mutableStateOf<List<Tracks.Group>>(emptyList()) }

    // Fetch available tracks once the player is ready
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onTracksChanged(tracks: Tracks) {
                val videoGroups = tracks.groups.filter { group ->
                    group.type == C.TRACK_TYPE_VIDEO
                }
                availableQualities = videoGroups
                Log.d("MAKE_PLAYER", "${availableQualities.size}")
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    val lifecycleOwner by rememberUpdatedState(LocalLifecycleOwner.current)
    DisposableEffect(key1 = lifecycleOwner) {
        val lifeCycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    exoPlayer.pause()
                    onVideoGoBackground()
                }

                Lifecycle.Event.ON_START -> exoPlayer.play()
                Lifecycle.Event.ON_DESTROY -> exoPlayer.release()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifeCycleObserver)
        onDispose {
            onVideoDispose()
            thumbnail = thumbnail.copy(second = true)
            lifecycleOwner.lifecycle.removeObserver(lifeCycleObserver)
        }
    }

    val playerView = remember {
        PlayerView(context).apply {
            player = exoPlayer
            useController = false
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
            setShutterBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
            setKeepContentOnPlayerReset(true)
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }
    return remember(
        context,
        thumbnail,
        availableQualities,
        isMuted
    ) {
        StreamingPlayer(
            playerView = playerView,
            exoPlayer = exoPlayer,
            availableQualities = availableQualities,
            isMuted = isMuted,
            toggleMute = { isMuted = !isMuted }
        )
    }
}

data class StreamingPlayer(
    val playerView: PlayerView,
    val exoPlayer: ExoPlayer,
    val availableQualities: List<Tracks.Group>,
    val isMuted: Boolean,
    val toggleMute: (Boolean) -> Unit
)


@OptIn(UnstableApi::class)
@Composable
fun QualitySelectionMenu(
    modifier: Modifier = Modifier,
    availableQualities: List<Tracks.Group>,
    onQualitySelected: (Tracks.Group?, Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedQuality by remember { mutableStateOf("Auto") }

    // State to keep track of the selected quality index
    var selectedGroupIndex by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    Box(modifier = modifier) {
        Button(onClick = { expanded = true }) {
            Text(text = "Quality: $selectedQuality")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // Add "Auto" option
            DropdownMenuItem(
                onClick = {
                    selectedQuality = "Auto"
                    selectedGroupIndex = null
                    expanded = false
                    onQualitySelected(null, -1)
                },
                modifier = Modifier.background(if (selectedGroupIndex == null) Color.LightGray else Color.Transparent),
                text = {
                    Text(text = "Auto")
                }
            )

            availableQualities.forEachIndexed { groupIndex, group ->
                for (i in 0 until group.mediaTrackGroup.length) {
                    val format = group.mediaTrackGroup.getFormat(i)
                    DropdownMenuItem(
                        onClick = {
                            selectedQuality = "${format.height}p"
                            selectedGroupIndex = Pair(groupIndex, i)
                            expanded = false
                            onQualitySelected(group, i)
                        },
                        modifier = Modifier.background(
                            if (selectedGroupIndex == Pair(groupIndex, i)) Color.LightGray else Color.Transparent
                        ),
                        text = {
                            Text(text = "${format.height}p")
                        }
                    )
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun QualitySelectionDialog(
    modifier: Modifier = Modifier,
    availableQualities: List<Tracks.Group>,
    selectedGroupIndex: Pair<Int, Int>?, // Truyền vào trạng thái hiện tại
    onQualitySelected: (Tracks.Group?, Int) -> Unit,
    onDismissRequest: () -> Unit,
) {
    // Sử dụng trạng thái từ selectedGroupIndex nếu có, mặc định là "Auto" nếu chưa có
    var selectedQuality by remember(selectedGroupIndex) {
        mutableStateOf(
            selectedGroupIndex?.let {
                "${availableQualities[it.first].mediaTrackGroup.getFormat(it.second).height}p"
            } ?: "Auto"
        )
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Select Quality") },
        text = {
            Column(
                modifier = modifier
                    .background(Color.Transparent)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                // Add "Auto" option
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedQuality = "Auto"
                            onQualitySelected(null, -1)
                            onDismissRequest()
                        }
                        .background(
                            if (selectedQuality == "Auto") MaterialTheme.colorScheme.inversePrimary else Color.Transparent,
                            shape = GamedgeTheme.shapes.medium
                        ),
                    headlineContent = { Text(text = "Auto") },
                    colors = ListItemDefaults.colors(
                        headlineColor = Color.Unspecified,
                        containerColor = Color.Unspecified
                    ),
                )

                // Available qualities list
                availableQualities.forEachIndexed { groupIndex, group ->
                    for (i in 0 until group.mediaTrackGroup.length) {
                        val format = group.mediaTrackGroup.getFormat(i)
                        val resolution = "${format.height}p"
                        val frameRate = format.frameRate // Assuming frameRate is accessible

                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedQuality = resolution
                                    onQualitySelected(group, i)
                                    onDismissRequest()
                                }
                                .background(
                                    if (selectedGroupIndex == Pair(
                                            groupIndex,
                                            i
                                        )
                                    ) MaterialTheme.colorScheme.inversePrimary else Color.Transparent,
                                    shape = GamedgeTheme.shapes.medium
                                ),
                            headlineContent = { Text(text = resolution) },
                            supportingContent = {
                                Text(text = "Frame rate: ${frameRate.toInt()} FPS")
                            },
                            colors = ListItemDefaults.colors(
                                headlineColor = Color.Unspecified,
                                containerColor = Color.Unspecified
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("OK")
            }
        }
    )
}





