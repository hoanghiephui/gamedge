package com.game.feature.streaming

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.paulrybitskyi.gamedge.common.domain.websockets.ModActionData
import com.game.feature.streaming.entities.AutoModMessageListImmutableCollection
import com.game.feature.streaming.entities.ListTitleValue
import com.game.feature.streaming.entities.ModActionListImmutableCollection
import com.game.feature.streaming.entities.ModViewStatus
import com.game.feature.streaming.entities.ModViewViewModelUIState
import com.game.feature.streaming.entities.RequestIds
import com.game.feature.streaming.entities.followerModeList
import com.game.feature.streaming.entities.slowModeList
import com.android.model.WebSocketResponse
import com.paulrybitskyi.gamedge.common.domain.live.entities.StreamPlaybackAccessToken
import com.paulrybitskyi.gamedge.common.domain.websockets.AutoModQueueMessage
import com.paulrybitskyi.gamedge.common.domain.websockets.TwitchEventSubscriptionWebSocket
import com.paulrybitskyi.gamedge.common.ui.base.BaseViewModel
import com.paulrybitskyi.gamedge.common.ui.base.events.STREAMING_KEY
import com.paulrybitskyi.gamedge.core.sharers.TextSharer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ModViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private var textSharer: TextSharer,
    private val twitchEventSubWebSocket: TwitchEventSubscriptionWebSocket,
    //private val twitchEventSub: TwitchEventSubscriptions,
) : BaseViewModel() {
    val streamPlaybackAccessToken: StreamPlaybackAccessToken =
        checkNotNull(savedStateHandle[STREAMING_KEY])

    private var _requestIds: MutableState<RequestIds> = mutableStateOf(RequestIds())

    /**modActionsList START*/
    val modActionsList = mutableStateListOf<ModActionData>()

    // Immutable state holder
    private var _modActionListImmutableCollection by mutableStateOf(
        ModActionListImmutableCollection(modActionsList)
    )

    // Publicly exposed immutable state as State
    val modActionListImmutable: State<ModActionListImmutableCollection>
        get() = mutableStateOf(_modActionListImmutableCollection)

    //Now I need to implement the methods
    private fun addAllModActionList(actionList: List<ModActionData>) {
        modActionsList.addAll(actionList)
        _modActionListImmutableCollection = ModActionListImmutableCollection(modActionsList)
    }

    private fun clearModActionList() {
        modActionsList.clear()
        _modActionListImmutableCollection = ModActionListImmutableCollection(modActionsList)
    }

    /**modActionsList END*/
    private val _uiState: MutableState<ModViewViewModelUIState> = mutableStateOf(ModViewViewModelUIState())
    val uiState: State<ModViewViewModelUIState> = _uiState

    /*****autoModMessageList START*****/
    val autoModMessageList = mutableStateListOf<AutoModQueueMessage>()

    // Immutable state holder
    private var _autoModMessageListImmutableCollection by mutableStateOf(
        AutoModMessageListImmutableCollection(autoModMessageList)
    )

    private fun addAllAutoModMessageList(commands: List<AutoModQueueMessage>) {
        autoModMessageList.addAll(commands)
        _autoModMessageListImmutableCollection = AutoModMessageListImmutableCollection(autoModMessageList)

    }

    private var _modViewStatus: MutableState<ModViewStatus> = mutableStateOf(ModViewStatus())
    val modViewStatus: State<ModViewStatus> = _modViewStatus

    fun shareStreaming(
        context: Context,
        content: String
    ) {
        textSharer.share(context, content)
    }

    init {
        monitorForSessionId()
        //monitorForAutoModMessages()
        monitorForAutoModMessageUpdates()
        monitorForModActions()
        monitorForChatSettingsUpdate()
    }

    fun createNewTwitchEventWebSocket() {
        // modActionsList.clear()
        clearModActionList()
        Log.d("CREATINGNEWEVENTSUBSOCKET", "CREATED")
        twitchEventSubWebSocket.newWebSocket()
    }

    private fun monitorForModActions() = viewModelScope.launch(Dispatchers.IO) {
        twitchEventSubWebSocket.modActions.collect { nullableModAction ->
            nullableModAction?.also { nonNullableModAction ->
                Log.d("ModActionsHappending", "action ->$nonNullableModAction")
                // modActionsList.add(nonNullableModAction)
                addAllModActionList(listOf(nonNullableModAction))
                if (_uiState.value.modActionNotifications) {
                    val updatedMessage = _uiState.value.modViewTotalNotifications + 1
                    _uiState.value = _uiState.value.copy(
                        modViewTotalNotifications = updatedMessage
                    )
                }
            }
        }
    }

    fun clearModViewNotifications() {
        _uiState.value = _uiState.value.copy(
            modViewTotalNotifications = 0
        )
    }

    fun changeAutoModQueueChecked(value: Boolean) {
        _uiState.value = _uiState.value.copy(
            autoModMessagesNotifications = value
        )
    }

    fun changeModActionsChecked(value: Boolean) {
        _uiState.value = _uiState.value.copy(
            modActionNotifications = value
        )
    }

    private fun monitorForChatSettingsUpdate() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                twitchEventSubWebSocket.updatedChatSettingsData.collect { nullableChatData ->
                    nullableChatData?.also { chatSettingsData ->
                        checkSlowModeWaitTime(chatSettingsData.slowModeWaitTime)
                        checkFollowerModeDuration(chatSettingsData.followerModeDuration)
                        Log.d("monitorForChatSettingsUpdate", "emoteMode--> ${chatSettingsData.emoteMode}")
                        Log.d("monitorForChatSettingsUpdate", "subscriberMode--> ${chatSettingsData.subscriberMode}")

                        _uiState.value = _uiState.value.copy(
                            chatSettings = _uiState.value.chatSettings.copy(
                                slowMode = chatSettingsData.slowMode,
                                slowModeWaitTime = chatSettingsData.slowModeWaitTime,
                                followerMode = chatSettingsData.followerMode,
                                followerModeDuration = chatSettingsData.followerModeDuration,
                                subscriberMode = chatSettingsData.subscriberMode,
                                emoteMode = chatSettingsData.emoteMode,
                            ),
                            //todo: remove the two below-> this is a hotfix and should be reworked
                            emoteOnly = chatSettingsData.emoteMode,
                            subscriberOnly = chatSettingsData.subscriberMode,
                        )

                    }
                }
            }
        }
    }

    /**
     * This is the function that calls createEventSubSubscription()
     * */
    private fun monitorForSessionId() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                twitchEventSubWebSocket.parsedSessionId.collect { nullableSessionId ->
                    nullableSessionId?.also { sessionId ->
                        Log.d("monitorForSessionId", "sessionId --> $sessionId")
                        _requestIds.value = _requestIds.value.copy(
                            sessionId = sessionId
                        )
                        createSubscriptionEvents(
                            moderatorActionSubscription = {
                                createModerationActionSubscription()
                            },
                            autoModMessageUpdateSubscription = {
                                //createAutoModMessageUpdateSubscriptionEvent() // This is registering but not updating the UI
                            },
                            autoModMessageHoldSubscription = {
                                //createAutoModMessageHoldSubscriptionEvent()  // This is registering but not updating the UI
                            },
                            chatSettingsSubscription = {
                                //createChatSettingsSubscriptionEvent()
                            }
                        )
                        //then with this session Id we need to make a call to subscribe to our event


                    }
                }
            }

        }
    }

    /**
     * - createSubscriptionEvents() is a private function that calls all the methods that are making EventSub subscriptions.
     * - You can read more about EventSub subscriptions. [HERE](https://dev.twitch.tv/docs/eventsub/)
     *
     * @param moderatorActionSubscription full description [HERE][createModerationActionSubscription]
     * @param autoModMessageUpdateSubscription full description [HERE][createAutoModMessageUpdateSubscriptionEvent]
     * @param autoModMessageHoldSubscription full description [HERE][createAutoModMessageHoldSubscriptionEvent]
     * @param chatSettingsSubscription full description [HERE][createChatSettingsSubscriptionEvent]
     * */
    private fun createSubscriptionEvents(
        moderatorActionSubscription: () -> Unit,
        autoModMessageUpdateSubscription: () -> Unit,
        autoModMessageHoldSubscription: () -> Unit,
        chatSettingsSubscription: () -> Unit,
    ) {
        moderatorActionSubscription()
        autoModMessageUpdateSubscription()
        autoModMessageHoldSubscription()
        chatSettingsSubscription()
    }

    /**
     * monitorForAutoModMessageUpdates monitors the updates to current automod messages.
     * It will also minus 1 to the `_uiState.value.autoModQuePedingMessages` state
     *
     * */
    private fun monitorForAutoModMessageUpdates() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                twitchEventSubWebSocket.messageIdForAutoModQueue.collect { nullableAutoModMessage ->
                    nullableAutoModMessage?.also { autoModMessage ->
                        Log.d(
                            "monitorForAutoModMessageUpdates",
                            "autoModMessage -->$autoModMessage"
                        )
                        val item =
                            autoModMessageList.find { it.messageId == autoModMessage.messageId }
                        item?.also {
                            val indexOfItem = autoModMessageList.indexOf(item)
                            autoModMessageList[indexOfItem] = item.copy(
                                approved = autoModMessage.approved,
                                swiped = true
                            )
                        }
                        addAllAutoModMessageList(autoModMessageList.toList())
                    }
                }
            }
        }
    }

    /**
     * - createModerationActionSubscription is a private function that is meant to establish a EventSub subsctiption type of `channel.moderate`. This will send a
     * notification when a moderator performs a moderation action in a channel.
     * - You can read more about this subscription type on Twitch's documentation site, [HERE](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelmoderate)
     * */
    private fun createModerationActionSubscription() {
        viewModelScope.launch(Dispatchers.IO) {
            _modViewStatus.value = _modViewStatus.value.copy(
                modActions = WebSocketResponse.Loading
            )
            Log.d("createModerationActionSubscriptionTESTING", "oAuthToken->${_requestIds.value.oAuthToken}")
            Log.d("createModerationActionSubscriptionTESTING", "clientId->${_requestIds.value.clientId}")
            Log.d("createModerationActionSubscriptionTESTING", "broadcasterId->${_requestIds.value.broadcasterId}")
            Log.d("createModerationActionSubscriptionTESTING", "moderatorId->${_requestIds.value.moderatorId}")
            Log.d("createModerationActionSubscriptionTESTING", "sessionId->${_requestIds.value.sessionId}")

            /*twitchEventSub.createEventSubSubscription(
                oAuthToken = _requestIds.value.oAuthToken,
                clientId = _requestIds.value.clientId,
                broadcasterId = _requestIds.value.broadcasterId,
                moderatorId = _requestIds.value.moderatorId,
                sessionId = _requestIds.value.sessionId,
                type = "channel.moderate"
            ).collect { response ->
                when (response) {
                    is WebSocketResponse.Loading -> {}
                    is WebSocketResponse.Success -> {
                        Log.d("createModerationActionSubscriptionTESTING","Success")
                        _modViewStatus.value = _modViewStatus.value.copy(
                            modActions = WebSocketResponse.Success(true)
                        )
                        _uiState.value = _uiState.value.copy(
                            showSubscriptionEventError = Response.Success(true)
                        )
                    }

                    is WebSocketResponse.Failure -> {
                        Log.d("createModerationActionSubscriptionTESTING","Failure")
                        _uiState.value = _uiState.value.copy(
                            showSubscriptionEventError = Response.Failure(response.e)
                        )
                        _modViewStatus.value = _modViewStatus.value.copy(
                            modActions = WebSocketResponse.Failure(Exception("failed to register subscription"))
                        )
                    }
                    is WebSocketResponse.FailureAuth403 ->{
                        Log.d("createModerationActionSubscriptionTESTING","FailureAuth403")
                        _modViewStatus.value = _modViewStatus.value.copy(
                            modActions = WebSocketResponse.FailureAuth403(Exception("Improper Exception"))
                        )
                    }
                }
            }*/
        }

    }

    /**
     * checkSlowModeWaitTime private function used to set the value of the selectedSlowMode ui state
     * */
    private fun checkSlowModeWaitTime(
        slowModeWaitTime: Int?,
    ) {
        try {
            val foundSlowItem = slowModeList.first { it.value == slowModeWaitTime }
            _uiState.value = _uiState.value.copy(
                selectedSlowMode = foundSlowItem
            )

        } catch (e: NoSuchElementException) {
            _uiState.value = _uiState.value.copy(
                selectedSlowMode = ListTitleValue("Custom", slowModeWaitTime)
            )

        }
    }

    /**
     * checkFollowerModeDuration private function used to set the value of the selectedFollowerMode ui state
     * */
    private fun checkFollowerModeDuration(followerModeDuration: Int?) {
        try {
            val foundFollowerModeDuration = followerModeList.first { it.value == followerModeDuration }
            _uiState.value = _uiState.value.copy(
                selectedFollowerMode = foundFollowerModeDuration,
            )

        } catch (e: NoSuchElementException) {
            _uiState.value = _uiState.value.copy(
                selectedFollowerMode = ListTitleValue("Custom", followerModeDuration)
            )

        }
    }

    override fun onCleared() {
        super.onCleared()
        twitchEventSubWebSocket.closeWebSocket()
        Log.d("ModViewViewModel", "onCleared()")
    }
}
