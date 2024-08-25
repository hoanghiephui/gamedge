package com.paulrybitskyi.gamedge.common.data.websockets

import android.util.Log
import com.android.model.websockets.ChatSettingsData
import com.paulrybitskyi.gamedge.common.domain.websockets.ModActionData
import com.paulrybitskyi.gamedge.common.domain.websockets.AutoModMessageParsing
import com.paulrybitskyi.gamedge.common.domain.websockets.AutoModMessageUpdate
import com.paulrybitskyi.gamedge.common.domain.websockets.AutoModQueueMessage
import com.paulrybitskyi.gamedge.common.domain.websockets.TwitchEventSubscriptionWebSocket
import com.paulrybitskyi.gamedge.core.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TwitchEventSubWebSocket @Inject constructor(
    private val modActionParsing: ModActionParsing,
    private val channelSettingsParsing: ChatSettingsParsing,
    private val autoModMessageParsing: AutoModMessageParsing
) : TwitchEventSubscriptionWebSocket, WebSocketListener() {
    private var client: OkHttpClient = OkHttpClient.Builder().build()
    var webSocket: WebSocket? = null

    private val _parsedSessionId: MutableStateFlow<String?> = MutableStateFlow(null)

    // The UI collects from this StateFlow to get its state updates
    override val parsedSessionId: StateFlow<String?> = _parsedSessionId

    private val _autoModMessageQueue: MutableStateFlow<AutoModQueueMessage?> = MutableStateFlow(null)

    // The UI collects from this StateFlow to get its state updates
    override val autoModMessageQueue: StateFlow<AutoModQueueMessage?> = _autoModMessageQueue

    private val _messageIdForAutoModQueue: MutableStateFlow<AutoModMessageUpdate?> = MutableStateFlow(null)

    // The UI collects from this StateFlow to get its state updates
    override val messageIdForAutoModQueue: StateFlow<AutoModMessageUpdate?> = _messageIdForAutoModQueue

    private val _updatedChatSettingsData: MutableStateFlow<ChatSettingsData?> = MutableStateFlow(null)

    // The UI collects from this StateFlow to get its state updates
    override val updatedChatSettingsData: StateFlow<ChatSettingsData?> = _updatedChatSettingsData

    private val _modActions: MutableStateFlow<ModActionData?> = MutableStateFlow(null)
    override val modActions: StateFlow<ModActionData?> = _modActions


    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)

        Log.d("TwitchEventSubWebSocket", "onOpen() response ->$response")

    }


    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        // this needs to run only when the notifiction type is session_welcome
        //TODO: THIS SHOULD CHECK IF IT IS A WELCOMING MESSAGE. similar to notificationTypeIsNotification()
        //TODO: THIS SHOULD NOT BE DOING THIS EVERY TIME
        //if()
        Log.d("EventWebsocketMessage", text)
        if (notificationTypeIsWelcome(text)) {
            val parsedSessionId = parseEventSubWelcomeMessage(text)
            _parsedSessionId.tryEmit(parsedSessionId)
        }

        if (notificationTypeIsNotification(text)) {

            val subscriptionType = parseSubscriptionType(text)
            Log.d("SubscriptionTypeEventWebSocket", "subscriptionType ->$subscriptionType")

            //todo: These could probably be stored in maps
            when (subscriptionType) {
                "automod.message.hold" -> {
                    Log.d("AutoModMessageHoldType", "messageHold")
                    _autoModMessageQueue.tryEmit(autoModMessageParsing.parseAutoModQueueMessage(text))
                }

                "automod.message.update" -> {
                    Log.d("AutoModMessageHoldType", "Update message")
                    val messageId = autoModMessageParsing.parseMessageId(text) ?: ""
                    val messageUpdate = autoModMessageParsing.checkUpdateStatus(text, messageId)
                    _messageIdForAutoModQueue.tryEmit(messageUpdate)
                }

                "channel.moderate" -> {
                    Log.d("ChannelModerateParsing", "TIME TO PARSE!!!!")
                    val action = modActionParsing.parseActionFromString(text)
                    modActionParsing.whenAction(
                        action = action,
                        stringToParse = text,
                        emitData = { modActionData -> _modActions.tryEmit(modActionData) }
                    )

                }

                "channel.chat_settings.update" -> {
                    Log.d("ChatSettingsParsing", text)
                    val parsedChatSettingsData = channelSettingsParsing.parseChatSettingsData(text)
                    _updatedChatSettingsData.tryEmit(parsedChatSettingsData)
                }
            }

        }
        Log.d("TwitchEventSubWebSocket", "onMessage() text ->$text")
        Log.d("createAnotherSubscriptionEvent", "onMessage text -->$text")

    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        Log.d("TwitchEventSubWebSocket", "onClosing()")

    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        Log.d("TwitchEventSubWebSocket", "onClosed()")

    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        Log.d("TwitchEventSubWebSocket", "onFailure() ${t.message}")
        Log.d("TwitchEventSubWebSocket", "response --> ${response?.message}")
        Log.d("TwitchEventSubWebSocket", "body --> ${response?.body}")

        val data = ModActionData(
            title = "Connection Error",
            message = "There was an error while trying to connect with Twitch's servers",
            iconId = R.drawable.error_outline_24,
            secondaryMessage = "To fix this issue try going back to the home page and click on the stream again"
        )
        _modActions.tryEmit(data)
    }

    override fun newWebSocket() {
        if (webSocket != null) {
            close()
            createNewSocket()
        } else {
            createNewSocket()
        }

    }

    override fun closeWebSocket() {
        webSocket?.close(1009, "Bye")
    }

    private fun createNewSocket() {
        val request: Request = Request.Builder()
            .url("wss://eventsub.wss.twitch.tv/ws")
            .build()
        client = OkHttpClient.Builder()
            .readTimeout(1000, TimeUnit.MILLISECONDS)
            .writeTimeout(1000, TimeUnit.MILLISECONDS)
            .build()

        webSocket = client.newWebSocket(request, this)
    }

    private fun close() {
        // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
        client.dispatcher.executorService.shutdown()
        webSocket?.close(1009, "Manually closed ")
        webSocket = null
    }

}
/***END OF TwitchEventSubWebSocket****/

/**
 * parseEventSubWelcomeMessage is a function meant to parse out the session id from the
 * [welcome message](https://dev.twitch.tv/docs/eventsub/handling-websocket-events/#welcome-message)
 *
 * @param stringToParse represents what was sent from the twitch servers
 * @return String? object
 * */
fun parseEventSubWelcomeMessage(stringToParse: String): String? {
    val pattern = "\"id\":([^,]+)".toRegex()
    val messageId = pattern.find(stringToParse)?.groupValues?.get(1)
    val parsedMessageId = messageId?.replace("\"", "")
    Log.d("parseEventSubWelcomeMessageData", "parsedMessagId --> $parsedMessageId")
    return parsedMessageId
}

/**
 * notificationTypeIsNotification is a function meant to determine if the data sent from the Twitch servers
 * has a type of notification
 *
 * @param stringToParse represents what was sent from the twitch servers
 * @return Boolean object
 * */
fun notificationTypeIsNotification(stringToParse: String): Boolean {
    val wantedNotification = "notification"
    val messageTypeRegex = "\"message_type\":([^,]+)".toRegex()
    val messageType = messageTypeRegex.find(stringToParse)?.groupValues?.get(1)?.replace("\"", "")
    return messageType == wantedNotification
}

/**
 * notificationTypeIsWelcome is a function that is used to determine if the [stringToParse] is a welcome message or not
 *
 * @param stringToParse a String that represents a piece of meta data that is sent by the Twitch websocket
 *
 * @return a Boolean that is used to determine if the [stringToParse] contains a `session_welcome` parameter
 *
 * */
fun notificationTypeIsWelcome(stringToParse: String): Boolean {
    val wantedNotification = "session_welcome"
    val messageTypeRegex = "\"message_type\":([^,]+)".toRegex()
    val messageType = messageTypeRegex.find(stringToParse)?.groupValues?.get(1)?.replace("\"", "")
    Log.d("MessageTypeIsWelcome", "string to parse -->$stringToParse")
    Log.d("MessageTypeIsWelcome", "is it message type -->${messageType == wantedNotification}")
    return messageType == wantedNotification
}

fun parseSubscriptionType(stringToParse: String): String {
    val messageTypeRegex = "\"subscription_type\":([^,]+)".toRegex()
    val subscriptionType = messageTypeRegex.find(stringToParse)?.groupValues?.get(1)?.replace("\"", "")
    return subscriptionType ?: ""
}






