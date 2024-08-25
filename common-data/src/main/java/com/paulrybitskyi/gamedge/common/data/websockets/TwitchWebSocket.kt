package com.paulrybitskyi.gamedge.common.data.websockets

import android.util.Log
import com.paulrybitskyi.gamedge.common.domain.websockets.LoggedInUserData
import com.android.model.websockets.MessageType
import com.paulrybitskyi.gamedge.common.domain.websockets.RoomState
import com.paulrybitskyi.gamedge.common.domain.websockets.TwitchUserData
import com.paulrybitskyi.gamedge.common.domain.auth.datastores.AuthLocalDataStore
import com.paulrybitskyi.gamedge.common.domain.websockets.TwitchSocket
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class TwitchWebSocket @Inject constructor(
    private val authLocalDataStore: AuthLocalDataStore,
    private val twitchParsingEngine: ParsingEngine
) : WebSocketListener(), TwitchSocket {

    private val webSocketScope = CoroutineScope(
        Dispatchers.Default + CoroutineName("webSocketScope")
    )

    private val initialValue = TwitchUserData(
        badgeInfo = "subscriber/77",
        badges = listOf(),
        clientNonce = "d7a543c7dc514886b439d55826eeeb5b",
        color = "FF0000",
        displayName = "verifying data",
        emotes = "",
        firstMsg = "0",
        flags = "",
        id = "fd594314-969b-4f5e-a83f-5e2f74261e6c",
        mod = "0",
        returningChatter = "0",
        roomId = "19070311",
        subscriber = true,
        tmiSentTs = 1690747946900L,
        turbo = false,
        userId = "144252234",
        userType = "Connecting to chat",
        messageType = MessageType.ANNOUNCEMENT,
        systemMessage = "Connecting to chat"
    )

    private val webSocketURL = "wss://irc-ws.chat.twitch.tv:443"
    var streamerChannelName = ""
    private var loggedInUsername = ""


    private val _state = MutableStateFlow(initialValue)
    override val state = _state.asStateFlow()


    private val _latestBannedUserId = MutableStateFlow<String?>(null)
    override val latestBannedUserId = _latestBannedUserId.asStateFlow()

    private val _hasWebSocketFailed = MutableStateFlow<Boolean?>(null)
    override val hasWebSocketFailed = _hasWebSocketFailed.asStateFlow()


    private val _loggedInUserUiState = MutableStateFlow<LoggedInUserData?>(null)
    override val loggedInUserUiState = _loggedInUserUiState

    private var client: OkHttpClient = OkHttpClient.Builder().build()
    var webSocket: WebSocket? = null

    var sentMessageString: String = ""

    private val _roomState = MutableStateFlow<RoomState?>(null)
    override val roomState = _roomState.asStateFlow()

    private val _messageToDeleteId: MutableStateFlow<String?> = MutableStateFlow(null)
    override val messageToDeleteId = _messageToDeleteId.asStateFlow() // this is the text data shown to the user

    private val _bannedUsername: MutableStateFlow<String?> = MutableStateFlow(null)
    val bannedUsername = _bannedUsername.asStateFlow() // this is the text data shown to the user

    override fun run(channelName: String?, username: String) {
        loggedInUsername = username
        if (channelName != null) {
            streamerChannelName = channelName
            if (webSocket != null) {
                close()
                newWebSocket()
            } else {
                newWebSocket()
            }
        } else {
        }
    }

    override fun close() {
        // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
        client.dispatcher.executorService.shutdown()
        webSocket?.close(1009, "Manually closed ")
        webSocket = null
    }

    private fun newWebSocket() {
        _hasWebSocketFailed.tryEmit(false)
        val request: Request = Request.Builder()
            .url(webSocketURL)
            .build()
        client = OkHttpClient.Builder()
            .readTimeout(1000, TimeUnit.MILLISECONDS)
            .writeTimeout(1000, TimeUnit.MILLISECONDS)
            .build()

        webSocket = client.newWebSocket(request, this)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)

        // todo: I think I am going to create a custom scope tied to the lifecycle of this websocket
        openChat(webSocket)
    }

    fun openChat(webSocket: WebSocket) = GlobalScope.launch {
        webSocket.send("CAP REQ :twitch.tv/tags twitch.tv/commands")
        Log.d("TwitchSocket", "state --> openChat")
        authLocalDataStore.authorizationTokenTwitch.collect { oAuthToken ->
            Log.d("NICKUSERNAME", "state --> $loggedInUsername")
            Log.d("OAuthtokenStoof", oAuthToken)
            webSocket.send("PASS oauth:$oAuthToken")
            webSocket.send("NICK $loggedInUsername")
            webSocket.send("JOIN #$streamerChannelName")
        }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        Log.d("websocketStoof", "onMessage()byte: ${bytes.hex()}")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d("onMessageSocketStoof", "state --> $text")

        if (text.contains("PING")) {
            twitchParsingEngine.sendPong(webSocket)
        }

        if (text.contains(" CLEARCHAT ")) {
            val parsedTwitchUserData = twitchParsingEngine.clearChatTesting(
                text,
                streamerChannelName
            )
            when (parsedTwitchUserData.messageType) {
                MessageType.CLEARCHATALL -> {
                    _state.tryEmit(parsedTwitchUserData)
                }

                MessageType.CLEARCHAT -> {
                    //  _state.tryEmit(parsedTwitchUserData)
                    _latestBannedUserId.tryEmit(parsedTwitchUserData.id)
                }

                else -> {

                }
            }

        }

        if (text.contains(" USERSTATE ")) {
            val parsedTwitchInUserData = twitchParsingEngine.userStateParsing(text)
            Log.d("USERSTATEPARSING", "TEXT->${parsedTwitchInUserData.sub}")

            _loggedInUserUiState.tryEmit(
                parsedTwitchInUserData
            )
        }
        if (text.contains(" CLEARMSG ")) {
            val messageId = twitchParsingEngine.clearMsgParsing(text)
            _messageToDeleteId.tryEmit(messageId)
        }

        if (text.contains(" JOIN ")) {
            Log.d("noticeParsingTHings", "createJoinObject() Global Id used -->$text")
            val joinObject = twitchParsingEngine.createJoinObject()
            _state.tryEmit(joinObject)
        }
        if (text.contains(" NOTICE ")) {
            Log.d("NOTICETriggered", "NOTICE --> $text")

            _state.tryEmit(
                twitchParsingEngine.noticeParsing(text, streamerChannelName)
            )
        }
        if (text.contains(" USERNOTICE ")) {
            val userStateData = twitchParsingEngine.userNoticeParsing(text, streamerChannelName)

            _state.tryEmit(userStateData)
        }

        if (text.contains("PRIVMSG")) {
            Log.d("loggedInDataOnMessage", "PRIVMSG --> $text")

            val parsedPRIVMSG = twitchParsingEngine.privateMessageParsing(text, streamerChannelName)
            _state.tryEmit(parsedPRIVMSG)
        }

        if (text.contains("ROOMSTATE")) {
            Log.d("logginTheRoomState", "Roomstate --> $text")
            val roomState = twitchParsingEngine.roomStateParsing(text)
            _roomState.tryEmit(roomState)
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
        println("CLOSE: $code $reason")
        Log.d("websocketStoof", "onClosing: $code $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        // t.printStackTrace()
        Log.d("websocketStooffail", "onFailure: ${t.printStackTrace()}")
        Log.d("websocketStooffail", "onFailure: ${t.message}")
        Log.d("websocketStooffail", "onFailure: ${t.cause}")


        _hasWebSocketFailed.tryEmit(true)
        // _state.tryEmit(errorValue)
    }

    override fun sendMessage(chatMessage: String): Boolean {
        val sendingText = webSocket?.send("PRIVMSG #$streamerChannelName :$chatMessage")
        sentMessageString = chatMessage
        // 'PRIVMSG #$channelName :$message'
        Log.d("websocketStooffail", "sendMessageResult:--> $sendingText")
        return sendingText ?: false
    }
}
/*********END OF THE TwitchWebSocket CLASS*******/
