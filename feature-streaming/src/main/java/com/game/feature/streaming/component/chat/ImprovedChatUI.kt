package com.game.feature.streaming.component.chat

import android.os.Build
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
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
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.android.model.websockets.MessageType
import com.game.feature.streaming.entities.FilteredChatListImmutableCollection
import com.game.feature.streaming.entities.ForwardSlashCommandsImmutableCollection
import com.paulrybitskyi.gamedge.common.api.BuildConfig
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteListMap
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteNameUrl
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteNameUrlEmoteTypeList
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteNameUrlList
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteTypes
import com.paulrybitskyi.gamedge.common.domain.chat.IndivBetterTTVEmoteList
import com.paulrybitskyi.gamedge.common.domain.websockets.MessageToken
import com.paulrybitskyi.gamedge.common.domain.websockets.PrivateMessageType
import com.paulrybitskyi.gamedge.common.domain.websockets.TwitchUserData
import com.paulrybitskyi.gamedge.common.ui.rememberDraggableActions
import kotlinx.collections.immutable.ImmutableList
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
            ChatUILazyColumn(
                lazyColumnListState = lazyColumnListState,
                twitchUserChat = twitchUserChat.toImmutableList(),
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
                inlineContentMap = inlineContentMap

            )
        },
        scrollToBottom = { modifierBottom ->
            ScrollToBottom(
                scrollingPaused = !autoscroll,
                enableAutoScroll = { autoscroll = true },
                emoteKeyBoardHeight = emoteKeyBoardHeight.value,
                modifier = modifierBottom
            )
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
        forwardSlashes = forwardSlashes
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
    scrollToBottom: @Composable ImprovedChatUI.(Modifier) -> Unit,
    enterChat: @Composable ImprovedChatUI.(Modifier) -> Unit,
    noChat: Boolean,
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


    //todo: add a conditional to show emoteBoard to help with recomps


    val chatUIScope = remember { ImprovedChatUI() }
    with(chatUIScope) {
        Box(modifier = modifier.fillMaxSize()) {
            scrollToBottom(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
                    .zIndex(5f)
            )

            Column(Modifier.fillMaxSize()) {

                chatUI(Modifier.weight(1f))
                enterChat(Modifier.fillMaxWidth())

                if (emoteKeyBoardHeight == 350.dp) {

                    EmoteBoard(
                        modifier = Modifier.zIndex(8f),
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
                    NoticeMessages(
                        systemMessage = "",
                        message = "You are in No Chat mode"
                    )
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

fun LazyGridScope.header(
    content: @Composable LazyGridItemScope.() -> Unit
) {
    item(span = { GridItemSpan(this.maxLineSpan) }, content = content)
}

/**
 * - restartable
 * - skippable
 * */
@OptIn(ExperimentalFoundationApi::class)
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
    val secondaryColor = MaterialTheme.colorScheme.secondary

    //modifier =Modifier.weight(1f)
    val pagerState = rememberPagerState(pageCount = {
        2
    })
    val underlineModifier = Modifier.drawBehind {
        val strokeWidthPx = 1.dp.toPx()
        val verticalOffset = size.height - 2.sp.toPx()
        drawLine(
            color = secondaryColor,
            strokeWidth = strokeWidthPx,
            start = Offset(0f, verticalOffset),
            end = Offset(size.width, verticalOffset)
        )
    }
    Column() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                "Twitch", color = MaterialTheme.colorScheme.onPrimary,
                modifier = if (pagerState.currentPage == 0) underlineModifier else Modifier.clickable {
                    scope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                },
            )
            Spacer(modifier = Modifier.width(25.dp))
            Text(
                "BetterTTV",
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = if (pagerState.currentPage == 1) underlineModifier else Modifier.clickable {
                    scope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                },
            )

        }
        HorizontalPager(state = pagerState) { page ->
            // Our page content
            when (page) {
                0 -> {

                    Column(
                        modifier = modifier
                    ) {
                        Box() {
                            LazyGridEmotes(
                                lazyGridState = lazyGridState,
                                emoteBoardGlobalList = emoteBoardGlobalList,
                                emoteBoardChannelList = emoteBoardChannelList,
                                emoteBoardMostFrequentList = emoteBoardMostFrequentList,//EmoteNameUrlList(),

                                updateTextWithEmote = { emoteValue ->
                                    updateTextWithEmote(
                                        emoteValue
                                    )
                                },
                                updateTempararyMostFrequentEmoteList = { value ->
                                    updateTempararyMostFrequentEmoteList(value)
                                },
                                modifier = Modifier.padding(bottom = 50.dp),
                                userIsSub = userIsSub

                            )
                            EmoteBottomUI(
                                modifier = Modifier.align(Alignment.BottomCenter),
                                closeEmoteBoard = { closeEmoteBoard() },
                                deleteEmote = { deleteEmote() },
                                scrollToGlobalEmotes = {
                                    scope.launch {
                                        lazyGridState.scrollToItem((emoteBoardChannelList.list.size + emoteBoardMostFrequentList.list.size + 1))
                                    }
                                },
                                scrollToChannelEmotes = {
                                    scope.launch {
                                        lazyGridState.scrollToItem(emoteBoardMostFrequentList.list.size + 1)
                                    }
                                },
                                scrollToMostFrequentlyUsedEmotes = {
                                    scope.launch {
                                        lazyGridState.scrollToItem(0)
                                    }
                                }


                            )
                        }
                    }


                }

                1 -> {
                    Column(
                        modifier = modifier
                    ) {
                        Box() {
                            BetterTTVEmoteBoard(
                                globalBetterTTVResponse = globalBetterTTVEmotes,
                                updateTextWithEmote = { value -> updateTextWithEmote(value) },
                                channelBetterTTVResponse = channelBetterTTVResponse,
                                sharedBetterTTVResponse = sharedBetterTTVResponse,
                                betterTTVLazyGridState = betterTTVLazyGridState,
                                modifier = Modifier.padding(bottom = 50.dp),

                                )
                            BetterTTVEmoteBottomUI(
                                closeEmoteBoard = { closeEmoteBoard() },
                                deleteEmote = { deleteEmote() },
                                scrollToGlobalEmotes = {
                                    scope.launch {
//
                                        betterTTVLazyGridState.scrollToItem(channelBetterTTVResponse.list.size + 2 + emoteBoardMostFrequentList.list.size + sharedBetterTTVResponse.list.size)
                                    }
                                },
                                scrollToChannelEmotes = {
                                    scope.launch {
                                        betterTTVLazyGridState.scrollToItem(emoteBoardMostFrequentList.list.size + 1)
                                    }
                                },
//                            scrollToMostFrequentlyUsedEmotes={
//                                scope.launch {
//                                    betterTTVLazyGridState.scrollToItem(0)
//                                }
//                            },
                                scrollToSharedChannelEmotes = {
                                    scope.launch {
                                        betterTTVLazyGridState.scrollToItem(channelBetterTTVResponse.list.size + 1 + emoteBoardMostFrequentList.list.size + 1)
                                    }
                                },
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )

                        }


                    }


                }
            }

        }
        /****END OF THE HORIZONTAL PAGER****/
        /****END OF THE HORIZONTAL PAGER****/
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
        columns = GridCells.Adaptive(minSize = 60.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp)
            .height(250.dp)
            .background(MaterialTheme.colorScheme.primary),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        /*****************************START OF THE Most Recent EMOTES*******************************/
        /*****************************START OF THE Most Recent EMOTES*******************************/

//        header {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 5.dp)
//            ) {
//                Spacer(modifier = Modifier.padding(5.dp))
//                Text(
//                    "Frequently Used Emotes",
//                    color = MaterialTheme.colorScheme.onPrimary,
//                    fontSize = MaterialTheme.typography.headlineSmall.fontSize
//                ) // or any composable for your single row
//                Spacer(modifier = Modifier.padding(2.dp))
//                Divider(
//                    thickness = 2.dp,
//                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
//                    modifier = Modifier.fillMaxWidth()
//                )
//                Spacer(modifier = Modifier.padding(5.dp))
//            }
//
//        }
//        items(
//            mostFrequentBetterTTVResponse.list,
//        ) {
//            GifLoadingAnimation(
//                url ="https://cdn.betterttv.net/emote/${it.id}/1x",
//                contentDescription = "${it.code} emote",
//                emoteName =it.code,
//                updateTextWithEmote ={value ->
//                    updateTextWithEmote(value)
//                }
//            )
//        }
        /*****************************START OF THE CHANNEL EMOTES*******************************/
        /*****************************START OF THE CHANNEL EMOTES*******************************/
        //todo: adding the channelUI
        header {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp)
            ) {
                Spacer(modifier = Modifier.padding(5.dp))
                Text(
                    "Channel Emotes",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize
                ) // or any composable for your single row
                Spacer(modifier = Modifier.padding(2.dp))
                Divider(
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.padding(5.dp))
            }

        }

        items(channelBetterTTVResponse.list) {
            GifLoadingAnimation(
                url = "https://cdn.betterttv.net/emote/${it.id}/1x",
                contentDescription = "${it.code} emote",
                emoteName = it.code,
                updateTextWithEmote = { value ->
                    updateTextWithEmote(value)
//                            updateFrequentBetterTTVTempList(it)
                }
            )
        }

        /*****************************START OF THE SHARED CHANNEL EMOTES*******************************/

        /*****************************START OF THE SHARED CHANNEL EMOTES*******************************/
        header {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp)
            ) {
                Spacer(modifier = Modifier.padding(5.dp))
                Text(
                    "Shared Emotes",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize
                ) // or any composable for your single row
                Spacer(modifier = Modifier.padding(2.dp))
                Divider(
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.padding(5.dp))
            }

        }

        items(sharedBetterTTVResponse.list) {
            GifLoadingAnimation(
                url = "https://cdn.betterttv.net/emote/${it.id}/1x",
                contentDescription = "${it.code} emote",
                emoteName = it.code,
                updateTextWithEmote = { value ->
                    updateTextWithEmote(value)
//                    updateFrequentBetterTTVTempList(it)
                }
            )
        }


        /*****************************START OF THE GLOBAL EMOTES*******************************/


        /*****************************START OF THE GLOBAL EMOTES*******************************/
        header {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp)
            ) {
                Spacer(modifier = Modifier.padding(5.dp))
                Text(
                    "Global Emotes",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize
                ) // or any composable for your single row
                Spacer(modifier = Modifier.padding(2.dp))
                Divider(
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.padding(5.dp))
            }

        }


        items(globalBetterTTVResponse.list) {
            GifLoadingAnimation(
                url = "https://cdn.betterttv.net/emote/${it.id}/1x",
                contentDescription = "${it.code} emote",
                emoteName = it.code,
                updateTextWithEmote = { value ->
                    updateTextWithEmote(value)
                }
            )
        }


    }
}


/**
 * - restartable
 * - skippable
 * */
@Composable
fun GifLoadingAnimation(
    url: String,
    contentDescription: String,
    emoteName: String,
    updateTextWithEmote: (String) -> Unit

) {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        imageLoader = imageLoader,
        modifier = Modifier
            .size(60.dp)
            .padding(5.dp)
            .clickable {
                updateTextWithEmote(emoteName)
            }
    )
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
            .padding(vertical = 5.dp, horizontal = 10.dp)
            .background(MaterialTheme.colorScheme.primary),
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
                tint = MaterialTheme.colorScheme.onPrimary,
                painter = painterResource(id = coreR.drawable.keyboard_arrow_down_24),
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
                tint = MaterialTheme.colorScheme.onPrimary,
                painter = painterResource(id = coreR.drawable.autorenew_24),
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
                tint = MaterialTheme.colorScheme.onPrimary,
                painter = painterResource(id = coreR.drawable.channel_emotes_24),
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
                tint = MaterialTheme.colorScheme.onPrimary,
                painter = painterResource(id = coreR.drawable.world_emotes_24),
                contentDescription = "click to scroll to gloabl emotes"
            )


        }
        Box(modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color.DarkGray)
            .clickable {
                deleteEmote()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            .padding(vertical = 5.dp, horizontal = 10.dp)

        ) {
            Icon(
                modifier = Modifier.size(25.dp),
                tint = MaterialTheme.colorScheme.onPrimary,
                painter = painterResource(id = coreR.drawable.baseline_backspace_24),
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
    scrollToGlobalEmotes: () -> Unit,
    scrollToChannelEmotes: () -> Unit,
//    scrollToMostFrequentlyUsedEmotes:()->Unit,
    scrollToSharedChannelEmotes: () -> Unit,
    modifier: Modifier,
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 10.dp)
            .background(MaterialTheme.colorScheme.primary),
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
                tint = MaterialTheme.colorScheme.onPrimary,
                painter = painterResource(id = coreR.drawable.keyboard_arrow_down_24),
                contentDescription = "click to close keyboard emote"
            )
            Spacer(modifier = Modifier.width(10.dp))

//            Icon(modifier= Modifier
//                .size(25.dp)
//                .clickable {
//                    Log.d("EmoteBottomUI", "RECENT")
//                    scrollToMostFrequentlyUsedEmotes()
//                },
//                tint = MaterialTheme.colorScheme.onPrimary,
//                painter = painterResource(id =R.drawable.autorenew_24), contentDescription = "click to scroll to most recent emotes")
//            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                modifier = Modifier
                    .size(25.dp)
                    .clickable {
                        scrollToChannelEmotes()
                        Log.d("EmoteBottomUI", "CHANNEL")
                    },
                tint = MaterialTheme.colorScheme.onPrimary,
                painter = painterResource(id = coreR.drawable.channel_emotes_24),
                contentDescription = "click to scroll to channel emotes"
            )
            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                modifier = Modifier
                    .size(25.dp)
                    .clickable {
                        scrollToSharedChannelEmotes()
                        Log.d("EmoteBottomUI", "SHARED")
                    },
                tint = MaterialTheme.colorScheme.onPrimary,
                painter = painterResource(id = coreR.drawable.shared_24),
                contentDescription = "click to scroll to shared emotes"
            )
            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                modifier = Modifier
                    .size(25.dp)
                    .clickable {
                        scrollToGlobalEmotes()
                        Log.d("EmoteBottomUI", "GLOBAL")
                    },
                tint = MaterialTheme.colorScheme.onPrimary,
                painter = painterResource(id = coreR.drawable.world_emotes_24),
                contentDescription = "click to scroll to gloabl emotes"
            )


        }
        Box(modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color.DarkGray)
            .clickable {
                deleteEmote()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            .padding(vertical = 5.dp, horizontal = 10.dp)

        ) {
            Icon(
                modifier = Modifier.size(25.dp),
                tint = MaterialTheme.colorScheme.onPrimary,
                painter = painterResource(id = coreR.drawable.baseline_backspace_24),
                contentDescription = "click to delete emote"
            )
        }


    }
}


/**
 * - restartable
 * - skippable
 * */
@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
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
        columns = GridCells.Adaptive(minSize = 60.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp)
            .height(250.dp)
            .background(MaterialTheme.colorScheme.primary),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        /*****************************START OF THE Most Recent EMOTES*******************************/
        /*****************************START OF THE Most Recent EMOTES*******************************/
        header {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp)
            ) {
                Spacer(modifier = Modifier.padding(5.dp))
                Text(
                    "Frequently Used Emotes",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize
                ) // or any composable for your single row
                Spacer(modifier = Modifier.padding(2.dp))
                Divider(
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.padding(5.dp))
            }

        }

        items(
            emoteBoardMostFrequentList.list,
        ) {
            AsyncImage(
                model = it.url,
                contentDescription = it.name,
                modifier = Modifier
                    .width(60.dp)
                    .height(60.dp)
                    .padding(5.dp)
                    .clickable {
                        updateTextWithEmote(it.name)
                    }
            )
        }


        /*****************************START OF THE Channel EMOTES*******************************/


        /*****************************START OF THE Channel EMOTES*******************************/
        header {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp)
            ) {
                Spacer(modifier = Modifier.padding(5.dp))
                Text(
                    "Channel Emotes",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize
                ) // or any composable for your single row
                Spacer(modifier = Modifier.padding(2.dp))
                Divider(
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.padding(5.dp))
            }

        }
//
        items(emoteBoardChannelList.list) {
            if (it.emoteType == EmoteTypes.SUBS && !userIsSub) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(60.dp)
                ) {
                    AsyncImage(
                        model = it.url,
                        contentDescription = it.name,
                        modifier = Modifier
                            .width(60.dp)
                            .height(60.dp)
                            .padding(5.dp)
                    )
                    Spacer(
                        modifier = Modifier
                            .height(60.dp)
                            .width(60.dp)
                            .background(Color.Black.copy(0.4f))
                    )
                    Icon(
                        painter = painterResource(id = coreR.drawable.lock_24),
                        contentDescription = "Emote locked. Subscribe to access",
                        modifier = Modifier
                            .width(18.dp)
                            .height(18.dp)
                            .align(Alignment.BottomEnd)
                            .padding(3.dp),
                        tint = Color.White
                    )

                }

            } else {
                AsyncImage(
                    model = it.url,
                    contentDescription = it.name,
                    modifier = Modifier
                        .width(60.dp)
                        .height(60.dp)
                        .padding(5.dp)
                        .clickable {
                            updateTextWithEmote(it.name)
                            updateTempararyMostFrequentEmoteList(
                                EmoteNameUrl(it.name, it.url)
                            )
                        }
                )
            }

//            Icon(
//                painter = rememberAsyncImagePainter(it.url),
//                contentDescription = null,
//                modifier = Modifier.size(42.dp),
//                tint = Color.Unspecified
//            )
        }
//        /*****************************START OF THE GLOBAL EMOTES*******************************/
        header {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp)
            ) {
                Spacer(modifier = Modifier.padding(5.dp))
                Text(
                    "Global Emotes",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize
                ) // or any composable for your single row
                Spacer(modifier = Modifier.padding(2.dp))
                Divider(
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.padding(5.dp))
            }

        }

        items(emoteBoardGlobalList.list) {
//            Log.d("GlobalEmotesLoaded","name ->${it.name}")
//            Log.d("GlobalEmotesLoaded","url ->${it.url}")
//
            AsyncImage(
                model = it.url,
                contentDescription = it.name,
                modifier = Modifier
                    .width(60.dp)
                    .height(60.dp)
                    .padding(5.dp)
                    .clickable {
                        updateTextWithEmote(it.name)
                        updateTempararyMostFrequentEmoteList(
                            EmoteNameUrl(it.name, it.url)
                        )
                    }
            )
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
        twitchUserChat: ImmutableList<TwitchUserData>,
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
        fullMode: Boolean = false,
        setDragging: () -> Unit = {},
    ) {
        val coroutineScope = rememberCoroutineScope()
        LazyColumn(
            modifier = modifier,
            state = lazyColumnListState
        ) {
            coroutineScope.launch {
                if (autoscroll) {
                    lazyColumnListState.scrollToItem(twitchUserChat.size)
                }
            }
            if (fullMode) {
                stickyHeader {
                    Text(
                        "Chat",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondary) //todo: this is what I want to change
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
                items = twitchUserChat,
            ) { item ->
                if (item != null) {
                    Log.d("CLickingCardCheck", "${item.userType}")
                    ChatMessages(
                        item,
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
                        inlineContentMap = inlineContentMap

                    )
                }
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
        inlineContentMap: EmoteListMap
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
                                    inlineContentMap = inlineContentMap
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
                            inlineContentMap = inlineContentMap
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
                                        inlineContentMap = inlineContentMap
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
                                inlineContentMap = inlineContentMap
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
    inlineContentMap: EmoteListMap


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
                    inlineContentMap = inlineContentMap

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
    hideIconColor: Color = MaterialTheme.colorScheme.primary,
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
    stylizedTextField: @Composable (modifier: Modifier) -> Unit,
    showIconBasedOnTextLength: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 4.dp)
    ) {
        filteredRow()
        HorizontalDivider()
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            showModStatus()
            stylizedTextField(Modifier.weight(2f).padding(start = 8.dp))
            showIconBasedOnTextLength()
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
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary),
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
    LazyRow(modifier = Modifier.padding(vertical = 10.dp)) {
        items(filteredChatListImmutable.chatList) {
            ClickedAutoText(
                clickedAutoCompleteText = { username -> clickedAutoCompleteText(username) },
                username = it
            )
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
@OptIn(ExperimentalComposeUiApi::class)
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
    //todo: NOW I NEED TO MAKE THE EMOTE BOARD AND KEYBOARD SHOW AT ALL
    Log.d("StylizedTextFieldRecomp", "RECOMP")

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
                    .focusRequester(focusRequester),
                singleLine = false,
                maxLines = 5,
                value = actualTextFieldValue,

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
                    Text(stringResource(coreR.string.send_a_message), color = Color.White)
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

    if (BuildConfig.BUILD_TYPE == "debug") {
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
                    androidx.compose.material.Text(
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
 * @param twitchUser a [TwitchUserData][com.example.clicker.network.websockets.models.TwitchUserData] object that represents the state of an individual user and their chat message
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
 * @param twitchUser a [TwitchUserData][com.example.clicker.network.websockets.models.TwitchUserData] object that represents the state of an individual user and their chat message
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
    inlineContentMap: EmoteListMap

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
            badgeList = twitchUser.badges

        )

    } // end of the row
}

/**
 * - restartable
 * */
/**
 *
 * ChatBadges is the composable that is responsible for showing the chat badges(mod or sub) beside the users username.
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
    inlineContentMap: EmoteListMap
) {

    Log.d("ChatBadgesusername", "username->$messageList")

    //moderator subscriber
    Log.d("LoggingBadges", "list -> $badgeList")

    val text = buildAnnotatedString {

        for (item in badgeList) {
            if (inlineContentMap.map.containsKey(item)) {
                withStyle(style = SpanStyle(fontSize = 10.sp)) {
                    appendInlineContent(item, item)
                }
            }
        }

        withStyle(
            style = SpanStyle(
                color = color,
                fontSize = textSize,
                fontWeight = FontWeight.Bold
            )
        ) {
            append("$username ")
        }
        //todo:below should get replaced with the new messageList
        for (messageToken in messageList) {
            if (messageToken.messageType == PrivateMessageType.MESSAGE) {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
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
            inlineContent = inlineContentMap.map,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            color = color,
            fontSize = textSize
        )
    }
}
