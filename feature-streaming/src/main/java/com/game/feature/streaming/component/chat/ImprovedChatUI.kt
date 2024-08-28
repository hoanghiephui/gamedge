package com.game.feature.streaming.component.chat

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ExpandCircleDown
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.imageLoader
import com.android.model.IndivBetterTTVEmote
import com.android.model.websockets.MessageType
import com.game.feature.streaming.entities.FilteredChatListImmutableCollection
import com.game.feature.streaming.entities.ForwardSlashCommandsImmutableCollection
import com.paulrybitskyi.gamedge.common.api.BuildConfig
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteListMap
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteNameUrl
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteNameUrlEmoteType
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteNameUrlEmoteTypeList
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteNameUrlList
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteTypes
import com.paulrybitskyi.gamedge.common.domain.chat.IndivBetterTTVEmoteList
import com.paulrybitskyi.gamedge.common.domain.websockets.MessageToken
import com.paulrybitskyi.gamedge.common.domain.websockets.PrivateMessageType
import com.paulrybitskyi.gamedge.common.domain.websockets.TwitchUserData
import com.paulrybitskyi.gamedge.common.ui.rememberDraggableActions
import com.paulrybitskyi.gamedge.common.ui.v2.component.NiaTab
import com.paulrybitskyi.gamedge.common.ui.v2.component.NiaTabRow
import com.paulrybitskyi.gamedge.common.ui.widgets.InputSelectorButton
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.paulrybitskyi.gamedge.core.R as coreR

@Composable
fun ChatView(
    modifier: Modifier,
    twitchUserChat: List<TwitchUserData>,
    showBottomModal: () -> Unit,
    updateClickedUser: (String, String, Boolean, Boolean) -> Unit,
    showTimeoutDialog: () -> Unit,
    showBanDialog: () -> Unit,
    doubleClickMessage: (String) -> Unit,


    //below is what is needed for the chat UI
    textFieldValue: MutableState<TextFieldValue>,
    clickedAutoCompleteText: (String) -> Unit,
    isMod: Boolean,
    sendMessageToWebSocket: (String) -> Unit,
    showModal: () -> Unit,
    newFilterMethod: (TextFieldValue) -> Unit,
    orientationIsVertical: Boolean,
    notificationAmount: Int,
    noChat: Boolean,
    deleteChatMessage: (String) -> Unit,
    clickedCommandAutoCompleteText: (String) -> Unit,
    inlineContentMap: EmoteListMap,
    hideSoftKeyboard: () -> Unit,
    emoteBoardGlobalList: EmoteNameUrlList,
    badgeListMap: EmoteListMap,
    emoteBoardChannelList: EmoteNameUrlEmoteTypeList,
    emoteBoardMostFrequentList: EmoteNameUrlList,
    globalBetterTTVEmotes: IndivBetterTTVEmoteList,
    channelBetterTTVResponse: IndivBetterTTVEmoteList,
    sharedBetterTTVResponse: IndivBetterTTVEmoteList,
    updateTempararyMostFrequentEmoteList: (EmoteNameUrl) -> Unit,
    updateTextWithEmote: (String) -> Unit,
    deleteEmote: () -> Unit,
    showModView: () -> Unit,
    userIsSub: Boolean,
    forwardSlashes: ForwardSlashCommandsImmutableCollection,
    filteredChatListImmutable: FilteredChatListImmutableCollection,

    actualTextFieldValue: TextFieldValue,
    changeActualTextFieldValue: (String, TextRange) -> Unit,
    usernameSize: Float,
    messageSize: Float,
    lineHeight: Float,
    useCustomUsernameColors: Boolean,
    globalTwitchEmoteContentMap: EmoteListMap,
    channelTwitchEmoteContentMap: EmoteListMap,
    globalBetterTTVEmoteContentMap: EmoteListMap,
    channelBetterTTVEmoteContentMap: EmoteListMap,
    sharedBetterTTVEmoteContentMap: EmoteListMap,
    lowPowerMode: Boolean,
) {
    val lazyColumnListState = rememberLazyListState()
    var autoscroll by remember { mutableStateOf(true) }
    val emoteKeyBoardHeight = remember { mutableStateOf(0.dp) }
    var iconClicked by remember { mutableStateOf(false) }

    ChatUIBox(
        modifier = modifier,
        determineScrollState = {
            DetermineScrollState(
                lazyColumnListState = lazyColumnListState,
                setAutoScrollFalse = { autoscroll = false },
                setAutoScrollTrue = { autoscroll = true },
            )
        },
        chatUI = { modifierChatUI ->
            Box(modifier = modifierChatUI.fillMaxSize()) {
                ChatUILazyColumn(
                    lazyColumnListState = lazyColumnListState,
                    twitchUserChat = twitchUserChat,
                    autoscroll = autoscroll,
                    showBottomModal = { showBottomModal() },
                    showTimeoutDialog = { showTimeoutDialog() },
                    showBanDialog = { showBanDialog() },
                    updateClickedUser = { username, userId, isBanned, isMod ->
                        updateClickedUser(
                            username,
                            userId,
                            isBanned,
                            isMod
                        )
                    },
                    doubleClickMessage = { username -> doubleClickMessage(username) },
                    modifier = modifierChatUI,
                    deleteChatMessage = { messageId -> deleteChatMessage(messageId) },
                    isMod = isMod,
                    inlineContentMap = inlineContentMap,
                    badgeListMap = badgeListMap,
                    usernameSize = usernameSize,
                    messageSize = messageSize,
                    lineHeight = lineHeight,
                    useCustomUsernameColors = useCustomUsernameColors,
                    globalTwitchEmoteContentMap = globalTwitchEmoteContentMap,
                    channelTwitchEmoteContentMap = channelTwitchEmoteContentMap,
                    globalBetterTTVEmoteContentMap = globalBetterTTVEmoteContentMap,
                    channelBetterTTVEmoteContentMap = channelBetterTTVEmoteContentMap,
                    sharedBetterTTVEmoteContentMap = sharedBetterTTVEmoteContentMap

                )
                if (emoteKeyBoardHeight.value == 350.dp) {
                    //fix bug
                } else {
                    JumpToBottom(
                        // Only show if the scroller is not at the bottom
                        enabled = !autoscroll,
                        onClicked = {
                            autoscroll = true
                        },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        },
        enterChat = { enterChatModifier ->
            EnterChatColumn(
                modifier = enterChatModifier,
                filteredRow = {
                    FilteredMentionLazyRow(
                        filteredChatListImmutable = filteredChatListImmutable,
                        clickedAutoCompleteText = { username ->
                            clickedAutoCompleteText(
                                username
                            )
                        }
                    )
                },
                showModStatus = {
                    /*ShowModStatus(
                        modStatus = isMod,
                        orientationIsVertical = orientationIsVertical,
                        notificationAmount = notificationAmount,
                        showModView = {
                            showModView()
                        }
                    )*/
                },
                stylizedTextField = { boxModifier ->
                    StylizedTextField(
                        modifier = boxModifier,
                        newFilterMethod = { newTextValue ->
                            newFilterMethod(newTextValue)
                        },
                        showEmoteBoard = {
                            hideSoftKeyboard()
                            emoteKeyBoardHeight.value = 350.dp
                        },
                        showKeyBoard = {
                            emoteKeyBoardHeight.value = 0.dp
                        },
                        iconClicked = iconClicked,
                        setIconClicked = { newValue -> iconClicked = newValue },

                        actualTextFieldValue = actualTextFieldValue,
                        changeActualTextFieldValue = { text, textRange ->
                            changeActualTextFieldValue(text, textRange)
                        },
                    )
                },
                showIconBasedOnTextLength = {
                    ShowIconBasedOnTextLength(
                        textFieldValue = textFieldValue,
                        chat = { item ->
                            hideSoftKeyboard()
                            //this is to close the emote board
                            emoteKeyBoardHeight.value = 0.dp
                            iconClicked = false
                            sendMessageToWebSocket(item)
                        },
                        showModal = {
                            showModal()
                        },
                    )
                },
            )
        },
        noChat = noChat,
        clickedCommandAutoCompleteText = { clickedValue -> clickedCommandAutoCompleteText(clickedValue) },
        emoteKeyBoardHeight = emoteKeyBoardHeight.value,
        emoteBoardGlobalList = emoteBoardGlobalList,
        updateTextWithEmote = { newValue -> updateTextWithEmote(newValue) },
        emoteBoardChannelList = emoteBoardChannelList,
        emoteBoardMostFrequentList = emoteBoardMostFrequentList,
        closeEmoteBoard = {
            emoteKeyBoardHeight.value = 0.dp
            iconClicked = false
        },
        deleteEmote = { deleteEmote() },
        updateTempararyMostFrequentEmoteList = { value -> updateTempararyMostFrequentEmoteList(value) },
        globalBetterTTVEmotes = globalBetterTTVEmotes,
        channelBetterTTVResponse = channelBetterTTVResponse,
        sharedBetterTTVResponse = sharedBetterTTVResponse,
        userIsSub = userIsSub,
        forwardSlashes = forwardSlashes,
        lowPowerMode = lowPowerMode
    )
}

/**
 * - restartable
 * - NOT skippable
 * */
@Composable
fun ChatUIBox(
    modifier: Modifier,
    determineScrollState: @Composable ImprovedChatUI.() -> Unit,
    chatUI: @Composable ImprovedChatUI.(Modifier) -> Unit,
    enterChat: @Composable ImprovedChatUI.(Modifier) -> Unit,
    noChat: Boolean,
    lowPowerMode: Boolean,
    clickedCommandAutoCompleteText: (String) -> Unit,
    emoteKeyBoardHeight: Dp,
    emoteBoardGlobalList: EmoteNameUrlList,
    emoteBoardChannelList: EmoteNameUrlEmoteTypeList,
    emoteBoardMostFrequentList: EmoteNameUrlList,
    globalBetterTTVEmotes: IndivBetterTTVEmoteList,
    channelBetterTTVResponse: IndivBetterTTVEmoteList,
    sharedBetterTTVResponse: IndivBetterTTVEmoteList,
    updateTempararyMostFrequentEmoteList: (EmoteNameUrl) -> Unit,
    updateTextWithEmote: (String) -> Unit,
    closeEmoteBoard: () -> Unit,
    deleteEmote: () -> Unit,
    userIsSub: Boolean,
    forwardSlashes: ForwardSlashCommandsImmutableCollection,
) {
    val titleFontSize = MaterialTheme.typography.headlineMedium.fontSize
    val messageFontSize = MaterialTheme.typography.headlineSmall.fontSize
    val chatScope = remember { ChatScope(titleFontSize, messageFontSize) }
    val chatUIScope = remember { ImprovedChatUI() }


    //todo: add a conditional to show emoteBoard to help with recomps

    if (lowPowerMode) {
        with(chatScope) {
            NoticeMessages(
                systemMessage = "",
                message = "Low power mode: ACTIVE"
            )
        }
    } else {
        with(chatUIScope) {
            val modifierChatUI = if (emoteKeyBoardHeight == 350.dp) {
                modifier.fillMaxWidth()
            } else {
                modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            }
            Box(modifier = modifier.fillMaxSize()) {
                Column(modifier.fillMaxSize()) {
                    chatUI(modifier.weight(1f))//TODO
                    enterChat(modifierChatUI)

                    if (emoteKeyBoardHeight == 350.dp) {
                        EmoteBoard(
                            modifier = modifier
                                .zIndex(8f),
                            emoteBoardGlobalList,
                            emoteBoardMostFrequentList = emoteBoardMostFrequentList,
                            updateTextWithEmote = { newValue ->
                                updateTextWithEmote(newValue)
                            },
                            emoteBoardChannelList = emoteBoardChannelList,
                            closeEmoteBoard = {
                                closeEmoteBoard()
                            },
                            deleteEmote = {
                                deleteEmote()
                            },
                            updateTempararyMostFrequentEmoteList = { value ->
                                updateTempararyMostFrequentEmoteList(value)
                            },
                            globalBetterTTVEmotes = globalBetterTTVEmotes,
                            channelBetterTTVResponse = channelBetterTTVResponse,
                            sharedBetterTTVResponse = sharedBetterTTVResponse,
                            userIsSub = userIsSub,

                            )
                    }
                }
                determineScrollState()
                if (noChat) {
                    with(chatScope) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "You are in No Chat mode",
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 30.dp)
                            )
                        }
                    }

                }

                ForwardSlash(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 60.dp),
                    forwardSlashes = forwardSlashes,
                    clickedCommandAutoCompleteText = { clickedValue ->
                        clickedCommandAutoCompleteText(clickedValue)
                    }
                )
            }
        }
    }

}

fun LazyGridScope.header(
    content: @Composable LazyGridItemScope.() -> Unit
) {
    item(span = { GridItemSpan(this.maxLineSpan) }, content = content)
}

/**
 * - restartable
 * - skippable
 * */
@Composable
fun EmoteBoard(
    modifier: Modifier,
    emoteBoardGlobalList: EmoteNameUrlList,
    emoteBoardChannelList: EmoteNameUrlEmoteTypeList,
    emoteBoardMostFrequentList: EmoteNameUrlList,
    updateTempararyMostFrequentEmoteList: (EmoteNameUrl) -> Unit,
    globalBetterTTVEmotes: IndivBetterTTVEmoteList,
    channelBetterTTVResponse: IndivBetterTTVEmoteList,
    sharedBetterTTVResponse: IndivBetterTTVEmoteList,
    updateTextWithEmote: (String) -> Unit,
    closeEmoteBoard: () -> Unit,
    deleteEmote: () -> Unit,
    userIsSub: Boolean,

    ) {
    Log.d("FlowRowSimpleUsageExampleClicked", "EmoteBoard recomp")
    val lazyGridState = rememberLazyGridState()
    val betterTTVLazyGridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val titles = listOf("Twitch", "BetterTTV")
    val pagerState = rememberPagerState(pageCount = {
        titles.size
    })
    var currentInputSelector by rememberSaveable {
        val currentInputSelector = if (emoteBoardMostFrequentList.list.isNotEmpty()) {
            InputSelector.RECENT
        } else if (emoteBoardChannelList.list.isNotEmpty()) {
            InputSelector.CHANNEL
        } else {
            InputSelector.GLOBAL
        }
        mutableStateOf(currentInputSelector)
    }
    var currentInputGIFSelector by rememberSaveable {
        val currentInputGIFSelector = if (sharedBetterTTVResponse.list.isNotEmpty()) {
            InputGIFSelector.SHARE
        } else if (channelBetterTTVResponse.list.isNotEmpty()) {
            InputGIFSelector.CHANNEL
        } else {
            InputGIFSelector.GLOBAL
        }
        mutableStateOf(currentInputGIFSelector)
    }
    val showBottomBetter = globalBetterTTVEmotes.list.isNotEmpty() || channelBetterTTVResponse.list.isNotEmpty()
            || sharedBetterTTVResponse.list.isNotEmpty()

    Surface(
        modifier = Modifier
            .wrapContentSize()
            .navigationBarsPadding(),
        tonalElevation = 2.dp,
        contentColor = MaterialTheme.colorScheme.secondary
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
        ) {
            NiaTabRow(selectedTabIndex = selectedTabIndex) {
                titles.forEachIndexed { index, title ->
                    NiaTab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            if (index == 0) {
                                scope.launch {
                                    pagerState.animateScrollToPage(0)
                                }
                            } else {
                                scope.launch {
                                    pagerState.animateScrollToPage(1)
                                }
                            }
                        },
                        text = { Text(text = title) },
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.wrapContentSize(),
                userScrollEnabled = false
            ) { page ->
                // Our page content
                when (page) {
                    0 -> {
                        Column(
                            modifier = modifier
                        ) {
                            Box {
                                LazyGridEmotes(
                                    lazyGridState = lazyGridState,
                                    emoteBoardGlobalList = emoteBoardGlobalList,
                                    emoteBoardChannelList = emoteBoardChannelList,
                                    emoteBoardMostFrequentList = emoteBoardMostFrequentList,
                                    updateTextWithEmote = { emoteValue ->
                                        updateTextWithEmote(
                                            emoteValue
                                        )
                                    },
                                    updateTempararyMostFrequentEmoteList = { value ->
                                        updateTempararyMostFrequentEmoteList(value)
                                    },
                                    modifier = Modifier.padding(bottom = 72.dp),
                                    userIsSub = userIsSub
                                )
                                UserInputSelector(
                                    modifier = Modifier.align(Alignment.BottomCenter),
                                    onSelectorChange = {
                                        currentInputSelector = it
                                        when (it) {
                                            InputSelector.RECENT -> {
                                                scope.launch {
                                                    lazyGridState.scrollToItem(0)
                                                }
                                            }

                                            InputSelector.CHANNEL -> {
                                                scope.launch {
                                                    lazyGridState.scrollToItem(emoteBoardMostFrequentList.list.size + 1)
                                                }
                                            }

                                            InputSelector.GLOBAL -> {
                                                scope.launch {
                                                    lazyGridState.scrollToItem((emoteBoardChannelList.list.size + emoteBoardMostFrequentList.list.size + 1))
                                                }
                                            }

                                            InputSelector.NONE -> Unit
                                        }
                                    },
                                    currentInputSelector = currentInputSelector,
                                    onDeleteMessage = {
                                        deleteEmote()
                                    },
                                    closeEmoteBoard = closeEmoteBoard
                                )
                            }
                        }

                    }

                    1 -> {
                        if (showBottomBetter) {
                            Box {
                                BetterTTVEmoteBoard(
                                    globalBetterTTVResponse = globalBetterTTVEmotes,
                                    updateTextWithEmote = { value -> updateTextWithEmote(value) },
                                    channelBetterTTVResponse = channelBetterTTVResponse,
                                    sharedBetterTTVResponse = sharedBetterTTVResponse,
                                    betterTTVLazyGridState = betterTTVLazyGridState,
                                    modifier = Modifier.padding(bottom = 72.dp)
                                )
                                BetterTTVEmoteBottomUI(
                                    closeEmoteBoard = { closeEmoteBoard() },
                                    deleteEmote = { deleteEmote() },
                                    onSelectorChange = {
                                        currentInputGIFSelector = it
                                        when (it) {
                                            InputGIFSelector.SHARE -> {
                                                scope.launch {
                                                    betterTTVLazyGridState.scrollToItem(emoteBoardMostFrequentList.list.size + 1)
                                                }
                                            }

                                            InputGIFSelector.CHANNEL -> {
                                                scope.launch {
                                                    betterTTVLazyGridState.scrollToItem(emoteBoardMostFrequentList.list.size + 1)
                                                }
                                            }

                                            InputGIFSelector.GLOBAL -> {
                                                scope.launch {
                                                    scope.launch {
                                                        betterTTVLazyGridState.scrollToItem(channelBetterTTVResponse.list.size + 2 + emoteBoardMostFrequentList.list.size + sharedBetterTTVResponse.list.size)
                                                    }
                                                }
                                            }

                                            InputGIFSelector.NONE -> Unit
                                        }
                                    },
                                    currentInputSelector = currentInputGIFSelector,
                                    modifier = Modifier.align(Alignment.BottomCenter)
                                )

                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                            ) {
                                Text(
                                    text = "Not available at this stream",
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }

            }
            /****END OF THE HORIZONTAL PAGER****/
        }
    }


}

/**
 * - restartable
 * - skippable
 * */
@Composable
fun BetterTTVEmoteBoard(
    globalBetterTTVResponse: IndivBetterTTVEmoteList,
    channelBetterTTVResponse: IndivBetterTTVEmoteList,
    sharedBetterTTVResponse: IndivBetterTTVEmoteList,
    betterTTVLazyGridState: LazyGridState,
    updateTextWithEmote: (String) -> Unit,
    modifier: Modifier

) {
    Log.d("BetterTTVEmoteBoardRELOAD", "RELOAD")
    LazyVerticalGrid(
        state = betterTTVLazyGridState,
        columns = GridCells.Adaptive(minSize = 50.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp)
            .height(250.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {

        /*****************************START OF THE CHANNEL EMOTES*******************************/
        if (channelBetterTTVResponse.list.isNotEmpty()) {
            header {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp)
                ) {
                    Spacer(modifier = Modifier.padding(5.dp))
                    Text(
                        "Channel Emotes",
                    )
                    Spacer(modifier = Modifier.padding(2.dp))
                    HorizontalDivider(
                        thickness = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.padding(5.dp))
                }

            }

            items(channelBetterTTVResponse.list) {
                EmoteGif(it, updateTextWithEmote)
            }
        }

        /*****************************START OF THE SHARED CHANNEL EMOTES*******************************/
        if (sharedBetterTTVResponse.list.isNotEmpty()) {
            header {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp)
                ) {
                    Spacer(modifier = Modifier.padding(5.dp))
                    Text(
                        "Shared Emotes",
                    ) // or any composable for your single row
                    Spacer(modifier = Modifier.padding(2.dp))
                    HorizontalDivider(
                        thickness = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.padding(5.dp))
                }

            }

            items(sharedBetterTTVResponse.list) {
                EmoteGif(it, updateTextWithEmote)
            }
        }

        /*****************************START OF THE GLOBAL EMOTES*******************************/
        if (globalBetterTTVResponse.list.isNotEmpty()) {
            header {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp)
                ) {
                    Spacer(modifier = Modifier.padding(5.dp))
                    Text(
                        "Global Emotes"
                    ) // or any composable for your single row
                    Spacer(modifier = Modifier.padding(2.dp))
                    HorizontalDivider(
                        thickness = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.padding(5.dp))
                }

            }

            items(globalBetterTTVResponse.list) {
                EmoteGif(it, updateTextWithEmote)
            }
        }
    }
}

@Composable
private fun EmoteGif(
    it: IndivBetterTTVEmote,
    updateTextWithEmote: (String) -> Unit
) {
    Surface(
        modifier = Modifier.size(50.dp),
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        contentColor = MaterialTheme.colorScheme.secondary,
        shape = RoundedCornerShape(8.dp)
    ) {
        GifLoadingAnimation(
            modifier = Modifier
                .size(50.dp)
                .padding(5.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp)),
            url = "https://cdn.betterttv.net/emote/${it.id}/2x",
            contentDescription = "${it.code} emote",
            emoteName = it.code,
            updateTextWithEmote = { value ->
                updateTextWithEmote(value)
            }
        )
    }
}


/**
 * - restartable
 * - skippable
 * */
@Composable
fun GifLoadingAnimation(
    modifier: Modifier,
    url: String,
    contentDescription: String,
    emoteName: String,
    updateTextWithEmote: (String) -> Unit

) {
    val context = LocalContext.current
    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        imageLoader = context.imageLoader,
        modifier =
        modifier
            .clickable {
                updateTextWithEmote(emoteName)
            }
    )
}

@Composable
private fun UserInputSelector(
    closeEmoteBoard: () -> Unit,
    onSelectorChange: (InputSelector) -> Unit,
    currentInputSelector: InputSelector,
    onDeleteMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(72.dp)
            .wrapContentHeight()
            .padding(start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InputSelectorButton(
            onClick = closeEmoteBoard,
            icon = Icons.Outlined.ExpandCircleDown,
            selected = false,
            description = ""
        )
        Spacer(modifier = Modifier.width(8.dp))

        InputSelectorButton(
            onClick = { onSelectorChange(InputSelector.RECENT) },
            icon = Icons.Outlined.History,
            selected = currentInputSelector == InputSelector.RECENT,
            description = ""
        )
        InputSelectorButton(
            onClick = { onSelectorChange(InputSelector.CHANNEL) },
            icon = Icons.Outlined.LiveTv,
            selected = currentInputSelector == InputSelector.CHANNEL,
            description = ""
        )
        InputSelectorButton(
            onClick = { onSelectorChange(InputSelector.GLOBAL) },
            icon = Icons.Outlined.Explore,
            selected = currentInputSelector == InputSelector.GLOBAL,
            description = ""
        )

        val border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.weight(1f))

        val disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

        val buttonColors = ButtonDefaults.buttonColors(
            disabledContainerColor = Color.Transparent,
            disabledContentColor = disabledContentColor
        )

        // Send button
        Button(
            modifier = Modifier.height(36.dp),
            onClick = onDeleteMessage,
            colors = buttonColors,
            border = border,
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                modifier = Modifier.size(25.dp),
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "click to delete emote"
            )
        }
    }
}

/**
 * - restartable
 * - skippable
 * */
@Composable
fun EmoteBottomUI(
    closeEmoteBoard: () -> Unit,
    deleteEmote: () -> Unit,
    scrollToGlobalEmotes: () -> Unit,
    scrollToChannelEmotes: () -> Unit,
    modifier: Modifier,
    scrollToMostFrequentlyUsedEmotes: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Log.d("EmoteBottomUIRedererd", "RENDERED")

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier = Modifier
                    .size(30.dp)
                    .clickable {
                        closeEmoteBoard()
                    },
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "click to close keyboard emote"
            )
            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                modifier = Modifier
                    .size(25.dp)
                    .clickable {
                        Log.d("EmoteBottomUI", "RECENT")
                        scrollToMostFrequentlyUsedEmotes()
                    },
                imageVector = Icons.Default.Autorenew,
                contentDescription = "click to scroll to most recent emotes"
            )
            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                modifier = Modifier
                    .size(25.dp)
                    .clickable {
                        scrollToChannelEmotes()
                        Log.d("EmoteBottomUI", "CHANNEL")
                    },
                imageVector = Icons.Default.LiveTv,
                contentDescription = "click to scroll to channel emotes"
            )
            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                modifier = Modifier
                    .size(25.dp)
                    .clickable {
                        scrollToGlobalEmotes()
                        Log.d("EmoteBottomUI", "GLOBAL")
                    },
                imageVector = Icons.Default.Explore,
                contentDescription = "click to scroll to gloabl emotes"
            )


        }
        IconButton(
            modifier = Modifier
                .padding(vertical = 5.dp, horizontal = 10.dp),
            onClick = {
                deleteEmote()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        ) {
            Icon(
                modifier = Modifier.size(25.dp),
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "click to delete emote"
            )
        }
    }
}

/**
 * - restartable
 * - skippable
 * */
@Composable
fun BetterTTVEmoteBottomUI(
    closeEmoteBoard: () -> Unit,
    deleteEmote: () -> Unit,
    onSelectorChange: (InputGIFSelector) -> Unit,
    currentInputSelector: InputGIFSelector,
    modifier: Modifier,
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = modifier
            .height(72.dp)
            .wrapContentHeight()
            .padding(start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InputSelectorButton(
            onClick = closeEmoteBoard,
            icon = Icons.Outlined.ExpandCircleDown,
            selected = false,
            description = ""
        )
        Spacer(modifier = Modifier.width(8.dp))
        InputSelectorButton(
            onClick = { onSelectorChange(InputGIFSelector.CHANNEL) },
            icon = Icons.Outlined.LiveTv,
            selected = currentInputSelector == InputGIFSelector.CHANNEL,
            description = ""
        )
        InputSelectorButton(
            onClick = { onSelectorChange(InputGIFSelector.SHARE) },
            icon = Icons.Outlined.Groups,
            selected = currentInputSelector == InputGIFSelector.SHARE,
            description = ""
        )
        InputSelectorButton(
            onClick = { onSelectorChange(InputGIFSelector.GLOBAL) },
            icon = Icons.Outlined.Explore,
            selected = currentInputSelector == InputGIFSelector.GLOBAL,
            description = ""
        )

        val border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.weight(1f))

        val disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

        val buttonColors = ButtonDefaults.buttonColors(
            disabledContainerColor = Color.Transparent,
            disabledContentColor = disabledContentColor
        )

        // Send button
        Button(
            modifier = Modifier.height(36.dp),
            onClick = {
                deleteEmote()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
            colors = buttonColors,
            border = border,
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                modifier = Modifier.size(25.dp),
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "click to delete emote"
            )
        }
    }
}


/**
 * - restartable
 * - skippable
 * */
@Composable
fun LazyGridEmotes(
    emoteBoardGlobalList: EmoteNameUrlList,
    emoteBoardChannelList: EmoteNameUrlEmoteTypeList,
    emoteBoardMostFrequentList: EmoteNameUrlList,
    updateTempararyMostFrequentEmoteList: (EmoteNameUrl) -> Unit,
    updateTextWithEmote: (String) -> Unit,
    lazyGridState: LazyGridState,
    userIsSub: Boolean,
    modifier: Modifier,
) {
    LazyVerticalGrid(
        state = lazyGridState,
        columns = GridCells.Adaptive(minSize = 50.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp)
            .height(250.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        /*****************************START OF THE Most Recent EMOTES*******************************/
        if (emoteBoardMostFrequentList.list.isNotEmpty()) {
            header {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp)
                ) {
                    Spacer(modifier = Modifier.padding(5.dp))
                    Text(
                        "Frequently Used Emotes",
                    ) // or any composable for your single row
                    Spacer(modifier = Modifier.padding(2.dp))
                    HorizontalDivider(
                        thickness = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.padding(5.dp))
                }

            }

            items(
                items = emoteBoardMostFrequentList.list,
                contentType = { "Recent" }
            ) {
                Surface(
                    modifier = Modifier.size(50.dp),
                    tonalElevation = 2.dp,
                    shadowElevation = 2.dp,
                    contentColor = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    AsyncImage(
                        model = it.url,
                        contentDescription = it.name,
                        modifier = Modifier
                            .size(50.dp)
                            .padding(5.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp))
                            .clickable {
                                updateTextWithEmote(it.name)
                            }
                    )
                }
            }
        }

        /*****************************START OF THE Channel EMOTES*******************************/
        if (emoteBoardChannelList.list.isNotEmpty()) {
            header {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp)
                ) {
                    Spacer(modifier = Modifier.padding(5.dp))
                    Text(
                        "Channel Emotes",
                    ) // or any composable for your single row
                    Spacer(modifier = Modifier.padding(2.dp))
                    HorizontalDivider(
                        thickness = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.padding(5.dp))
                }

            }

            items(
                items = emoteBoardChannelList.list,
                key = EmoteNameUrlEmoteType::id,
                contentType = {"Channel"}
            ) {
                if (it.emoteType == EmoteTypes.SUBS && !userIsSub) {
                    Surface(
                        modifier = Modifier.size(50.dp),
                        shadowElevation = 2.dp,
                        contentColor = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            AsyncImage(
                                model = it.url,
                                contentDescription = it.name,
                                modifier = Modifier
                                    .size(50.dp)
                                    .padding(5.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp))
                            )
                            Spacer(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(
                                        Color.Black.copy(0.4f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .align(Alignment.Center)
                            )
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Emote locked. Subscribe to access",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(
                                        end = 6.dp,
                                        bottom = 5.dp
                                    )
                                    .size(18.dp)
                                    .align(Alignment.BottomEnd),
                            )
                        }
                    }

                } else {
                    Surface(
                        modifier = Modifier.size(50.dp),
                        tonalElevation = 2.dp,
                        shadowElevation = 2.dp,
                        contentColor = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        AsyncImage(
                            model = it.url,
                            contentDescription = it.name,
                            modifier = Modifier
                                .size(50.dp)
                                .padding(5.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp))
                                .clickable {
                                    updateTextWithEmote(it.name)
                                    updateTempararyMostFrequentEmoteList(
                                        EmoteNameUrl(it.id, it.name, it.url)
                                    )
                                }
                        )
                    }
                }
            }
        }

        /*****************************START OF THE GLOBAL EMOTES*******************************/
        if (emoteBoardGlobalList.list.isNotEmpty()) {
            header {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp)
                ) {
                    Spacer(modifier = Modifier.padding(5.dp))
                    Text(
                        "Global Emotes",
                    ) // or any composable for your single row
                    Spacer(modifier = Modifier.padding(2.dp))
                    HorizontalDivider(
                        thickness = 2.dp,
                    )
                    Spacer(modifier = Modifier.padding(5.dp))
                }

            }

            items(
                items = emoteBoardGlobalList.list,
                key = EmoteNameUrl::id,
                contentType = {"GLOBAL"}
            ) {
                Surface(
                    modifier = Modifier.size(50.dp),
                    tonalElevation = 2.dp,
                    shadowElevation = 2.dp,
                    contentColor = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    AsyncImage(
                        model = it.url,
                        contentDescription = it.name,
                        modifier = Modifier
                            .size(50.dp)
                            .padding(5.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp))
                            .clickable {
                                updateTextWithEmote(it.name)
                                updateTempararyMostFrequentEmoteList(
                                    EmoteNameUrl(it.id, it.name, it.url)
                                )
                            }
                    )
                }
            }
        }
    }
}

/********END OF LazyGridEmotes**********/


@Stable
class ImprovedChatUI {

    /**
     * - restartable
     * - skippable
     * */
    @Composable
    fun DetermineScrollState(
        lazyColumnListState: LazyListState,
        setAutoScrollFalse: () -> Unit,
        setAutoScrollTrue: () -> Unit,
    ) {
        val interactionSource = lazyColumnListState.interactionSource
        val endOfListReached by remember {
            derivedStateOf {
                lazyColumnListState.isScrolledToEnd()
            }
        }

        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is DragInteraction.Start -> {
                        setAutoScrollFalse()
                    }

                    is PressInteraction.Press -> {
                        setAutoScrollFalse()
                    }
                }
            }
        }

        // observer when reached end of list
        LaunchedEffect(endOfListReached) {
            // do your stuff
            if (endOfListReached) {
                setAutoScrollTrue()
            }
        }

    }


    /**
     * - restartable
     * */
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun ChatUILazyColumn(
        lazyColumnListState: LazyListState,
        twitchUserChat: List<TwitchUserData>,
        autoscroll: Boolean,
        showBottomModal: () -> Unit,
        showTimeoutDialog: () -> Unit,
        showBanDialog: () -> Unit,
        updateClickedUser: (String, String, Boolean, Boolean) -> Unit,
        doubleClickMessage: (String) -> Unit,
        deleteChatMessage: (String) -> Unit,
        modifier: Modifier,
        isMod: Boolean,
        inlineContentMap: EmoteListMap,
        globalTwitchEmoteContentMap: EmoteListMap,
        channelTwitchEmoteContentMap: EmoteListMap,
        globalBetterTTVEmoteContentMap: EmoteListMap,
        channelBetterTTVEmoteContentMap: EmoteListMap,
        sharedBetterTTVEmoteContentMap: EmoteListMap,
        badgeListMap: EmoteListMap,
        fullMode: Boolean = false,
        setDragging: () -> Unit = {},
        usernameSize: Float,
        messageSize: Float,
        lineHeight: Float,
        useCustomUsernameColors: Boolean

    ) {
        val twitchUserChatFilter = remember(twitchUserChat) {
            twitchUserChat.toImmutableList().filterNotNull()
        }
        val coroutineScope = rememberCoroutineScope()
        LazyColumn(
            modifier = modifier,
            state = lazyColumnListState
        ) {
            coroutineScope.launch {
                if (autoscroll) {
                    lazyColumnListState.scrollToItem(twitchUserChatFilter.size)
                }
            }
            if (fullMode) {
                stickyHeader {
                    Text(
                        "Chat",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onDoubleClick = {
                                    setDragging()
                                },
                                onClick = {}
                            )
                            .padding(horizontal = 10.dp)
                    )
                }
            }
            items(
                items = twitchUserChatFilter,
            ) { indivChatMessage ->
                Log.d("CLickingCardCheck", "${indivChatMessage.userType}")
                ChatMessages(
                    indivChatMessage,
                    showBottomModal = {
                        showBottomModal()
                    },
                    updateClickedUser = { username, userId, isBanned, isMod ->
                        updateClickedUser(
                            username,
                            userId,
                            isBanned,
                            isMod
                        )
                    },
                    showTimeoutDialog = {
                        showTimeoutDialog()
                    },
                    showBanDialog = {
                        showBanDialog()
                    },
                    doubleClickMessage = { username ->
                        doubleClickMessage(username)
                    },
                    deleteChatMessage = { messageId ->
                        deleteChatMessage(messageId)
                    },
                    isMod = false,
                    inlineContentMap = inlineContentMap,
                    badgeListMap = badgeListMap,
                    usernameSize = usernameSize,
                    messageSize = messageSize,
                    lineHeight = lineHeight,
                    useCustomUsernameColors = useCustomUsernameColors,
                    globalTwitchEmoteContentMap = globalTwitchEmoteContentMap,
                    channelTwitchEmoteContentMap = channelTwitchEmoteContentMap,
                    globalBetterTTVEmoteContentMap = globalBetterTTVEmoteContentMap,
                    channelBetterTTVEmoteContentMap = channelBetterTTVEmoteContentMap,
                    sharedBetterTTVEmoteContentMap = sharedBetterTTVEmoteContentMap
                )
            }
        }
    }

    /**
     * - restartable
     * - skippable
     * */
    @Composable
    fun ChatMessages(
        twitchChatMessage: TwitchUserData,
        showBottomModal: () -> Unit,
        updateClickedUser: (String, String, Boolean, Boolean) -> Unit,
        showTimeoutDialog: () -> Unit,
        showBanDialog: () -> Unit,
        doubleClickMessage: (String) -> Unit,
        deleteChatMessage: (String) -> Unit,
        isMod: Boolean,
        inlineContentMap: EmoteListMap,
        globalTwitchEmoteContentMap: EmoteListMap,
        channelTwitchEmoteContentMap: EmoteListMap,
        globalBetterTTVEmoteContentMap: EmoteListMap,
        channelBetterTTVEmoteContentMap: EmoteListMap,
        sharedBetterTTVEmoteContentMap: EmoteListMap,
        badgeListMap: EmoteListMap,
        usernameSize: Float,
        messageSize: Float,
        lineHeight: Float,
        useCustomUsernameColors: Boolean
    ) {
        val titleFontSize = MaterialTheme.typography.titleMedium.fontSize
        val messageFontSize = MaterialTheme.typography.bodyMedium.fontSize
        val chatScope = remember { ChatScope(titleFontSize, messageFontSize) }
        val colorString = twitchChatMessage.color ?: "FF0000"
        val formattedColorString = if (!colorString.startsWith("#")) "#$colorString" else colorString
        val color = remember { mutableStateOf(Color(android.graphics.Color.parseColor(formattedColorString))) }
        if (color.value == Color.Black) {
            color.value = MaterialTheme.colorScheme.onSurface
        }

        with(chatScope) {
            when (twitchChatMessage.messageType) {
                MessageType.NOTICE -> { //added
                    NoticeMessages(
                        systemMessage = "",
                        message = twitchChatMessage.userType
                    )
                }

                MessageType.USER -> { //added
                    if (isMod) {
                        HorizontalDragDetectionBox(
                            itemBeingDragged = { dragOffset ->
                                ClickableCard(
                                    twitchUser = twitchChatMessage,
                                    color = color.value,
                                    showBottomModal = { showBottomModal() },
                                    updateClickedUser = { username, userId, isBanned, isMod ->
                                        updateClickedUser(
                                            username,
                                            userId,
                                            isBanned,
                                            isMod
                                        )
                                    },
                                    offset = if (twitchChatMessage.mod != "1") dragOffset else 0f,
                                    doubleClickMessage = { username -> doubleClickMessage(username) },
                                    inlineContentMap = inlineContentMap,
                                    badgeListMap = badgeListMap,
                                    usernameSize = usernameSize,
                                    messageSize = messageSize,
                                    lineHeight = lineHeight,
                                    useCustomUsernameColors = useCustomUsernameColors,
                                    globalTwitchEmoteContentMap = globalTwitchEmoteContentMap,
                                    channelTwitchEmoteContentMap = channelTwitchEmoteContentMap,
                                    globalBetterTTVEmoteContentMap = globalBetterTTVEmoteContentMap,
                                    channelBetterTTVEmoteContentMap = channelBetterTTVEmoteContentMap,
                                    sharedBetterTTVEmoteContentMap = sharedBetterTTVEmoteContentMap,
                                )
                            },
                            quarterSwipeLeftAction = {
                                Log.d("quarterSwipeLeftAction", "Cclicked")
                                if (twitchChatMessage.mod != "1") {
                                    updateClickedUser(
                                        twitchChatMessage.displayName ?: "",
                                        twitchChatMessage.userId ?: "",
                                        twitchChatMessage.banned,
                                        twitchChatMessage.mod == "1"
                                    )
                                    showTimeoutDialog()
                                }

                            },
                            quarterSwipeRightAction = {
                                Log.d("quarterSwipeLeftAction", "Cclicked")
                                if (twitchChatMessage.mod != "1") {
                                    updateClickedUser(
                                        twitchChatMessage.displayName ?: "",
                                        twitchChatMessage.userId ?: "",
                                        twitchChatMessage.banned,
                                        twitchChatMessage.mod == "1"
                                    )
                                    showBanDialog()
                                }

                            },
                            halfSwipeAction = {
                                deleteChatMessage(twitchChatMessage.id ?: "")
                            },
                            swipeEnabled = true,
                            twoSwipeOnly = false
                        )
                    } else {
                        ClickableCard(
                            twitchUser = twitchChatMessage,
                            color = color.value,
                            showBottomModal = { showBottomModal() },
                            updateClickedUser = { username, userId, isBanned, isMod ->
                                updateClickedUser(
                                    username,
                                    userId,
                                    isBanned,
                                    isMod
                                )
                            },
                            offset = 0f,
                            doubleClickMessage = { username -> doubleClickMessage(username) },
                            inlineContentMap = inlineContentMap,
                            globalTwitchEmoteContentMap = globalTwitchEmoteContentMap,
                            badgeListMap = badgeListMap,
                            usernameSize = usernameSize,
                            messageSize = messageSize,
                            lineHeight = lineHeight,
                            useCustomUsernameColors = useCustomUsernameColors,
                            channelTwitchEmoteContentMap = channelTwitchEmoteContentMap,
                            globalBetterTTVEmoteContentMap = globalBetterTTVEmoteContentMap,
                            channelBetterTTVEmoteContentMap = channelBetterTTVEmoteContentMap,
                            sharedBetterTTVEmoteContentMap = sharedBetterTTVEmoteContentMap,
                        )
                    }
                }

                MessageType.FIRSTTIMECHATTER -> {
                    if (isMod) {
                        HorizontalDragDetectionBox(
                            itemBeingDragged = { dragOffset ->
                                FirstTimeChatter {
                                    ClickableCard(
                                        twitchUser = twitchChatMessage,
                                        color = color.value,
                                        showBottomModal = { showBottomModal() },
                                        updateClickedUser = { username, userId, isBanned, isMod ->
                                            updateClickedUser(
                                                username,
                                                userId,
                                                isBanned,
                                                isMod
                                            )
                                        },
                                        offset = if (twitchChatMessage.mod != "1") dragOffset else 0f,
                                        doubleClickMessage = { username -> doubleClickMessage(username) },
                                        inlineContentMap = inlineContentMap,
                                        globalTwitchEmoteContentMap = globalTwitchEmoteContentMap,
                                        badgeListMap = badgeListMap,
                                        usernameSize = usernameSize,
                                        messageSize = messageSize,
                                        lineHeight = lineHeight,
                                        useCustomUsernameColors = useCustomUsernameColors,
                                        channelTwitchEmoteContentMap = channelTwitchEmoteContentMap,
                                        globalBetterTTVEmoteContentMap = globalBetterTTVEmoteContentMap,
                                        channelBetterTTVEmoteContentMap = channelBetterTTVEmoteContentMap,
                                        sharedBetterTTVEmoteContentMap = sharedBetterTTVEmoteContentMap,
                                    )
                                }

                            },
                            quarterSwipeLeftAction = {
                                Log.d("quarterSwipeLeftAction", "Cclicked")
                                if (twitchChatMessage.mod != "1") {
                                    updateClickedUser(
                                        twitchChatMessage.displayName ?: "",
                                        twitchChatMessage.userId ?: "",
                                        twitchChatMessage.banned,
                                        twitchChatMessage.mod == "1"
                                    )
                                    showTimeoutDialog()
                                }

                            },
                            quarterSwipeRightAction = {
                                Log.d("quarterSwipeLeftAction", "Cclicked")
                                if (twitchChatMessage.mod != "1") {
                                    updateClickedUser(
                                        twitchChatMessage.displayName ?: "",
                                        twitchChatMessage.userId ?: "",
                                        twitchChatMessage.banned,
                                        twitchChatMessage.mod == "1"
                                    )
                                    showBanDialog()
                                }

                            },
                            halfSwipeAction = {
                                deleteChatMessage(twitchChatMessage.id ?: "")
                            },
                            swipeEnabled = true,
                            twoSwipeOnly = false
                        )
                    } else {
                        FirstTimeChatter {
                            ClickableCard(
                                twitchUser = twitchChatMessage,
                                color = color.value,
                                showBottomModal = { showBottomModal() },
                                updateClickedUser = { username, userId, isBanned, isMod ->
                                    updateClickedUser(
                                        username,
                                        userId,
                                        isBanned,
                                        isMod
                                    )
                                },
                                offset = 0f,
                                doubleClickMessage = { username -> doubleClickMessage(username) },
                                inlineContentMap = inlineContentMap,
                                globalTwitchEmoteContentMap = globalTwitchEmoteContentMap,
                                badgeListMap = badgeListMap,
                                usernameSize = usernameSize,
                                messageSize = messageSize,
                                lineHeight = lineHeight,
                                useCustomUsernameColors = useCustomUsernameColors,
                                channelTwitchEmoteContentMap = channelTwitchEmoteContentMap,
                                globalBetterTTVEmoteContentMap = globalBetterTTVEmoteContentMap,
                                channelBetterTTVEmoteContentMap = channelBetterTTVEmoteContentMap,
                                sharedBetterTTVEmoteContentMap = sharedBetterTTVEmoteContentMap,
                            )
                        }
                    }


                }


                MessageType.ANNOUNCEMENT -> { //added
                    AnnouncementMessages(
                        message = "${twitchChatMessage.displayName}: ${twitchChatMessage.systemMessage}"
                    )
                }

                MessageType.RESUB -> { //added
                    ReSubMessage(
                        systemMessage = twitchChatMessage.systemMessage,
                        message = twitchChatMessage.userType,
                    )
                }

                MessageType.SUB -> { //added
                    SubMessages(
                        systemMessage = twitchChatMessage.systemMessage,
                        message = twitchChatMessage.userType,
                    )
                }
                // MYSTERYGIFTSUB,GIFTSUB
                MessageType.GIFTSUB -> { //added
                    GiftSubMessages(
                        message = twitchChatMessage.userType,
                        systemMessage = twitchChatMessage.systemMessage
                    )
                }

                MessageType.MYSTERYGIFTSUB -> { //
                    AnonGiftMessages(
                        message = twitchChatMessage.userType,
                        systemMessage = twitchChatMessage.systemMessage
                    )
                }

                MessageType.ERROR -> {
                    ChatErrorMessage(twitchChatMessage.userType ?: "")
                }

                MessageType.JOIN -> {
                    JoinMessage(
                        message = twitchChatMessage.userType ?: ""
                    )
                }

                else -> Unit

            }
        }
    }


    /**
     * - restartable
     * - skippable
     * */
    @Composable
    fun ScrollToBottom(
        scrollingPaused: Boolean,
        enableAutoScroll: () -> Unit,
        emoteKeyBoardHeight: Dp,
        modifier: Modifier
    ) {
        if (emoteKeyBoardHeight == 350.dp) {

        } else {
            Row(
                modifier = modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (scrollingPaused) {
                    DualIconsButton(
                        buttonAction = { enableAutoScroll() },
                        iconImageVector = Icons.Default.ArrowDropDown,
                        iconDescription = stringResource(coreR.string.arrow_drop_down_description),
                        buttonText = stringResource(coreR.string.scroll_to_bottom)

                    )
                }
            }
        }

    }
}


/**
 * - restartable
 * - skippable
 * */
/**
 * ClickableCard is the composable that implements the functionality that allows the user to click on a chat message
 * and have the bottom modal pop up
 *
 * @param twitchUser a [TwitchUserData][com.example.clicker.network.websockets.models.TwitchUserData] object that represents the state of an individual user and their chat message
 * @param color  a Color that will eventually be passed to [ChatBadges] and represent the color of the text
 * @param offset a Float representing how far this composable will be moving on screen
 * @param bottomModalState the state of a [ModalBottomSheetState][androidx.compose.material]
 * @param fontSize the font size of the text inside the [ChatBadges] composable
 * @param updateClickedUser a function that will run once this composable is clicked and will update the ViewModel with information
 * about the clicked user
 * */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClickableCard(
    twitchUser: TwitchUserData,
    color: Color,
    offset: Float,
    showBottomModal: () -> Unit,
    updateClickedUser: (String, String, Boolean, Boolean) -> Unit,
    doubleClickMessage: (String) -> Unit,
    inlineContentMap: EmoteListMap,
    globalTwitchEmoteContentMap: EmoteListMap,
    channelTwitchEmoteContentMap: EmoteListMap,
    globalBetterTTVEmoteContentMap: EmoteListMap,
    channelBetterTTVEmoteContentMap: EmoteListMap,
    sharedBetterTTVEmoteContentMap: EmoteListMap,
    badgeListMap: EmoteListMap,
    usernameSize: Float,
    messageSize: Float,
    lineHeight: Float,
    useCustomUsernameColors: Boolean


) {
    val showIcon = remember { mutableStateOf(false) }
    //this log is how I can check for unnecessary recomps
    Log.d("CLickingCardCheck", "${twitchUser.userType}")
    Column(
        modifier = Modifier.combinedClickable(
            enabled = true,
            onDoubleClick = {
                showIcon.value = true
                doubleClickMessage(twitchUser.displayName ?: "")
            },
            onClick = {
                updateClickedUser(
                    twitchUser.displayName ?: "",
                    twitchUser.userId ?: "",
                    twitchUser.banned,
                    twitchUser.mod == "1"
                )
                showBottomModal()
            }
        )
    ) {
        Box(
            modifier = Modifier
                .absoluteOffset { IntOffset(x = offset.roundToInt(), y = 0) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                CheckIfUserDeleted(twitchUser = twitchUser)
                CheckIfUserIsBanned(twitchUser = twitchUser)
                TextWithChatBadges(
                    twitchUser = twitchUser,
                    color = color,
                    fontSize = 13.sp,
                    inlineContentMap = inlineContentMap,
                    globalTwitchEmoteContentMap = globalTwitchEmoteContentMap,
                    channelTwitchEmoteContentMap = channelTwitchEmoteContentMap,
                    globalBetterTTVEmoteContentMap = globalBetterTTVEmoteContentMap,
                    badgeListMap = badgeListMap,
                    usernameSize = usernameSize,
                    messageSize = messageSize,
                    lineHeight = lineHeight,
                    useCustomUsernameColors = useCustomUsernameColors,
                    channelBetterTTVEmoteContentMap = channelBetterTTVEmoteContentMap,
                    sharedBetterTTVEmoteContentMap = sharedBetterTTVEmoteContentMap,

                    )
            }
            if (showIcon.value) {
                DoubleClickSeemsGoodIcon()
            }
        }
    }

}

/**
 * - restartable
 * - skippable
 * */
@Composable
fun DoubleClickSeemsGoodIcon() {

    val size = remember { Animatable(10F) }
    LaunchedEffect(true) {
        size.animateTo(40f)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 30.dp)
    ) {
        AsyncImage(
            model = "https://static-cdn.jtvnw.net/emoticons/v2/64138/static/light/1.0",
            contentDescription = stringResource(coreR.string.moderator_badge_icon_description),
            modifier = Modifier
                .size(size.value.dp)
                .align(Alignment.CenterEnd)
        )
    }
}

/**
 * - restartable
 * - skippable
 * */

/**
 * HorizontalDragDetectionBox is a [Box] that will detect the user's drag movement and will move [itemBeingDragged] accordingly. Also, depending
 * of if the thresholds are dragged across functions such as [quarterSwipeRightAction], [quarterSwipeLeftAction] and [halfSwipeAction]
 * once the drag stopped. Icons such as [halfSwipeIconResource], [quarterSwipeLeftIconResource] and [quarterSwipeRightIconResource] will
 * also be shown when the user crosses those thresholds
 *
 * @param itemBeingDragged a composable function that will be dragged when the drags it accross the screen.
 * @param twoSwipeOnly a boolean that is used to determine of there are functions for quarter swipes and half swipes or just quarter swipes.
 * A true value indicates that [quarterSwipeRightAction] and [quarterSwipeLeftAction] will get triggered. A false value means that
 * [quarterSwipeRightAction], [quarterSwipeLeftAction] and [halfSwipeAction] will get triggered
 * @param quarterSwipeRightAction is a function that will be called if a user swipes and passes the threshold of 0.25 of [itemBeingDragged] width
 * @param quarterSwipeLeftAction is a function that will be called if a user swipes and passes the threshold of -1*(0.25) of [itemBeingDragged] width
 * @param halfSwipeAction a optional function that will be called if [twoSwipeOnly] is set to false and the user's drag passes
 * the threshold of +/- 0.5 of [itemBeingDragged] width
 * @param halfSwipeIconResource is a [Painter] that will be shown to the user if the half swipe threshold is crossed and [twoSwipeOnly] is false
 * @param quarterSwipeLeftIconResource is a [Painter] that will be shown to the user if the -1 *(quarter) swipe threshold is crossed
 * @param quarterSwipeRightIconResource is a [Painter] that will be shown to the user if the quarter swipe threshold is crossed
 * @param hideIconColor: a [Color] that the icons will be set to hide them from the user
 * @param showIconColor: a [Color] that the icons will be set to reveal them to the user
 * */
@Composable
fun HorizontalDragDetectionBox(
    itemBeingDragged: @Composable (dragOffset: Float) -> Unit,
    twoSwipeOnly: Boolean,
    quarterSwipeRightAction: () -> Unit,
    quarterSwipeLeftAction: () -> Unit,
    halfSwipeAction: () -> Unit = {},
    halfSwipeIconResource: Painter = painterResource(id = coreR.drawable.delete_outline_24),
    quarterSwipeLeftIconResource: Painter = painterResource(id = coreR.drawable.time_out_24),
    quarterSwipeRightIconResource: Painter = painterResource(id = coreR.drawable.ban_24),
    hideIconColor: Color = MaterialTheme.colorScheme.onSurface,
    showIconColor: Color = MaterialTheme.colorScheme.onPrimary,
    swipeEnabled: Boolean
) {
    var iconShownToUser: Painter = painterResource(id = coreR.drawable.ban_24)
    var dragging by remember { mutableStateOf(true) }
    val state = rememberDraggableActions()
    val offset = if (swipeEnabled) state.offset.value else 0f
    var iconColor = hideIconColor

    //todo: this could probably use derivedstateof
    if (dragging && !twoSwipeOnly) {
        if (state.offset.value >= (state.halfWidth)) {
            iconShownToUser = halfSwipeIconResource
            iconColor = showIconColor
        } else if (state.offset.value <= -(state.halfWidth)) {
            iconShownToUser = halfSwipeIconResource
            iconColor = showIconColor
        } else if (state.offset.value <= -(state.quarterWidth)) {
            iconShownToUser = quarterSwipeLeftIconResource
            iconColor = showIconColor
        } else if (state.offset.value >= (state.quarterWidth)) {
            iconShownToUser = quarterSwipeRightIconResource
            iconColor = showIconColor
        }
    } else if (dragging && twoSwipeOnly) {
        if (state.offset.value <= -(state.quarterWidth)) {
            iconShownToUser = quarterSwipeLeftIconResource
            iconColor = showIconColor
        } else if (state.offset.value >= (state.quarterWidth)) {
            iconShownToUser = quarterSwipeRightIconResource
            iconColor = showIconColor
        }
    }


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .draggable(
                orientation = Orientation.Horizontal,
                onDragStopped = {
                    if (twoSwipeOnly && swipeEnabled) {
                        state.checkQuarterSwipeThresholds(
                            leftSwipeAction = {
                                quarterSwipeLeftAction()
                            },
                            rightSwipeAction = {
                                quarterSwipeRightAction()
                            }
                        )
                    } else if (swipeEnabled) {
                        state.checkDragThresholdCrossed(
                            deleteMessageSwipe = {
                                halfSwipeAction()
                            },
                            timeoutUserSwipe = {
                                quarterSwipeLeftAction()
                            },
                            banUserSwipe = {
                                quarterSwipeRightAction()
                            }
                        )
                    }

                    dragging = false
                    state.resetOffset()
                },
                onDragStarted = {
                    dragging = true
                },


                enabled = true,
                state = state.draggableState
            )
            .onGloballyPositioned { layoutCoordinates ->
                state.setWidth(layoutCoordinates.size.width)
            }
    ) {

        Icon(
            painter = iconShownToUser, contentDescription = "", tint = iconColor, modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp)
        )
        Icon(
            painter = iconShownToUser, contentDescription = "", tint = iconColor,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 10.dp)
        )

        itemBeingDragged(offset)
    }


}

/**
 * - restartable
 * - skippable
 * */
/**
 * This is the entire chat textfield with the filtered row above it
 * */
@Composable
fun EnterChatColumn(
    modifier: Modifier,
    filteredRow: @Composable () -> Unit,
    showModStatus: @Composable () -> Unit,
    stylizedTextField: @Composable (Modifier) -> Unit,
    showIconBasedOnTextLength: @Composable () -> Unit,
) {
    Surface(
        tonalElevation = 2.dp,
        contentColor = MaterialTheme.colorScheme.secondary
    ) {
        Column(
            modifier = modifier
                .padding(bottom = 4.dp)
        ) {
            filteredRow()

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                showModStatus()
                stylizedTextField(
                    Modifier
                        .weight(2f)
                        .padding(start = 8.dp)
                )
                showIconBasedOnTextLength()
            }
        }
    }
}

/**
 * - restartable
 * - skippable
 * */
@Composable
fun ForwardSlash(
    modifier: Modifier,
    forwardSlashes: ForwardSlashCommandsImmutableCollection,
    clickedCommandAutoCompleteText: (String) -> Unit,
) {
    Log.d("ForwardSlashRecomp", "recomp")

    LazyColumn(
        modifier = modifier
            .fillMaxWidth(),
        reverseLayout = true
    ) {
        items(forwardSlashes.snacks) { command ->
            Column(modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .clickable {
                    clickedCommandAutoCompleteText(command.clickedValue)
                }
            ) {
                Text(
                    command.title,
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    command.subtitle,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    lineHeight = 15.sp
                )
            }
        }

    }

}

/**Extension function used to determine if the use has scrolled to the end of the chat*/
fun LazyListState.isScrolledToEnd() = layoutInfo.visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1


/**
 * - restartable
 * - skippable
 * */
/**
 * - Contains 0 extra parts
 * A [Button] meant to display a message surrounded by two icons.
 *
 * @param buttonAction a function that will run when this button is clicked
 * @param iconImageVector the image vector for the two icons surrounding the [buttonText]
 * @param iconDescription a String that will act as the contentDescription for the two icons created by the [iconImageVector]
 * @param buttonText a String that will be displayed on top of the Button. This String should be short and no longer than
 * 3 words
 * */
@Composable
fun DualIconsButton(
    buttonAction: () -> Unit,
    iconImageVector: ImageVector,
    iconDescription: String,
    buttonText: String,
) {
    Button(
        onClick = { buttonAction() }
    ) {
        Icon(
            imageVector = iconImageVector,
            contentDescription = iconDescription,
            tint = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier
        )
        Text(buttonText, color = MaterialTheme.colorScheme.onSecondary)
        Icon(
            imageVector = iconImageVector,
            contentDescription = iconDescription,
            tint = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier
        )
    }
}

/**
 * - restartable
 * - skippable
 * */
/**
 * This composable represents a clickable username shown to the user. When the [username] is clicked it will
 * automatically be added to the users text that they are typing
 *
 * @param clickedAutoCompleteText a function that will do the auto completing when this text is clicked
 * @param username the String shown to the user. This represents a username of a user in chat.
 * */
@Composable
fun ClickedAutoText(
    clickedAutoCompleteText: (String) -> Unit,
    username: String
) {
    Box(
        Modifier
            .padding(horizontal = 5.dp)
    ) {
        Text(
            text = username,
            Modifier
                .clip(RoundedCornerShape(5.dp))
                .background(Color.DarkGray)
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .clickable {
                    clickedAutoCompleteText(username)
                },
            color = Color.White,
        )
    }


}


/**
 * - restartable
 * - skipable
 * */
/**
 * A [LazyRow] used to represent all the usernames of every chatter in chat. This will be triggered to be shown
 * when a user enters the ***@*** character. This composable is also made up of the [TextChatParts.ClickedAutoText]
 * composable
 *
 * @param filteredChatListImmutable a [FilteredChatListImmutableCollection] object used to make this composable function restartable and skippeable
 * @param clickedAutoCompleteText a function passed to [TextChatParts.ClickedAutoText] that enables autocomplete on click
 * */
@Composable
fun FilteredMentionLazyRow(
    filteredChatListImmutable: FilteredChatListImmutableCollection,
    clickedAutoCompleteText: (String) -> Unit,
) {
    if (filteredChatListImmutable.chatList.isNotEmpty()) {
        LazyRow(modifier = Modifier.padding(vertical = 10.dp)) {
            items(filteredChatListImmutable.chatList) {
                ClickedAutoText(
                    clickedAutoCompleteText = { username -> clickedAutoCompleteText(username) },
                    username = it
                )
            }
        }
    }
}


/**
 * - restartable
 * - skippable
 * */
/**
 * A Composable that will show an Icon based on the length of [textFieldValue]. If the length is greater than 0 then
 * the [ArrowForward] will be shown. If the length is less then or equal to 0 then the [MoreVert] will be shown
 *
 * @param textFieldValue the values used to determine which icon should be shown
 * @param chat a function that is used to send a message to the websocket and allows the user to communicate with other users
 * @param showModal a function that is used to open the side chat and show the chat settings
 * */
@Composable
fun ShowIconBasedOnTextLength(
    textFieldValue: MutableState<TextFieldValue>,
    chat: (String) -> Unit,
    showModal: () -> Unit,
) {
    Log.d("ShowIconBasedOnTextLengthRecomp", "RECOMP")

    if (textFieldValue.value.text.isNotEmpty()) {
        IconButton(
            modifier = Modifier
                .padding(start = 5.dp),
            onClick = {
                chat(textFieldValue.value.text)
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(coreR.string.send_chat),
                modifier = Modifier,
            )
        }
    } else {
        IconButton(
            modifier = Modifier
                .padding(start = 5.dp),
            onClick = {
                showModal()
            }
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(coreR.string.more_vert_icon_description),
                modifier = Modifier
            )
        }

    }
}

/**
 * - restartable
 * - skippable
 * */
/**
 * A styled [TextField] to allow the user to enter chat messages
 * @param modifier determines how much of the screen it takes up. should be given a value of .weight(2f)
 * @param textFieldValue The value that the user it currently typing in
 * @param newFilterMethod This method will trigger where to show the [TextChatParts.FilteredMentionLazyRow] or not
 *
 * */
@Composable
fun StylizedTextField(
    modifier: Modifier,
    newFilterMethod: (TextFieldValue) -> Unit,
    showKeyBoard: () -> Unit,
    showEmoteBoard: () -> Unit,
    iconClicked: Boolean,
    setIconClicked: (Boolean) -> Unit,
    actualTextFieldValue: TextFieldValue,
    changeActualTextFieldValue: (String, TextRange) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val source = remember {
        MutableInteractionSource()
    }

    val customTextSelectionColors = TextSelectionColors(
        handleColor = MaterialTheme.colorScheme.secondary,
        backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
    )
    if (source.collectIsPressedAsState().value && iconClicked) {
        Log.d("TextFieldClicked", "clicked and iconclicked")
        setIconClicked(false)
        showKeyBoard()
    }

    CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
        Box(
            modifier = modifier.padding(top = 5.dp)
        ) {
            TextField(
                interactionSource = source,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(30.dp)
                    .focusRequester(focusRequester),
                singleLine = false,
                maxLines = 5,
                value = actualTextFieldValue,
                textStyle = TextStyle( // Adjust text style for padding reduction
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 15.sp
                ),
                shape = RoundedCornerShape(8.dp),
                onValueChange = { newText ->
                    newFilterMethod(newText)
                    changeActualTextFieldValue(newText.text, newText.selection)
                },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    disabledTextColor = Color.White,
                    errorTextColor = Color.Red,
                    focusedContainerColor = Color.DarkGray,
                    unfocusedContainerColor = Color.DarkGray,
                    disabledContainerColor = Color.DarkGray,
                    errorContainerColor = Color.DarkGray,
                    cursorColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
                placeholder = {
                    Text(
                        text = stringResource(coreR.string.send_a_message), color = Color.White,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 15.sp
                        )
                    )
                },
                trailingIcon = {
                    if (!iconClicked) {
                        Icon(
                            painter = painterResource(id = coreR.drawable.emote_face_24),
                            contentDescription = "",
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 5.dp)
                                .clickable {
                                    setIconClicked(true)
                                    showEmoteBoard()
                                }
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = coreR.drawable.keyboard_24),
                            contentDescription = "",
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 5.dp)
                                .clickable {
                                    setIconClicked(false)
                                    showKeyBoard()
                                    keyboard?.show()
                                    focusRequester.requestFocus()
                                }
                        )
                    }
                }
            )
        }
    }
}


/**
 * - restartable
 * - skippable
 * */
/**
 * A composable meant to show a moderator Icon based on the status of [modStatus]
 *
 * @param modStatus a boolean meant to determine if the user is a moderator or not.
 * @param showOuterBottomModalState a function used to show the a bottom layout sheet
 * */
@Composable
fun ShowModStatus(
    modStatus: Boolean?,
    orientationIsVertical: Boolean,
    notificationAmount: Int,
    showModView: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    Log.d("isModCheck", "isMod --> $modStatus")
    Log.d("ShowModStatusRecomp", "RECOMP")

    if (BuildConfig.DEBUG) {
        Box {

            AsyncImage(
                modifier = Modifier
                    .clickable {
                        if (orientationIsVertical) {
                            showModView()
                        }
                    }
                    .padding(top = 10.dp, end = 2.dp),
                model = "https://static-cdn.jtvnw.net/badges/v1/3267646d-33f0-4b17-b3df-f923a41db1d0/3",
                contentDescription = stringResource(coreR.string.moderator_badge_icon_description)
            )
            if (notificationAmount > 0) {
                Text(
                    "$notificationAmount",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Red)
                        .padding(horizontal = 3.dp),
                    color = Color.White, fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        if (modStatus != null && modStatus == true) {
            Box {

                AsyncImage(
                    modifier = Modifier
                        .clickable {
                            Log.d("ClickingTheModStatus", "orientationIsVertical ->$orientationIsVertical")
                            if (orientationIsVertical) {
                                Log.d("ClickingTheModStatus", "orientationIsVertical ->$orientationIsVertical")
                                showModView()
                            }

                        },
                    model = "https://static-cdn.jtvnw.net/badges/v1/3267646d-33f0-4b17-b3df-f923a41db1d0/3",
                    contentDescription = stringResource(coreR.string.moderator_badge_icon_description)
                )
                if (notificationAmount > 0) {
                    Text(
                        "$notificationAmount",
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Red)
                            .padding(horizontal = 3.dp),
                        color = Color.White,
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }


}

/**
 * - restartable
 * - skippable
 * */
/**
 * CheckIfUserDeleted is the composable that will be used to determine if there should be extra information shown
 * depending if the user's message has been deleted or not
 *
 * @param twitchUser a [TwitchUserData][TwitchUserData] object that represents the state of an individual user and their chat message
 * */
@Composable
fun CheckIfUserDeleted(twitchUser: TwitchUserData) {
    if (twitchUser.deleted) {
        Text(
            stringResource(coreR.string.moderator_deleted_comment),
            fontSize = MaterialTheme.typography.headlineSmall.fontSize,
            modifier = Modifier.padding(start = 5.dp),
            color = androidx.compose.material.MaterialTheme.colors.onSurface
        )
    }
}

/**
 * - restartable
 * - skippable
 * */
/**
 *
 * CheckIfUserIsBanned is the composable that will be used to determine if there should be extra information shown
 * depending if the user has been banned by a moderator
 *
 * */
@Composable
fun CheckIfUserIsBanned(twitchUser: TwitchUserData) {
    if (twitchUser.banned) {
        val duration =
            if (twitchUser.bannedDuration != null) "Banned for ${twitchUser.bannedDuration} seconds" else "Banned permanently"
        Text(
            duration,
            fontSize = MaterialTheme.typography.headlineSmall.fontSize,
            modifier = Modifier.padding(start = 5.dp)
        )
    }
}


/**
 * - restartable
 * - skippable
 * */
/**
 *
 * TextWithChatBadges is really just a wrapper class around [ChatBadges] to allow us to use it a little more cleanly
 * throughout our code
 *
 * @param twitchUser a [TwitchUserData][com.example.clicker.network.websockets.models.TwitchUserData] object that represents the state of an individual user and their chat message
 * @param color  a color passed to [ChatBadges]
 * @param fontSize a font size passed to [ChatBadges]
 * */
@Composable
fun TextWithChatBadges(
    twitchUser: TwitchUserData,
    color: Color,
    fontSize: TextUnit,
    inlineContentMap: EmoteListMap,
    globalTwitchEmoteContentMap: EmoteListMap,
    channelTwitchEmoteContentMap: EmoteListMap,
    globalBetterTTVEmoteContentMap: EmoteListMap,
    badgeListMap: EmoteListMap,
    usernameSize: Float,
    messageSize: Float,
    lineHeight: Float,
    useCustomUsernameColors: Boolean,
    channelBetterTTVEmoteContentMap: EmoteListMap,
    sharedBetterTTVEmoteContentMap: EmoteListMap,

    ) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        ChatBadges(
            username = "${twitchUser.displayName} :",
            message = " ${twitchUser.userType}",
            isMod = twitchUser.mod == "1",
            isSub = twitchUser.subscriber == true,
            isMonitored = twitchUser.isMonitored,
            color = color,
            textSize = fontSize,
            messageList = twitchUser.messageList,
            inlineContentMap = inlineContentMap,
            globalTwitchEmoteContentMap = globalTwitchEmoteContentMap,
            badgeList = twitchUser.badges,
            badgeListMap = badgeListMap,
            usernameSize = usernameSize,
            messageSize = messageSize,
            lineHeight = lineHeight,
            useCustomUsernameColors = useCustomUsernameColors,
            channelTwitchEmoteContentMap = channelTwitchEmoteContentMap,
            globalBetterTTVEmoteContentMap = globalBetterTTVEmoteContentMap,
            channelBetterTTVEmoteContentMap = channelBetterTTVEmoteContentMap,
            sharedBetterTTVEmoteContentMap = sharedBetterTTVEmoteContentMap,
        )

    } // end of the row
}

/**
 * - restartable
 * */
/**
 *
 * ChatBadges is the composable that is responsible for showing the chat badges(mod,sub and global) beside the users username.
 * Also, it shows all of the normal chat messages(with their emotes)
 *
 * @param username a String representing the user that is currently sending chats
 * @param message  a String representing the message sent by this user
 * @param isMod a boolean determining if the user is a moderator or not
 * @param isSub a boolean determining if the user is a subscriber or not
 * @param color the color of the text
 * @param textSize the size of the text
 * */
@Composable
fun ChatBadges(
    username: String,
    message: String,
    isMod: Boolean,
    isSub: Boolean,
    isMonitored: Boolean,
    color: Color,
    textSize: TextUnit,
    messageList: List<MessageToken>,
    badgeList: List<String>,
    inlineContentMap: EmoteListMap,
    globalTwitchEmoteContentMap: EmoteListMap,
    channelTwitchEmoteContentMap: EmoteListMap,
    globalBetterTTVEmoteContentMap: EmoteListMap,
    channelBetterTTVEmoteContentMap: EmoteListMap,
    sharedBetterTTVEmoteContentMap: EmoteListMap,
    badgeListMap: EmoteListMap,
    usernameSize: Float,
    messageSize: Float,
    lineHeight: Float,
    useCustomUsernameColors: Boolean
) {
    val usernameColor = if (useCustomUsernameColors) color else MaterialTheme.colorScheme.secondary


    val newMap =
        globalTwitchEmoteContentMap.map + badgeListMap.map + channelTwitchEmoteContentMap.map + globalBetterTTVEmoteContentMap.map + channelBetterTTVEmoteContentMap.map + sharedBetterTTVEmoteContentMap.map

    //subscriber
    /***********TESTING OUT THE EMOTES MAPS*******************/


    /***********END TESTING OUT THE EMOTES MAPS*******************/
    //moderator subscriber
    Log.d("LoggingBadges", "list -> $badgeList")

    val text = buildAnnotatedString {

        for (item in badgeList) {
            if (badgeListMap.map.containsKey(item)) {
                withStyle(style = SpanStyle(fontSize = 15.sp)) {
                    appendInlineContent(item, item)
                }

            }
        }

        withStyle(style = SpanStyle(color = usernameColor, fontSize = usernameSize.sp, fontWeight = FontWeight.Bold)) {
            append("$username ")
        }
        //todo:below should get replaced with the new messageList
        for (messageToken in messageList) {
            if (messageToken.messageType == PrivateMessageType.MESSAGE) {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = messageSize.sp)) {
                    appendInlineContent(messageToken.messageValue, "${messageToken.messageValue} ")
                }
            } else {
                appendInlineContent(messageToken.messageValue, "[${messageToken.messageValue}]")
            }
        }

    }

    Row {
        Text(
            text = text,
            inlineContent = newMap,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            color = color,
            fontSize = textSize,
            lineHeight = lineHeight.sp
        )
    }
}

enum class InputSelector {
    NONE,
    RECENT,
    CHANNEL,
    GLOBAL
}

enum class InputGIFSelector {
    NONE,
    CHANNEL,
    SHARE,
    GLOBAL
}
