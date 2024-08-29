package com.game.feature.streaming

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.android.model.IndivBetterTTVEmote
import com.android.model.Response
import com.android.model.websockets.ChatSettingsData
import com.android.model.websockets.MessageType
import com.game.feature.streaming.entities.AdvancedChatSettings
import com.game.feature.streaming.entities.AutoCompleteChat
import com.game.feature.streaming.entities.ClickedUIState
import com.game.feature.streaming.entities.ClickedUserBadgesImmutable
import com.game.feature.streaming.entities.ClickedUserNameChats
import com.game.feature.streaming.entities.ClickedUsernameChatsWithDateSentImmutable
import com.game.feature.streaming.entities.StreamUIState
import com.game.feature.streaming.entities.TextParsing
import com.paulrybitskyi.gamedge.common.data.websockets.MessageScanner
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteNameUrl
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteNameUrlList
import com.paulrybitskyi.gamedge.common.domain.common.extensions.resultOrError
import com.paulrybitskyi.gamedge.common.domain.games.usecases.StreamUseCase
import com.paulrybitskyi.gamedge.common.domain.live.entities.StreamPlaybackAccessToken
import com.paulrybitskyi.gamedge.common.domain.live.usecases.TwitchEmoteUseCase
import com.paulrybitskyi.gamedge.common.domain.websockets.MessageToken
import com.paulrybitskyi.gamedge.common.domain.websockets.PrivateMessageType
import com.paulrybitskyi.gamedge.common.domain.websockets.Scanner
import com.paulrybitskyi.gamedge.common.domain.websockets.TextCommands
import com.paulrybitskyi.gamedge.common.domain.websockets.TokenCommand
import com.paulrybitskyi.gamedge.common.domain.websockets.TokenMonitoring
import com.paulrybitskyi.gamedge.common.domain.websockets.TwitchSocket
import com.paulrybitskyi.gamedge.common.domain.websockets.TwitchUserData
import com.paulrybitskyi.gamedge.common.domain.websockets.TwitchUserDataObjectMother
import com.paulrybitskyi.gamedge.common.ui.base.BaseViewModel
import com.paulrybitskyi.gamedge.common.ui.base.events.STREAMING_KEY
import com.paulrybitskyi.gamedge.common.ui.base.events.common.GeneralCommand
import com.paulrybitskyi.gamedge.core.Dispatcher
import com.paulrybitskyi.gamedge.core.ErrorMapper
import com.paulrybitskyi.gamedge.core.Logger
import com.paulrybitskyi.gamedge.core.NiaDispatchers
import com.paulrybitskyi.gamedge.core.sharers.TextSharer
import com.paulrybitskyi.gamedge.core.utils.onError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class StreamingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private var textSharer: TextSharer,
    private val webSocket: TwitchSocket,
    private val autoCompleteChat: AutoCompleteChat,
    private val tokenMonitoring: TokenMonitoring = TokenMonitoring(),
    private val tokenCommand: TokenCommand = TokenCommand(),
    private val textParsing: TextParsing = TextParsing(),
    private val streamUseCase: StreamUseCase,
    private val errorMapper: ErrorMapper,
    private val logger: Logger,
    @Dispatcher(NiaDispatchers.IO)
    private val ioDispatcher: CoroutineDispatcher,
    private val twitchEmoteUseCase: TwitchEmoteUseCase
) : BaseViewModel() {
    val streamPlaybackAccessToken: StreamPlaybackAccessToken =
        checkNotNull(savedStateHandle[STREAMING_KEY])

    fun shareStreaming(
        context: Context,
        content: String
    ) {
        textSharer.share(context, content)
    }

    private var _uiState: MutableState<StreamUIState> = mutableStateOf(StreamUIState())
    val state: State<StreamUIState> = _uiState

    private var _uiStateProfile: MutableStateFlow<StreamPlaybackAccessToken> = MutableStateFlow(streamPlaybackAccessToken)
    val uiStateProfile = _uiStateProfile.asStateFlow()

    /********THIS IS ALL THE EMOTE RELATED CALLS**************************************/
    val inlineTextContentTest = twitchEmoteUseCase.emoteList
    val globalEmoteUrlList = twitchEmoteUseCase.emoteBoardGlobalList
    val channelEmoteUrlList = twitchEmoteUseCase.emoteBoardChannelList
    val badgeListMap = twitchEmoteUseCase.globalChatBadges

    private val _globalBetterTTVEmotes: MutableState<Response<List<IndivBetterTTVEmote>>> =
        mutableStateOf(Response.Loading)

    val globalBetterTTVEmotes = twitchEmoteUseCase.globalBetterTTVEmotes
    val channelBetterTTVEmote = twitchEmoteUseCase.channelBetterTTVEmotes
    val sharedChannelBetterTTVEmote = twitchEmoteUseCase.sharedBetterTTVEmotes

    /**
     * The name of the channel that this chat is connecting to
     * */
    private val _channelName: MutableStateFlow<String?> = MutableStateFlow(null)
    val channelName: StateFlow<String?> = _channelName

    /**THis is the data for the new filter methods*/
    private val _idOfLatestBan = mutableStateOf("")
    private val _clickedUIState = mutableStateOf(ClickedUIState())
    val clickedUIState = _clickedUIState
    val clickedUsernameChatsWithDateSent = mutableStateListOf<ClickedUserNameChats>()
    val clickedUserBadges = mutableStateListOf<String>() //this needs to be make stable
    private var _clickedUserBadgesImmutable by mutableStateOf(
        ClickedUserBadgesImmutable(clickedUserBadges)
    )
    // Publicly exposed immutable state as State
    val clickedUserBadgesImmutable: State<ClickedUserBadgesImmutable>
        get() = mutableStateOf(_clickedUserBadgesImmutable)
    private fun addAllClickedUserBadgesImmutable(clickedBadges:List<String>){
        clickedUserBadges.addAll(clickedBadges)
        _clickedUserBadgesImmutable = ClickedUserBadgesImmutable(clickedUserBadges)
    }
    private fun clearAllClickedUserBadgesImmutable(){
        clickedUserBadges.clear()
        _clickedUserBadgesImmutable = ClickedUserBadgesImmutable(listOf())
    }

    /**
     * I need to make the immutable version of clickedUsernameChatsWithDateSent
     * */
    // this is the immutable clickedUsernameChatsWithDateSentImmutable
    // Immutable state holder
    private var _clickedUsernameChatsDateSentImmutable by mutableStateOf(
        ClickedUsernameChatsWithDateSentImmutable(clickedUsernameChatsWithDateSent)
    )

    // Publicly exposed immutable state as State
    val clickedUsernameChatsDateSentImmutable: State<ClickedUsernameChatsWithDateSentImmutable>
        get() = mutableStateOf(_clickedUsernameChatsDateSentImmutable)

    private fun addAllClickedUsernameChatsDateSent(clickedChats: List<ClickedUserNameChats>) {
        clickedUsernameChatsWithDateSent.addAll(clickedChats)
        _clickedUsernameChatsDateSentImmutable =
            ClickedUsernameChatsWithDateSentImmutable(clickedUsernameChatsWithDateSent)

    }

    private fun clearClickedUsernameChatsDateSent() {
        clickedUsernameChatsWithDateSent.clear()
        _clickedUsernameChatsDateSentImmutable = ClickedUsernameChatsWithDateSentImmutable(listOf())

    }

    private val allChatters = mutableStateListOf<String>()

    private val monitoredUsers = mutableStateListOf<String>()

    /**
     * updateAdvancedChatSettings is used to update the [_advancedChatSettingsState] UI state
     *
     * @param advancedChatSettings the new state that will now represent the [_advancedChatSettingsState] UI state
     */
    fun updateAdvancedChatSettings(advancedChatSettings: AdvancedChatSettings) {
        _advancedChatSettingsState.value = advancedChatSettings
    }

    fun setNoChatMode(status: Boolean) {
        _advancedChatSettingsState.value = _advancedChatSettingsState.value.copy(
            noChatMode = status
        )
        if (status) {
            webSocket.close()
            listChats.clear()

        } else {
            startWebSocket(channelName.value ?: "")
        }
        viewModelScope.launch {
            delay(200)
            listChats.clear()
        }
    }

    /**
     * autoTextChangeCommand is function that is used to change the value of [textFieldValue] with [command]
     *
     * @param command  a string meant to represent the slash command that was clicked on by the user
     * */
    fun clickedCommandAutoCompleteText(command: String) {
        textParsing.clickSlashCommandTextAutoChange(
            command = command,
        )
    }

    /**
     * autoTextChange is function that is used to change the value of [textFieldValue] with [username]
     *
     * @param username  a string meant to represent the username that was clicked on by the user
     * */
    fun autoTextChange(username: String) {
        textParsing.clickUsernameAutoTextChange(
            username = username,
        )
    }

    fun addEmoteToText(emoteText: String) {
        textParsing.updateTextField(" $emoteText ")
    }

    fun updateTemporaryMostFrequentList(clickedItem: EmoteNameUrl) {
        if (!temporaryMostFrequentList.contains(clickedItem)) {
            temporaryMostFrequentList.add(clickedItem)
        }
        Log.d("updateTemporaryMostFrequentList", "list ->${temporaryMostFrequentList.toList()}")
    }

    fun deleteEmote() {
        Log.d("addToken", "deleteEmote()")
        textParsing.deleteEmote(inlineTextContentTest.value.map)
    }

    /**
     * A list representing all the chats users have sent
     * */
    val listChats = mutableStateListOf<TwitchUserData>()

    /**
     * A list representing all the actions taken by moderators
     * */
    val modActionList = mutableStateListOf<TwitchUserData>()

    /**
     * The UI state that represents all the data meant for the composable
     * */
    private val _advancedChatSettingsState = mutableStateOf(AdvancedChatSettings())
    val advancedChatSettingsState = _advancedChatSettingsState
    val textFieldValue: MutableState<TextFieldValue> = textParsing.textFieldValue
    val openWarningDialog = mutableStateOf(false)
    fun changeOpenWarningDialog(newValue: Boolean) {
        openWarningDialog.value = newValue
    }

    /*****LOW POWER MODE******/
    private var _lowPowerModeActive: MutableState<Boolean> = mutableStateOf(false)
    val lowPowerModeActive: State<Boolean> = _lowPowerModeActive
    fun changeLowPowerModeActive(newValue: Boolean) {
        _lowPowerModeActive.value = newValue
    }

    /**
     * A list representing all the most recent clicked emotes
     * */
    //todo: mutableStateOf<EmoteNameUrlList>(EmoteNameUrlList())
    val mostFrequentEmoteList = mutableStateListOf<EmoteNameUrl>()
    val mostFrequentEmoteListTesting = mutableStateOf(EmoteNameUrlList())
    val temporaryMostFrequentList = mutableStateListOf<EmoteNameUrl>()
    val filteredChatListImmutable = textParsing.filteredChatListImmutable

    val forwardSlashCommandImmutable = textParsing.forwardSlashCommandsState

    init {
        viewModelScope.launch {
            webSocket.hasWebSocketFailed.collect { nullableValue ->
                nullableValue?.also { value ->
                    if (value && _uiState.value.networkStatus == true) {
                        //todo: CHECK IF THERE IS AN INTERNET CONNECTION
                        listChats.add(noInternetErrorValue)
                    } else {
                        listChats.add(errorValue)
                    }
                }

            }
        }
    }

    init {
        val username = _uiState.value.login
        val channelName = _channelName.value ?: ""
        webSocket.run(channelName, username)
    }

    init {
        monitorForLatestBannedMessageId()
    }

    init {
        monitorForLatestBannedUserId()
    }

    init {
        monitorForLoggedInUserData()
    }

    init {
        monitorSocketForChatMessages()
    }

    init {
        monitorForChannelName()
    }

    init {
        monitorSocketRoomState()
    }

    /**
     * updateChannelNameAndClientIdAndUserId is the method that gets called whenever the user clicks on a stream title when
     * they want to navigate to the streamer's page. It updates the ***clientId*** ***broadcasterId*** and ***userId***
     * */
    fun updateChannelNameAndClientIdAndUserId(
        channelName: String,
        clientId: String,
        broadcasterId: String,
        userId: String,
        login: String
    ) {
        Log.d("updateChannelNameAndClientIdAndUserId", "broadcasterId --->${broadcasterId}")
        _channelName.tryEmit(channelName)
        //startWebSocket(channelName)

        _uiState.value = _uiState.value.copy(
            clientId = clientId,
            broadcasterId = broadcasterId,
            userId = userId,
            login = login
        )

        // getChatSettings(broadcasterId)
        listChats.clear()
        getUserInformation(broadcasterId)
    }

    private fun getUserInformation(userId: String) {
        streamUseCase.getUserInformation(userId)
            .resultOrError()
            .onError {
                logger.error(logTag, "Failed to UserInformation", it)
                dispatchCommand(GeneralCommand.ShowLongToast(errorMapper.mapToMessage(it)))
            }.distinctUntilChanged()
            .onEach { emittedUiState ->
                logger.info(logTag, "UserInformation ${emittedUiState.data.first().id}")
                _uiStateProfile.update {
                    it.copy(
                        thumbnailProfile = emittedUiState.data.first().profileImageUrl
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * getChatSettings() is a private function used by [updateChannelNameAndClientIdAndUserId] and [retryGettingChatSetting] to
     * get the chat settings of the current channel the viewer is viewing
     * */
    private fun getChatSettings(
        broadcasterId: String
    ) {
        streamUseCase.getChatSettings(broadcasterId = broadcasterId)
            .resultOrError()
            .onError {
                logger.error(logTag, "Failed to ChatSettings", it)
                dispatchCommand(GeneralCommand.ShowLongToast(errorMapper.mapToMessage(it)))
            }
            .onStart {

            }
            .distinctUntilChanged()
            .onEach { emittedUiState ->
                logger.info(logTag, "Chat Settings ${emittedUiState.first().slowMode}")

            }
            .launchIn(viewModelScope)
    }

    /**
     * startWebSocket() is a private method meant to be called by methods inside of [StreamViewModel]
     * It is used to start and connect a Websocket using the [TwitchSocket]
     * */
    private fun startWebSocket(channelName: String) = viewModelScope.launch {
        Log.d("startWebSocket", "startWebSocket() is being called")

        if (_advancedChatSettingsState.value.noChatMode) {
            //this is meant to be empty to represent doing nothing and the user being in no chat mode
            //no actions are to be commited in this conditional branch
        } else {
            val username = _uiState.value.login
            webSocket.run(channelName, username)
            listChats.clear()
        }
    }

    fun sendMessage(chatMessage: String) {
        //the scanner should be inside of the tokenCommand. I should be able to just call
        //tokenCommand.checkForSlashCommands(chatMessage) and everything gets done automatically
        // Why do I even tokenCommand.tokenCommand to be a state view?
        val scanner = Scanner(chatMessage)

        scanner.scanTokens()
        val tokenList = scanner.tokenList
        // Log.d("TokenTextCommand","tokenList ->${tokenList}")
        val messageTokenList = tokenList.map { MessageToken(PrivateMessageType.MESSAGE, messageValue = it.lexeme) }
        // todo: I need to test this
        val textCommands = tokenCommand.checkForSlashCommands(tokenList)

        Log.d("TokenTextCommand", "text command username ->${textCommands.username}")
        Log.d("TokenTextCommand", "type ->${textCommands.javaClass}")


        monitorToken(
            textCommands,
            chatMessage,
            isMod = _uiState.value.loggedInUserData?.mod ?: false,
            addMessageToListChats = { message -> listChats.add(message) },
            messageTokenList = messageTokenList
        )

        // val messageResult = webSocket.sendMessage(chatMessage)
        textFieldValue.value = TextFieldValue(
            text = "",
            selection = TextRange(0)
        )

    }

    fun updateMostFrequentEmoteList() {
        //Need to do some sorting between the two
        val oldList = mostFrequentEmoteListTesting.value.list.toMutableList()
        val oldTemporaryList = temporaryMostFrequentList.filter { !oldList.contains(it) }
        val newList = oldList + oldTemporaryList
        //need to do sorting and validation checks

        mostFrequentEmoteListTesting.value = mostFrequentEmoteListTesting.value.copy(
            list = newList.toImmutableList()
        )
        temporaryMostFrequentList.clear()
    }

    fun getBetterTTVGlobalEmotes() {
        twitchEmoteUseCase.getBetterTTVGlobalEmotes()
            .resultOrError()
            .onError {
                _globalBetterTTVEmotes.value = Response.Failure(Exception("Failed"))
            }
            .onStart {
                _globalBetterTTVEmotes.value = Response.Loading
            }
            .distinctUntilChanged()
            .onEach { emittedUiState ->
                //_globalBetterTTVEmotes.value = emittedUiState
            }
            .launchIn(viewModelScope)
    }

    fun getBetterTTVChannelEmotes(broadcasterId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            twitchEmoteUseCase.getBetterTTVChannelEmotes(broadcasterId).collect {

            }
        }
    }

    /**
     * This is the function that is responsible for running the commands of the slash commands
     *
     * */
    private fun monitorToken(
        tokenCommand: TextCommands,
        chatMessage: String,
        isMod: Boolean,
        addMessageToListChats: (TwitchUserData) -> Unit,
        messageTokenList: List<MessageToken>
    ) {
        tokenMonitoring.runMonitorToken(
            tokenCommand = tokenCommand,
            chatMessage = chatMessage, isMod = isMod,
            addMessageToListChats = { message -> addMessageToListChats(message) },
            banUserSlashCommandTest = { userId, reason ->
                //banUserSlashCommand(userId, reason)
            },
            unbanUserSlashTest = { userId ->
                //unBanUserSlashCommand(userId)
            },
            getUserId = { conditional ->
                listChats.find { conditional(it) }?.userId
            },
            addToMonitorUser = { username -> monitoredUsers.add(username) },
            removeFromMonitorUser = { username -> monitoredUsers.remove(username) },
            currentUsername = _uiState.value.loggedInUserData?.displayName ?: "",
            sendToWebSocket = { message ->
                webSocket.sendMessage(message)
            },
            messageTokenList = messageTokenList,
            warnUser = { userId, reason, username -> /*warnUserSlashCommand(userId, reason, username)*/ }

        )

    }

    fun clearAllChatMessages(chatList: SnapshotStateList<TwitchUserData>) {
        chatList.clear()
        val data = TwitchUserDataObjectMother
            .addMessageType(MessageType.JOIN)
            .addUserType("Chat cleared by moderator")
            .addColor("#000000")
            .build()

        chatList.add(data)
    }

    private fun monitorForLatestBannedMessageId() {
        viewModelScope.launch {
            withContext(ioDispatcher + CoroutineName("MessageToDeleteId")) {
                webSocket.messageToDeleteId.collect { nullableMsgId ->
                    nullableMsgId?.let { nonNullMsgId ->
                        filterMessages(nonNullMsgId)
                    }
                }
            }
        }
    }

    /**
     * This is meant to monitor of the latest ban/timeout messages
     *
     * */

    private fun monitorForLatestBannedUserId() {
        viewModelScope.launch {
            webSocket.latestBannedUserId.collect { latestBannedId ->
                latestBannedId?.also {
                    Log.d("latestBannedId", "latestBannedId --> ${latestBannedId}")
                    _idOfLatestBan.value = latestBannedId
                }

            }
        }
    }

    //this function is used heavily to determine if the user is a moderator or not
    private fun monitorForLoggedInUserData() {
        viewModelScope.launch {
            webSocket.loggedInUserUiState.collect { nullableLoggedInData ->
                nullableLoggedInData?.let { LoggedInData ->
                    _uiState.value = _uiState.value.copy(
                        loggedInUserData = LoggedInData
                    )
                }
            }
        }
    }

    private fun monitorForChannelName() {
        viewModelScope.launch {
            withContext(ioDispatcher + CoroutineName("StartingWebSocket")) {
                _channelName.collect { channelName ->

                    channelName?.let {
                        startWebSocket(channelName)
                    }
                }
            }
        }
    }

    private fun monitorSocketRoomState() {
        viewModelScope.launch {
            withContext(ioDispatcher + CoroutineName("RoomState")) {
                webSocket.roomState.collect { nullableRoomState ->
                    nullableRoomState?.let { nonNullroomState ->
                        Log.d("theCurrentRoomState", "$nonNullroomState")
                        // todo: update the _uiState chatSettings with these values
                        _uiState.value = _uiState.value.copy(
                            chatSettings = Response.Success(
                                ChatSettingsData(
                                    slowMode = nonNullroomState.slowMode,
                                    slowModeWaitTime = nonNullroomState.slowModeDuration,
                                    followerMode = nonNullroomState.followerMode,
                                    followerModeDuration = nonNullroomState.followerModeDuration,
                                    subscriberMode = nonNullroomState.subMode,
                                    emoteMode = nonNullroomState.emoteMode,

                                    )
                            )

                        )

                    }
                }
            }
        }
    }

    /**monitorSocketForChatMessages is a function that checks for types of messages that come from the
     * websocket.
     * */
    private fun monitorSocketForChatMessages() {
        viewModelScope.launch {
            webSocket.state.collect { twitchUserMessage ->
                Log.d("loggedMessage", " tmiSentTs --> ${twitchUserMessage.tmiSentTs}")
                Log.d("twitchUserMessage", " messageType --> ${twitchUserMessage.messageType}")
                Log.d("twitchUserMessage", " twitchUserMessage --> ${twitchUserMessage}")
                Log.d("twitchUserMessage", "-----------------------------------------------------")
                Log.d("twitchUserMessageTesting", "displayName ->${twitchUserMessage.displayName}")
                Log.d("twitchUserMessageTesting", "clickedUsername ->${_clickedUIState.value.clickedUsername}")
                Log.d(
                    "twitchUserMessageTesting",
                    "equal ->${twitchUserMessage.displayName == _clickedUIState.value.clickedUsername}"
                )

                if (twitchUserMessage.displayName == _clickedUIState.value.clickedUsername) {


//                    clickedUsernameChatsWithDateSent.add(
//                        ClickedUserNameChats(
//                            message =twitchUserMessage.userType?:"",
//                            dateSent = twitchUserMessage.dateSend
//                        )
//                    )
                    addAllClickedUsernameChatsDateSent(
                        listOf(
                            ClickedUserNameChats(
                                message = twitchUserMessage.userType ?: "",
                                dateSent = twitchUserMessage.dateSend,
                                messageTokenList = MessageScanner(twitchUserMessage.userType ?: "").tokenList
                            )
                        )
                    )

                }
                if (monitoredUsers.contains(twitchUserMessage.displayName)) {
                    //twitchUserMessage.isMonitored = true
                }
                when (twitchUserMessage.messageType) {
                    MessageType.CLEARCHAT -> {
                        modActionList.add(twitchUserMessage)
                        listChats.add(twitchUserMessage)
                        // notifyChatOfBanTimeoutEvent(listChats,twitchUserMessage.userType)
                    }

                    MessageType.NOTICE -> {
                        modActionList.add(twitchUserMessage)
                        listChats.add(twitchUserMessage)
                        // notifyChatOfBanTimeoutEvent(listChats,twitchUserMessage.userType)
                    }

                    MessageType.CLEARCHATALL -> {
                        modActionList.add(twitchUserMessage)
                        clearAllChatMessages(listChats)
                    }

                    MessageType.USER -> {
                        Log.d("CheckingChattersNmae", twitchUserMessage.displayName!!)
                        Log.d("CheckingChattersNmae", twitchUserMessage.userType!!)
                        autoCompleteChat.addChatter(twitchUserMessage.displayName!!)
                        addChatter(twitchUserMessage.displayName!!, twitchUserMessage.userType!!)
                        listChats.add(twitchUserMessage)
                    }

                    MessageType.SUB -> {
                        if (_advancedChatSettingsState.value.showSubs) {
                            listChats.add(twitchUserMessage)
                        }

                    }

                    MessageType.RESUB -> {
                        if (_advancedChatSettingsState.value.showReSubs) {
                            listChats.add(twitchUserMessage)
                        }
                    }

                    MessageType.GIFTSUB -> {
                        if (_advancedChatSettingsState.value.showGiftSubs) {
                            listChats.add(twitchUserMessage)
                        }
                    }

                    MessageType.MYSTERYGIFTSUB -> {
                        if (_advancedChatSettingsState.value.showAnonSubs) {
                            listChats.add(twitchUserMessage)
                        }
                    }

                    else -> {
                        listChats.add(twitchUserMessage)
                    }
                }


                //todo:CLEAR THIS MESS OUT ABOVE
            }
        }
    }

    //CHAT METHOD
    fun addChatter(username: String, message: String) {
        if (!allChatters.contains(username)) {
            allChatters.add(username)
        }
    }

    fun clearAllChatters() {
        allChatters.clear()
    }

    //CHAT METHOD
    fun updateClickedChat(
        clickedUsername: String,
        clickedUserId: String,
        banned: Boolean,
        isMod: Boolean
    ) {
        Log.d("updateClickedChat","CLICKED")
        Log.d("updateClickedChat","clickedUsername ->${clickedUsername}")

        clearClickedUsernameChatsDateSent()
        clearAllClickedUserBadgesImmutable()
        val messages = listChats.filter { it.displayName == clickedUsername }
            .map { "${it.dateSend} " +if (it.deleted)  it.userType!! + " (deleted by mod)" else it.userType!!   }
        val clickedUserChats = listChats.filter { it.displayName == clickedUsername }
        val clickedUserMessages = clickedUserChats.map {
            val scanner = MessageScanner(it.userType?:"")
            /**WHEN*/
            scanner.startScanningTokens()
            ClickedUserNameChats(
                message =it.userType?:"",
                dateSent = it.dateSend,
                messageTokenList = scanner.tokenList
            )
        }
        val badges = clickedUserChats.first().badges
        addAllClickedUserBadgesImmutable(badges)
        addAllClickedUsernameChatsDateSent(clickedUserMessages)
        _clickedUIState.value = _clickedUIState.value.copy(
            clickedUsername = clickedUsername,
            clickedUserId = clickedUserId,
            clickedUsernameBanned = banned,
            clickedUsernameIsMod = isMod
        )
    }

    val errorValue = TwitchUserDataObjectMother
        .addColor("#FF0000")
        .addDisplayName("Connection Error")
        .addMessageType(MessageType.ERROR)
        .addUserType(
            "Disconnected from chat."
        )
        .build()
    val noInternetErrorValue = TwitchUserDataObjectMother
        .addColor("#FF0000")
        .addDisplayName("Connection Error")
        .addMessageType(MessageType.ERROR)
        .addUserType(
            "Disconnected from chat. Please check network and try again"
        )
        .build()

    fun sendDoubleTapEmote(username: String) {
        Log.d("SendingDoubleClick", "username -->$username")
        if (username.isNotEmpty()) {
            webSocket.sendMessage("@$username SeemsGood")
        }
    }

    fun filterMessages(messageId: String) {
        try {
            val found = listChats.first { it.id == messageId }
            val foundIndex = listChats.indexOf(found)
            listChats[foundIndex] = found.copy(
                deleted = true
            )
        } catch (e: Exception) {
            Log.d("FilterMessageCrash", "messageId-----> $messageId")
            Log.d("FilterMessageCrash", "messageId-----> ${e.message}")
        }

    }

    fun changeActualTextFieldValue(
        text: String,
        textRange: TextRange
    ) {
        textFieldValue.value = TextFieldValue(
            text = text,
            selection = textRange
        )

    }

    override fun onCleared() {
        super.onCleared()
        webSocket.close()
        clearAllChatters()
    }
}
