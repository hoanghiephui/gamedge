package com.paulrybitskyi.gamedge.common.data.websockets

import android.util.Log
import com.android.model.websockets.MessageType
import com.paulrybitskyi.gamedge.common.domain.websockets.LoggedInUserData
import com.paulrybitskyi.gamedge.common.domain.websockets.MessageToken
import com.paulrybitskyi.gamedge.common.domain.websockets.PrivateMessageType
import com.paulrybitskyi.gamedge.common.domain.websockets.RoomState
import com.paulrybitskyi.gamedge.common.domain.websockets.TwitchUserData
import com.paulrybitskyi.gamedge.common.domain.websockets.TwitchUserDataObjectMother
import okhttp3.WebSocket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class EmoteInText(
    val emoteUrl: String,
    val startIndex: Int,
    val endIndex: Int
)

fun findEmoteNames(input: String, emoteNames: List<String>): List<EmoteInText> {
    val regex = Regex("\\b(?:${emoteNames.joinToString("|")})\\b")
    return regex.findAll(input).map {
        EmoteInText(
            emoteUrl = "https://static-cdn.jtvnw.net/emoticons/v2/64138/static/light/1.0",
            startIndex = it.range.first,
            endIndex = it.range.last
        )
    }.toList()
}

/**
 * The ParsingEngine class represents all the current methods avaliable to parse messages sent from the Twitch IRC chat.
 */
class ParsingEngine @Inject constructor() {
    var globalId = 1
    private var initialRoomState: RoomState = RoomState(false, false, false, false, 0, 0)

    /**
     * clearChatTesting() a triggered inside of [TwitchWebSocket] once a " CLEARCHAT " command is found.
     * This function is used to determine if the " CLEARCHAT " command was sent to ban the user or to clear the chat of
     * chat messages
     *
     * @param text A string representing all the meta data sent from the twitch IRC server
     * @param streamerName a String representing the name of the channel currently watching
     * */
    fun clearChatTesting(text: String, streamerName: String): TwitchUserData {
        // THIS IS TO CLEAR EVERYTHING @room-id=520593641;tmi-sent-ts=1696019043159 :tmi.twitch.tv CLEARCHAT #theplebdev
        // THIS IS TO BAN USER @room-id=520593641;target-user-id=949335660;tmi-sent-ts=1696019132494 :tmi.twitch.tv CLEARCHAT #theplebdev :meanermeeny
        val streamerNamePattern = "$streamerName$".toRegex()

        val clearChat = streamerNamePattern.find(text)
        val channelNameFound = clearChat?.value

        if (channelNameFound == null) {
            return banUserParsing(text)
        } else {
            Log.d("noticeParsingTHings", "clearChatParsing() Global Id used -->$text")
            return clearChatParsing()
        }
    }

    /**
     * banUserParsing() is a private function used by [clearChatTesting] and will be triggered when a `ban user` command is
     * detected, meaning a certain user was banned from the chat
     *
     * @param text a String that represents the text sent from the Twitch IRC server and is what we are going to parse
     * */
    private fun banUserParsing(text: String): TwitchUserData {
        // THIS IS TO BAN USER @room-id=520593641;target-user-id=949335660;tmi-sent-ts=1696019132494 :tmi.twitch.tv CLEARCHAT #theplebdev :meanermeeny

        val banUserPattern = "([^:]+)$".toRegex()
        val bannedUserIdPattern = "target-user-id=(\\d+)".toRegex()

        val banDurationPattern = "ban-duration=([^;]+)".toRegex()
        val banDurationFound = banDurationPattern.find(text)?.groupValues?.get(1)
        Log.d("banDurationFound", "banDurationFound ---> $banDurationFound")

        val bannedUserUsername = banUserPattern.find(text)
        val bannedUserId = bannedUserIdPattern.find(text)

        val usernameFound = bannedUserUsername?.value ?: "User"
        val bannedUserIdFound = bannedUserId?.groupValues?.get(1)

        var message = "$usernameFound banned by moderator"
        if (banDurationFound != null) {
            Log.d("banDurationFound", "banDurationFound ---> NOT NULL")
            message = "$usernameFound timed out for $banDurationFound seconds"
        }

        return TwitchUserDataObjectMother
            .addColor("#000000")
            .addDisplayName(usernameFound)
            .addBannedDuration(banDurationFound?.toInt())
            .addId(bannedUserIdFound)
            .addUserType(message)
            .addMessageType(MessageType.CLEARCHAT)
            .build()
    }

    /**
     * clearChatParsing() is a private function used by [clearChatTesting] and will be triggered when a `clear chat` command is
     * detected, that the entire chat room is to be cleared by a moderator
     *
     * */
    private fun clearChatParsing(): TwitchUserData {
        globalId += 1
        return TwitchUserData(
            badgeInfo = null,
            badges = listOf(),
            clientNonce = null,
            color = "#000000",
            displayName = null,
            emotes = null,
            firstMsg = null,
            flags = null,
            id = globalId.toString(),
            mod = null,
            returningChatter = null,
            roomId = null,
            subscriber = false,
            tmiSentTs = null,
            turbo = false,
            userId = null,
            userType = "Chat cleared by moderator",
            messageType = MessageType.CLEARCHATALL,
            bannedDuration = null
        )
    }

    /**
     * Parses the websocket data sent from twitch. should run when a USERSTATE command is sent
     * @return [LoggedInUserData] Which represents the state of the current logged in user
     */
    fun userStateParsing(text: String): LoggedInUserData {
        val colorPattern = "color=([^;]+)".toRegex()
        val displayNamePattern = "display-name=([^;]+)".toRegex()
        val modStatusPattern = "mod=([^;]+)".toRegex()
        val subStatusPattern = "subscriber=([^;]+)".toRegex()

        val colorMatch = colorPattern.find(text)
        val displayNameMatch = displayNamePattern.find(text)
        val modStatusMatch = modStatusPattern.find(text)
        val subStatusMatch = subStatusPattern.find(text)

        val loggedData = LoggedInUserData(
            color = colorMatch?.groupValues?.get(1),
            displayName = displayNameMatch?.groupValues?.get(1)!!,
            mod = modStatusMatch?.groupValues?.get(1)!! == "1",
            sub = subStatusMatch?.groupValues?.get(1)!! == "1"

        )

        return loggedData
    }

    /**
     * Parses the websocket data sent from twitch. Will run when a CLEARMSG command is sent
     * @property text the string to be parsed
     * @return a nullable string containing the id of the message to be deleted
     */
    fun clearMsgParsing(text: String): String? {
        val pattern = "target-msg-id=([^;]+)".toRegex()

        val messageId = pattern.find(text)?.groupValues?.get(1)
        return messageId
    }

    /**
     * Creates a [TwitchUserData] that will be sent when a JOIN command is sent
     *
     * @return a [TwitchUserData] used to notify the user that they have connected to a streamer's chat room
     */
    fun createJoinObject(): TwitchUserData {

        globalId += 1
        return TwitchUserDataObjectMother.addColor("#000000")
            .addDisplayName("Room update")
            .addUserType("Connected to chat!")
            .addId(globalId.toString())
            .addMessageType(MessageType.JOIN)
            .build()
    }

    fun noticeParsing(text: String, streamerChannelName: String): TwitchUserData {
        globalId += 1
        Log.d("noticeParsingTHings", "noticeParsing() Global Id used -->$text")
        if (text.contains("NOTICE *")) {
            val regexPattern = "(NOTICE \\* :)(.+)".toRegex()
            val matchedPattern = regexPattern.find(text)?.groupValues?.get(2) ?: "Room information updated"
            //gettting the message id
            return TwitchUserDataObjectMother
                .addColor("#000000")
                .addDisplayName("Room update")
                .addUserType(matchedPattern)
                .addId(globalId.toString())
                .addMessageType(MessageType.NOTICE)
                .build()

        } else {
            val pattern = "#$streamerChannelName\\s*:(.+)".toRegex()
            val matchResult = pattern.find(text)
            val extractedInfo = matchResult?.groupValues?.get(1)?.trim() ?: "Room information updated"
            return TwitchUserDataObjectMother
                .addColor("#000000")
                .addDisplayName("Room update")
                .addUserType(extractedInfo)
                .addId(globalId.toString())
                .addMessageType(MessageType.NOTICE)
                .build()
        }


    }

    /**
     * Creates a [TwitchUserData] that will be sent when a USERNOTICE command is sent
     * - more can be read [HERE](https://dev.twitch.tv/docs/irc/commands/#usernotice)
     *
     * @return a [TwitchUserData] used to notify the chat that an certain event has occurred. Example events are,
     * announcement, resub, sub, submysterygift, subgift
     */
    fun userNoticeParsing(text: String, streamerChannelName: String): TwitchUserData {

        val displayNamePattern = "display-name=([^;]+)".toRegex()
        val messageIdPattern = "msg-id=([^;]+)".toRegex()
        val systemMessagePattern = "system-msg=([^;]+)".toRegex()
        val personalMessagePattern = "#$streamerChannelName :([^;]+)".toRegex()
        val idPattern = "id=([^;]+)".toRegex()

        val displayNameMatch = displayNamePattern.find(text)
        val messageIdMatch = messageIdPattern.find(text)
        val systemMessageMatch = systemMessagePattern.find(text)
        val personalMessageMatch = personalMessagePattern.find(text)
        val id = idPattern.find(text)?.groupValues?.get(1)


        val displayName = displayNameMatch?.groupValues?.get(1) ?: "username"

        val messageType: MessageType = when (messageIdMatch?.groupValues?.get(1) ?: "announcement") {
            "announcement" -> {
                MessageType.ANNOUNCEMENT
            }

            "resub" -> MessageType.RESUB
            "sub" -> MessageType.SUB
            "submysterygift" -> MessageType.MYSTERYGIFTSUB
            "subgift" -> MessageType.GIFTSUB
            else -> MessageType.ANNOUNCEMENT
        }
        val systemMessage = systemMessageMatch?.groupValues?.get(1)?.replace("\\s", " ") ?: "Announcement!"
        val personalMessage = personalMessageMatch?.groupValues?.get(1)

        return TwitchUserDataObjectMother
            .addDisplayName(displayName)
            .addMessageType(messageType)
            .addUserType(personalMessage)
            .addSystemMessage(systemMessage)
            .addId(id!!)
            .build()
    }

    fun parseBadges(stringToParse: String): List<String> {
        val regex = Regex("badges=([^;]+)")
        val matchResult = regex.find(stringToParse)
        val badgesParsed = matchResult?.groupValues?.get(1) ?: ""

        val replaceRegex = "/[0-9-]+".toRegex()
        return badgesParsed.replace(replaceRegex, "").split(",")
    }

    fun parseBadgeInfo(stringToParse: String): String {
        val regex = Regex("@badge-info=([^;]+)")
        val matchResult = regex.find(stringToParse)
        val parsedResult = matchResult?.groupValues?.get(1)



        return if (parsedResult == null) {
            "Not a subscriber"
        } else {
            val another = if (Regex("subscriber/([^;]+)").find(parsedResult)?.groupValues?.get(1) != null) {
                "Subbed for ${Regex("subscriber/([^;]+)").find(parsedResult)?.groupValues?.get(1)} months"
            } else {
                "Not a subscriber"
            }
            another
        }
    }

    //  var textChatCount = 0
    /**
     * Parses the websocket data sent from twitch. Should run when a PRIVMSG command is sent
     * @property text the string to be parsed
     * @return a [TwitchUserData] representing all the meta data from an individual chatter
     */
    fun privateMessageParsing(text: String, channelName: String): TwitchUserData {
        val pattern = "([^;@]+)=([^;]+)".toRegex()
        val privateMsgPattern = "(#$channelName :)(.+)".toRegex()
        //Log.d("TextChatNumber","total number of messages = ${textChatCount++} ")

        Log.d("privateMessageParsing", "string --> $text")


        val matchResults = pattern.findAll(text)
        val privateMsgResult = privateMsgPattern.find(text)

        val parsedData = mutableMapOf<String, String>()
        val privateMsg = privateMsgResult?.groupValues?.get(2) ?: ""

        for (matchResult in matchResults) {
            val (key, value) = matchResult.destructured
            parsedData[key] = value
        }
        val messageTokenScanner = MessageScanner(privateMsg)
        messageTokenScanner.startScanningTokens()
        /******TIME STAMP DATA******/
        val timestampMillis = parsedData["tmi-sent"]

        val patternForTimeSent = "tmi-sent-ts=([0-9]+)"
        var dateSent = "No date found"
        Regex(patternForTimeSent).find(text)?.groupValues?.get(1)?.toLong()?.also {
            val date = Date(it)
            val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val formattedDate = format.format(date)
            dateSent = formattedDate

        }
        val parsedBadgesList = parseBadges(text)

        //  Log.d("PRIVATEMSSGDATE","dateParsedOut --> $dateParsedOut")
//            ?.toLong()?.also {
//            val date = Date(it)
//            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//            val formattedDate = format.format(date)
////            Log.d("PRIVATEMSSGDATE","formattedDate --> $formattedDate")
//
//        }
        val badgeInfo = parseBadgeInfo(text)

        return TwitchUserData(
            badgeInfo = badgeInfo,
            badges = parsedBadgesList,
            clientNonce = parsedData["client-nonce"],
            color = parsedData["color"] ?: "#FF6650a4",
            displayName = parsedData["display-name"],
            emotes = parsedData["emotes"],
            firstMsg = parsedData["first-msg"],
            flags = parsedData["flags"],
            id = parsedData["id"],
            mod = parsedData["mod"],
            returningChatter = parsedData["returning-chatter"],
            subscriber = parsedData["subscriber"]?.toIntOrNull() == 1,
            roomId = parsedData["room-id"],
            tmiSentTs = parsedData["tmi-sent"]?.toLongOrNull(),
            turbo = parsedData["turbo"]?.toIntOrNull() == 1,
            userType = privateMsg,
            userId = parsedData["user-id"],
            messageType = checkFirstTimeChatter(text), //todo: add parsing to check for first time chatter
            messageList = messageTokenScanner.tokenList,
            dateSend = dateSent
        )
    }

    fun checkFirstTimeChatter(stringToParse: String): MessageType {
        val regex = "first-msg=(\\d+)".toRegex()
        val matchResult = regex.find(stringToParse)
        val firstMsgValue = matchResult?.groupValues?.get(1) ?: "0"
        if (firstMsgValue == "1") {
            return MessageType.FIRSTTIMECHATTER
        } else {
            return MessageType.USER
        }
    }


    /**
     * Parses the information relate to the chat rooms state. Should be run when ROOMSTATE is sent
     * @property text the string to be parsed
     * @return a [RoomState] representing the current state of the chat room
     */
    fun roomStateParsing(text: String): RoomState {

        val slowModeDuration = getDuration(text, "slow")
        val followerModeDuration = getDuration(text, "followers-only")

        val emoteMode = getValueFromInput(text, "emote-only")
        val followersMode = getValueFromInput(text, "followers-only")
        val slowMode = getValueFromInput(text, "slow")
        val subMode = getValueFromInput(text, "subs-only")


        when {
            slowMode != null && emoteMode != null && followersMode != null && subMode != null -> {
                initialRoomState = RoomState(
                    emoteMode, followersMode, slowMode, subMode, followerModeDuration, slowModeDuration
                )
            }

            slowMode != null -> {
                initialRoomState = RoomState(
                    initialRoomState.emoteMode,
                    initialRoomState.followerMode,
                    slowMode,
                    initialRoomState.subMode,
                    followerModeDuration,
                    slowModeDuration
                )
            }

            emoteMode != null -> {
                initialRoomState = RoomState(
                    emoteMode,
                    initialRoomState.followerMode,
                    initialRoomState.slowMode,
                    initialRoomState.subMode,
                    followerModeDuration,
                    slowModeDuration
                )
            }

            followersMode != null -> {
                initialRoomState = RoomState(
                    initialRoomState.emoteMode,
                    followersMode,
                    initialRoomState.slowMode,
                    initialRoomState.subMode,
                    followerModeDuration,
                    slowModeDuration
                )
            }

            subMode != null -> {
                initialRoomState = RoomState(
                    initialRoomState.emoteMode,
                    initialRoomState.followerMode,
                    initialRoomState.slowMode,
                    subMode,
                    followerModeDuration,
                    slowModeDuration
                )
            }
        }
        return RoomState(
            emoteMode = emoteMode ?: initialRoomState.emoteMode,
            followerMode = followersMode ?: initialRoomState.followerMode,
            slowMode = slowMode ?: initialRoomState.slowMode,
            subMode = subMode ?: initialRoomState.subMode,

            followerModeDuration = followerModeDuration,
            slowModeDuration = slowModeDuration
        )
    }

    /**
     * Send the PONG message to the websocket
     * @property webSocket the [WebSocket] which PONG will be sent to and tell the Twitch IRC servers to not disconnect
     */
    fun sendPong(webSocket: WebSocket) {
        webSocket.send("PONG")
    }

    fun getDuration(input: String, key: String): Int {
        val pattern = "$key=([^;:\\s]+)".toRegex()
        val match = pattern.find(input)
        val returnedValue = match?.groupValues?.get(1)
        if (returnedValue == null) {
            return 0
        } else {
            if (returnedValue == "-1") {
                return 0
            } else {
                return returnedValue.toInt()
            }
        }

    }

    private fun getValueFromInput(input: String, key: String): Boolean? {
        val pattern = "$key=([^;:\\s]+)".toRegex()
        val match = pattern.find(input)
        val returnedValue = match?.groupValues?.get(1) ?: return null
        if (returnedValue == "-1") {
            return false
        }
        if (key == "followers-only" && returnedValue == "0") {
            return true
        } else {
            return returnedValue != "0"
        }
    }
}


class MessageScanner(
    private val source: String
) {
    private val map = hashMapOf<String, MessageToken>()

    private val tokens = mutableListOf<MessageToken>()
    val tokenList: List<MessageToken> = tokens

    init {
        map["SeemsGood"] = MessageToken(
            messageType = PrivateMessageType.EMOTE,
            messageValue = "SeemsGood",
            url = "https://static-cdn.jtvnw.net/emoticons/v2/64138/static/light/1.0"
        )
    }

    private var start = 0
    private var current = 0

    fun startScanningTokens() {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }
    }

    private fun scanToken() {
        val c = advance()
        when (c) {
            ' ' -> {}
            else -> {
                while (notEndNullOrEmptySpace(peek())) {
                    advance()
                }
                addToken()
            }

        }
    }

    private fun addToken() {
        val text = source.substring(start, current)
        val tokenType = map[text] ?: MessageToken(messageType = PrivateMessageType.MESSAGE, messageValue = text)
        tokens.add(tokenType)
    }

    private fun notEndNullOrEmptySpace(c: Char): Boolean {
        return !isAtEnd() && c != '\u0000' && c != '\u0020'
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length
    }

    private fun advance(): Char {
        return source[current++]
    }

    private fun peek(): Char {
        return if (isAtEnd()) '\u0000' else source[current]
    }


}
