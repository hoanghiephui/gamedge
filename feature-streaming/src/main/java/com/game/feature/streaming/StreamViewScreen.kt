package com.game.feature.streaming

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.hilt.navigation.compose.hiltViewModel
import com.game.feature.streaming.component.ChatSettingsV1Column
import com.game.feature.streaming.component.chat.ChatView
import com.game.feature.streaming.entities.AdvancedChatSettings
import com.game.feature.streaming.entities.FilteredChatListImmutableCollection
import com.game.feature.streaming.entities.ForwardSlashCommandsImmutableCollection
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteListMap
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteNameUrlEmoteTypeList
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteNameUrlList
import com.paulrybitskyi.gamedge.common.domain.chat.IndivBetterTTVEmoteList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamViewScreen(
    streamViewModel: StreamingViewModel = hiltViewModel(),
) {
    val twitchUserChat = streamViewModel.listChats.toList()
    val outerBottomModalState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(true) }
    val configuration = LocalConfiguration.current
    var orientation by remember { mutableIntStateOf(Configuration.ORIENTATION_PORTRAIT) }
    val advancedChatSettingsState by streamViewModel.advancedChatSettingsState
    val openWarningDialog by streamViewModel.openWarningDialog

    LaunchedEffect(configuration) {
        // Save any changes to the orientation value on the configuration object
        snapshotFlow { configuration.orientation }
            .collect { orientation = it }
    }

    when (orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {

        }

        else -> {
            if (showBottomSheet) {
                Box(modifier = Modifier.fillMaxSize()) {
                    ChatView(
                        modifier = Modifier,
                        twitchUserChat = twitchUserChat,
                        showBottomModal = {
                            //showClickedUserBottomModal()
                        },
                        updateClickedUser = { username, userId, banned, isMod ->
                            /*updateClickedUser(
                                username,
                                userId,
                                banned,
                                isMod
                            )*/
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

                        isMod = streamViewModel.state.value.loggedInUserData?.mod ?: false,
                        clickedAutoCompleteText = { username ->
                            //streamViewModel.autoTextChange(username)
                        },
                        showModal = {
                            //showChatSettingsBottomModal()
                        },
                        notificationAmount = 1,//modViewViewModel.uiState.value.modViewTotalNotifications,
                        textFieldValue = streamViewModel.textFieldValue,
                        sendMessageToWebSocket = { message ->
                            //sendMessageToWebSocket(message)
                        },
                        noChat = streamViewModel.advancedChatSettingsState.value.noChatMode,
                        deleteChatMessage = { /*messageId -> streamViewModel.deleteChatMessage(messageId)*/ },
                        clickedCommandAutoCompleteText = { clickedValue ->
                            //clickedCommandAutoCompleteText(clickedValue)

                        },
                        inlineContentMap = EmoteListMap(emptyMap()),//streamViewModel.inlineTextContentTest.value,
                        hideSoftKeyboard = {

                        },
                        emoteBoardGlobalList = EmoteNameUrlList(),//streamViewModel.globalEmoteUrlList.value,
                        //todo: this is what I need to change
                        updateTextWithEmote = { newValue -> /*updateTextWithEmote(newValue)*/ },
                        emoteBoardChannelList = EmoteNameUrlEmoteTypeList(),//streamViewModel.channelEmoteUrlList.value,
                        emoteBoardMostFrequentList = EmoteNameUrlList(),//streamViewModel.mostFrequentEmoteListTesting.value,
                        deleteEmote = {
                            //deleteEmote() // this needs to be changed
                        },
                        showModView = {
                            //showModView()
                            //clearModViewNotifications()
                        },
                        updateTempararyMostFrequentEmoteList = { value -> /*updateMostFrequentEmoteList(value)*/ },
                        globalBetterTTVEmotes = IndivBetterTTVEmoteList(),//streamViewModel.globalBetterTTVEmotes.value,
                        channelBetterTTVResponse = IndivBetterTTVEmoteList(),//streamViewModel.channelBetterTTVEmote.value,
                        sharedBetterTTVResponse = IndivBetterTTVEmoteList(),//streamViewModel.sharedChannelBetterTTVEmote.value,
                        userIsSub = streamViewModel.state.value.loggedInUserData?.sub ?: false,
                        forwardSlashes = ForwardSlashCommandsImmutableCollection(emptyList()),//streamViewModel.forwardSlashCommandImmutable.value,
                        filteredChatListImmutable = FilteredChatListImmutableCollection(emptyList()),//streamViewModel.filteredChatListImmutable.value,
                        actualTextFieldValue = streamViewModel.textFieldValue.value,
                        changeActualTextFieldValue = { text, textRange ->
                            //changeActualTextFieldValue(text, textRange)
                        }
                    )
                    OrientationPortraitScreen(
                        sheetState = outerBottomModalState,
                        onDismissRequest = {
                            showBottomSheet = false
                        },
                        advancedChatSettings = advancedChatSettingsState,
                        onChangeAdvancedChatSettings = {},
                        onChangeNoChatMode = { },
                    )

                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrientationPortraitScreen(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    advancedChatSettings: AdvancedChatSettings,
    onChangeAdvancedChatSettings: (AdvancedChatSettings) -> Unit,
    onChangeNoChatMode: (Boolean) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        ChatSettingsV1Column(
            advancedChatSettings = advancedChatSettings,
            changeAdvancedChatSettings = onChangeAdvancedChatSettings,
            changeNoChatMode = onChangeNoChatMode
        )
    }
}
