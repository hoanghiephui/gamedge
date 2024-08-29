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
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import coil.imageLoader
import com.game.feature.streaming.component.QualitySelectionDialog
import com.game.feature.streaming.component.StreamingPlayer
import com.game.feature.streaming.component.chat.ChatView
import com.game.feature.streaming.component.rememberStreamingPlayer
import com.game.feature.streaming.entities.AdvancedChatSettings
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteNameUrl
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

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@OptIn(UnstableApi::class)
@Composable
fun StreamingScreen(
    modifier: Modifier = Modifier,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    onBackScreen: () -> Unit,
    viewModel: StreamingViewModel = hiltViewModel(),
    chatSettingsViewModel: ChatSettingsViewModel = hiltViewModel()
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
    val uiStateProfile by viewModel.uiStateProfile.collectAsStateWithLifecycle()

    val configuration = LocalConfiguration.current
    var orientation by remember { mutableIntStateOf(Configuration.ORIENTATION_PORTRAIT) }
    var videoWidthFraction by remember { mutableFloatStateOf(0.67f) } // Initial weight for the video player
    var isPlaying by remember { mutableStateOf(false) }
    var isPlayWhenReadyChanged by remember { mutableStateOf(false) }
    var showDialogSetting by remember { mutableStateOf(false) }
    var selectedGroupIndex by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val startTime = viewModel.streamPlaybackAccessToken.startTime
    val twitchUserChat = viewModel.listChats.toList()
    val outerBottomModalState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val streamPlaybackData = viewModel.streamPlaybackAccessToken
    val density = LocalDensity.current
    val advancedChatSettingsState by viewModel.advancedChatSettingsState
    val openWarningDialog by viewModel.openWarningDialog
    val keyboardController = LocalSoftwareKeyboardController.current
    var isVisibleTitle by remember { mutableStateOf(true) }  // State to track visibility of controls
    val textFieldValue by viewModel.textFieldValue

    LaunchedEffect(configuration) {
        // Save any changes to the orientation value on the configuration object
        snapshotFlow { configuration.orientation }
            .collect { orientation = it }
    }

    LaunchedEffect(viewModel) {
        viewModel.updateChannelNameAndClientIdAndUserId(
            channelName = streamPlaybackData.streamerName,
            clientId = streamPlaybackData.clientId,
            broadcasterId = streamPlaybackData.broadcasterId,
            userId = streamPlaybackData.userId,
            login = streamPlaybackData.appLogin
        )
        viewModel.getBetterTTVChannelEmotes(streamPlaybackData.broadcasterId)
        viewModel.getBetterTTVGlobalEmotes()
    }

    LaunchedEffect(chatSettingsViewModel) {
        chatSettingsViewModel.getGlobalChatBadges()
    }

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
    val showClickedUserBottomModal: () -> Unit = remember {
        {
            showBottomSheet = true
        }
    }
    val showChatSettingsBottomModal: () -> Unit = remember(outerBottomModalState) {
        {
            showBottomSheet = true
        }
    }

    val changeActualTextFieldValue: (String, TextRange) -> Unit = remember(viewModel) {
        { text, textRange ->
            viewModel.changeActualTextFieldValue(text, textRange)
        }
    }

    val sendMessageToWebSocket: (String) -> Unit = remember(viewModel) {
        { message ->
            viewModel.sendMessage(message)
            viewModel.updateMostFrequentEmoteList()
        }
    }

    val updateAdvancedChatSettings: (AdvancedChatSettings) -> Unit = remember(viewModel) {
        { newValue ->
            viewModel.updateAdvancedChatSettings(newValue)
        }
    }

    val setNoChatMode: (Boolean) -> Unit = remember(viewModel) {
        { newValue ->
            viewModel.setNoChatMode(newValue)
        }
    }

    val clickedCommandAutoCompleteText: (String) -> Unit = remember(viewModel) {
        { clickedValue ->
            viewModel.clickedCommandAutoCompleteText(clickedValue)
        }
    }

    val updateTextWithEmote: (String) -> Unit = remember(viewModel) {
        {
            viewModel.addEmoteToText(it)
        }
    }

    val updateMostFrequentEmoteList: (EmoteNameUrl) -> Unit = remember(viewModel) {
        {
            viewModel.updateTemporaryMostFrequentList(it)
        }
    }

    val deleteEmote: () -> Unit = remember(viewModel) {
        {
            viewModel.deleteEmote()
        }
    }

    val updateClickedUser: (String, String, Boolean, Boolean) -> Unit = remember(viewModel) {
        { username, userId, banned, isMod ->
            viewModel.updateClickedChat(
                username,
                userId,
                banned,
                isMod
            )
        }
    }

    KeepScreenOn()
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        when (orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    /*ChatView(
                        modifier = Modifier
                            .fillMaxHeight()
                            .statusBarsPadding()
                            .fillMaxWidth(0.33f) // Chiếm phần còn lại của chiều rộng màn hình
                            .align(Alignment.CenterEnd)
                            .padding(start = 20.dp),
                        twitchUserChat = viewModel.listChats.toList()
                    )*/
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
                        AnimatedVisibility(
                            visible = showLoading,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .wrapContentSize()
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
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
                            viewerCount = viewModel.streamPlaybackAccessToken.viewerCount,
                            onHideTitle = {
                                isVisibleTitle = false
                            },
                            onShowTitle = {
                                isVisibleTitle = true
                            }
                        )
                    }

                }
            }

            Configuration.ORIENTATION_PORTRAIT -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    Column(
                        modifier = modifier
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = modifier
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
                            androidx.compose.animation.AnimatedVisibility(
                                visible = showLoading,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .wrapContentSize()
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .align(Alignment.Center),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
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
                                viewerCount = viewModel.streamPlaybackAccessToken.viewerCount,
                                onHideTitle = {
                                    isVisibleTitle = false
                                },
                                onShowTitle = {
                                    isVisibleTitle = true
                                }
                            )
                        }
                        AnimatedVisibility(
                            modifier = modifier,
                            visible = isVisibleTitle,
                            enter = slideInVertically {
                                // Slide in from 40 dp from the top.
                                with(density) { -40.dp.roundToPx() }
                            } + expandVertically(
                                // Expand from the top.
                                expandFrom = Alignment.Top
                            ) + fadeIn(
                                // Fade in with the initial alpha of 0.3f.
                                initialAlpha = 0.3f
                            ),
                            exit = slideOutVertically() + shrinkVertically() + fadeOut()
                        ) {
                            TitleAndProfile(
                                modifier = modifier,
                                uiStateProfile
                            )
                        }

                        ChatView(
                            modifier = modifier,
                            twitchUserChat = twitchUserChat,
                            showBottomModal = {
                                //showClickedUserBottomModal()
                            },
                            updateClickedUser = { username, userId, banned, isMod ->
                                updateClickedUser(
                                    username,
                                    userId,
                                    banned,
                                    isMod
                                )
                            },
                            showTimeoutDialog = {
                                //streamViewModel.openTimeoutDialog.value = true
                            },
                            showBanDialog = { /*streamViewModel.openBanDialog.value = true*/ },
                            doubleClickMessage = { username ->
                                //doubleClickChat(username)
                            },

                            newFilterMethod = { newTextValue ->
                                //newFilterMethod(newTextValue)
                            },

                            orientationIsVertical = true,

                            isMod = viewModel.state.value.loggedInUserData?.mod ?: false,
                            clickedAutoCompleteText = { username ->
                                viewModel.autoTextChange(username)
                            },
                            showModal = {
                                showChatSettingsBottomModal()
                            },
                            notificationAmount = 0,//modViewViewModel.uiState.value.modViewTotalNotifications,
                            textFieldValue = viewModel.textFieldValue,
                            sendMessageToWebSocket = { message ->
                                sendMessageToWebSocket(message)
                            },
                            noChat = advancedChatSettingsState.noChatMode,
                            deleteChatMessage = { messageId ->
                                //viewModel.deleteChatMessage(messageId)TODO chưa làm tính năng mod
                            },
                            clickedCommandAutoCompleteText = { clickedValue ->
                                clickedCommandAutoCompleteText(clickedValue)

                            },
                            inlineContentMap = viewModel.inlineTextContentTest.value,
                            hideSoftKeyboard = {
                                rememberCoroutineScope.launch {
                                    delay(300)
                                    keyboardController?.hide()
                                }
                            },
                            emoteBoardGlobalList = viewModel.globalEmoteUrlList.value,
                            //todo: this is what I need to change
                            updateTextWithEmote = { newValue -> updateTextWithEmote(newValue) },
                            emoteBoardChannelList = viewModel.channelEmoteUrlList.value,
                            emoteBoardMostFrequentList = viewModel.mostFrequentEmoteListTesting.value,
                            deleteEmote = {
                                deleteEmote() // this needs to be changed
                            },
                            showModView = {
                                //showModView()
                                //clearModViewNotifications()
                            },
                            updateTempararyMostFrequentEmoteList = { value -> updateMostFrequentEmoteList(value) },
                            globalBetterTTVEmotes = viewModel.globalBetterTTVEmotes.value,
                            channelBetterTTVResponse = viewModel.channelBetterTTVEmote.value,
                            sharedBetterTTVResponse = viewModel.sharedChannelBetterTTVEmote.value,
                            userIsSub = viewModel.state.value.loggedInUserData?.sub ?: false,
                            forwardSlashes = viewModel.forwardSlashCommandImmutable.value,
                            filteredChatListImmutable = viewModel.filteredChatListImmutable.value,
                            actualTextFieldValue = textFieldValue,
                            changeActualTextFieldValue = { text, textRange ->
                                changeActualTextFieldValue(text, textRange)
                            },
                            badgeListMap = chatSettingsViewModel.globalChatBadgesMap.value,
                            usernameSize = chatSettingsViewModel.usernameSize.value,
                            messageSize = chatSettingsViewModel.messageSize.value,
                            lineHeight = chatSettingsViewModel.lineHeight.value,
                            useCustomUsernameColors = chatSettingsViewModel.customUsernameColor.value,
                            globalTwitchEmoteContentMap = chatSettingsViewModel.globalEmoteMap.value,
                            channelTwitchEmoteContentMap = chatSettingsViewModel.inlineContentMapChannelEmoteList.value,
                            globalBetterTTVEmoteContentMap = chatSettingsViewModel.betterTTVGlobalInlineContentMapChannelEmoteList.value,
                            channelBetterTTVEmoteContentMap = chatSettingsViewModel.betterTTVChannelInlineContentMapChannelEmoteList.value,
                            sharedBetterTTVEmoteContentMap = chatSettingsViewModel.betterTTVSharedInlineContentMapChannelEmoteList.value,
                            lowPowerMode = viewModel.lowPowerModeActive.value,
                        )
                    }

                    if (showBottomSheet) {
                        OrientationPortraitScreen(
                            sheetState = outerBottomModalState,
                            onDismissRequest = {
                                showBottomSheet = false
                            },
                            advancedChatSettings = advancedChatSettingsState,
                            onChangeAdvancedChatSettings = {
                                updateAdvancedChatSettings(it)
                            },
                            onChangeNoChatMode = {
                                setNoChatMode(it)
                            },
                        )
                    }
                }

            }
        }
    }
}

@Composable
private fun TitleAndProfile(
    modifier: Modifier,
    data: StreamPlaybackAccessToken
) {
    Surface(
        tonalElevation = 2.dp,
        contentColor = MaterialTheme.colorScheme.secondary,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            AsyncImage(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(50.dp)
                    .clip(CircleShape)                       // clip to the circle shape
                    .border(2.dp, Color.Gray, CircleShape),
                model = data.thumbnailProfile,
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
    viewerCount: Long,
    onHideTitle: () -> Unit,
    onShowTitle: () -> Unit,
) {

    // State for rotation animation
    val rotation by animateFloatAsState(
        targetValue = if (isPlaying) 0f else 360f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "rotation player"
    )
    val coroutineScope = rememberCoroutineScope()
    var isVisible by remember { mutableStateOf(true) }  // State to track visibility of controls
    var hasUserInteracted by remember { mutableStateOf(false) }
    var visibilityJob by remember { mutableStateOf<Job?>(null) }
    LaunchedEffect(Unit) {
        delay(5000)
        if (!hasUserInteracted) {
            isVisible = false
            onHideTitle()
        }
    }
    var startTimeString by remember {
        mutableStateOf(OnlineSince.getOnlineSince(startTime))
    }
    // Launch an effect that continuously updates the time
    DisposableEffect(startTime) {
        val scope = CoroutineScope(Dispatchers.Main + Job())
        val job = scope.launch {
            while (true) {
                startTimeString = OnlineSince.getOnlineSince(startTime)
                delay(1000L)  // Update every second
            }
        }
        onDispose {
            job.cancel()
        }
    }

    Box(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        hasUserInteracted = true // Mark that user has interacted
                        visibilityJob?.cancel()
                        if (isVisible) {
                            // Hide title if it is currently visible
                            isVisible = false
                            onHideTitle()
                        } else {
                            // Show title if it is currently hidden
                            isVisible = true
                            onShowTitle()

                            visibilityJob = coroutineScope.launch {
                                delay(5000)
                                isVisible = false
                                onHideTitle()
                            }
                        }
                    }
                )
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
