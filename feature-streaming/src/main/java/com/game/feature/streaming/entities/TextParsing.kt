package com.game.feature.streaming.entities

import android.util.Log
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.getTextBeforeSelection
import javax.inject.Inject

data class ForwardSlashCommands(
    val title: String,
    val subtitle: String,
    val clickedValue: String,
)

/**
 * ForwardSlashCommandsImmutableCollection is a Wrapper object created specifically to handle the problem of the Compose compiler
 *  always marking the List as unstable.
 *  - You can read more about this Wrapper solution, [HERE](https://developer.android.com/develop/ui/compose/performance/stability/fix#annotated-classes)
 *
 * */
@Immutable
data class ForwardSlashCommandsImmutableCollection(
    val snacks: List<ForwardSlashCommands>
)

/**
 * FilteredChatListImmutableCollection is a Wrapper object created specifically to handle the problem of the Compose compiler
 *  always marking the List as unstable.
 *  - You can read more about this Wrapper solution, [HERE](https://developer.android.com/develop/ui/compose/performance/stability/fix#annotated-classes)
 *
 * */
@Immutable
data class FilteredChatListImmutableCollection(
    val chatList: List<String>
)


class TextParsing @Inject constructor() {
    private val listOfCommands = listOf(
        ForwardSlashCommands(
            title = "/ban [username] [reason] ",
            subtitle = "Permanently ban a user from chat",
            clickedValue = "ban"
        ),
        ForwardSlashCommands(
            title = "/unban [username] ",
            subtitle = "Remove a timeout or a permanent ban on a user",
            clickedValue = "unban"
        ),
        ForwardSlashCommands(
            title = "/warn [username] [reason]",
            subtitle = "issue a warning to a user that they must acknowledge before chatting again",
            clickedValue = "warn"
        )
//        ForwardSlashCommands(title="/monitor [username] ", subtitle = "Start monitoring a user's messages (only visible to you)",clickedValue="monitor"),
//        ForwardSlashCommands(title="/unmonitor [username] ", subtitle = "Stop monitoring a user's messages",clickedValue="unmonitor")
    )

    //
    val textFieldValue = mutableStateOf(
        TextFieldValue(
            text = "",
            selection = TextRange(0)
        )
    )
    var filteredChatList = mutableStateListOf<String>()

    private val _forwardSlashCommands = mutableStateListOf<ForwardSlashCommands>()

    /********Immutable _filteredChatListImmutableCollection*************/
    // Immutable state holder
    private var _filteredChatListImmutableCollection by mutableStateOf(
        FilteredChatListImmutableCollection(filteredChatList)
    )

    // Publicly exposed immutable state as State
    val filteredChatListImmutable: State<FilteredChatListImmutableCollection>
        get() = mutableStateOf(_filteredChatListImmutableCollection)

    private fun addAllFilteredChatList(commands: List<String>) {
        filteredChatList.addAll(commands)
        _filteredChatListImmutableCollection = FilteredChatListImmutableCollection(filteredChatList)

    }

    private fun filterChatListUsername(
        usernameRegex: Regex
    ) {
        filteredChatList.removeIf {
            !it.contains(usernameRegex)
        }
        _filteredChatListImmutableCollection = FilteredChatListImmutableCollection(filteredChatList)
    }

    private fun clearFilteredChatterListImmutable() {
        filteredChatList.clear()
        _filteredChatListImmutableCollection = FilteredChatListImmutableCollection(listOf())
    }


    /********END --> Immutable _filteredChatListImmutableCollection*************/

    /********** New forward slash command to make it immutable *************/
    // Immutable state holder
    private var _forwardSlashCommandsImmutableCollection by mutableStateOf(
        ForwardSlashCommandsImmutableCollection(_forwardSlashCommands)
    )

    // Publicly exposed immutable state as State
    val forwardSlashCommandsState: State<ForwardSlashCommandsImmutableCollection>
        get() = mutableStateOf(_forwardSlashCommandsImmutableCollection)

    // Update the collection whenever the mutable list changes
    fun addForwardSlashCommand(command: ForwardSlashCommands) {
        _forwardSlashCommands.add(command)
        _forwardSlashCommandsImmutableCollection = ForwardSlashCommandsImmutableCollection(_forwardSlashCommands)
    }

    private fun addAllForwardSlashCommand(commands: List<ForwardSlashCommands>) {
        _forwardSlashCommands.addAll(commands)
        _forwardSlashCommandsImmutableCollection = ForwardSlashCommandsImmutableCollection(_forwardSlashCommands)

    }

    private fun clearForwardSlashCommand() {
        _forwardSlashCommands.clear()
        _forwardSlashCommandsImmutableCollection = ForwardSlashCommandsImmutableCollection(listOf())
    }


    /**********New forward slash command to make it immutable  above*************/

    var parsingIndex: Int = 0
    var startParsing: Boolean = false

    var slashCommandState: Boolean = false
    var slashCommandIndex: Int = 0

    /********BELOW ARE ALL THE methods*******/

    /**
     * clickUsernameAutoTextChange() is a function meant to be run when a user clicks on a username after typing the ***@*** symbol.
     * It will create a new text with the clicked username and replace the old text that the user was typing
     *
     * @param username a string meant to represent the username that the user just clicked on
     * @param parsingIndex a integer that represents where the parsing should begin
     * @param clearChat a function meant to represent any extra clean up that needs to take place.
     * */
    fun clickUsernameAutoTextChange(
        username: String,

        ) {
        val currentCharacterIndex = textFieldValue.value.selection.end

        val replacedString = textFieldValue.value.text.replaceRange(parsingIndex, currentCharacterIndex, "$username ")
        textFieldValue.value = textFieldValue.value.copy(
            text = replacedString,
            selection = TextRange(replacedString.length)
        )
        //  filteredChatList.clear()
        clearFilteredChatterList()

    }

    fun clearFilteredChatterList() {
        //  filteredChatList.clear()
        clearFilteredChatterListImmutable()
    }

    fun updateTextField(emoteText: String) {
        val currentString = textFieldValue.value.text
        val cursorPosition = textFieldValue.value.selection.start

        val newText = StringBuilder(currentString).insert(cursorPosition, emoteText).toString()
        val newCursorPosition = cursorPosition + emoteText.length

        textFieldValue.value = textFieldValue.value.copy(
            text = newText,
            selection = TextRange(newCursorPosition, newCursorPosition)
        )
    }

    fun deleteEmote(
        emoteMap: Map<String, InlineTextContent>

    ) {
        val tokenScanner = DeletingEmotes(
            textFieldValue.value,
            deleteEmotes = { newText, newCursorIndex ->
                textFieldValue.value = textFieldValue.value.copy(
                    text = newText,
                    selection = TextRange(newCursorIndex, newCursorIndex)
                )
            },
            emoteMap = emoteMap,
        )
        Log.d("addToken", "startScanningTokens()")
        tokenScanner.startScanningTokens()


    }


    /**
     * clickUsernameAutoTextChange() is a function meant to be run when a user clicks on a slash command after typing the `\` symbol.
     * It will create a new text with the clicked slash command and replace the old text that the user was typing
     *
     * @param command a string meant to represent the slash command that the user clicked on
     * @param slashCommandIndex a integer that represents where the parsing should begin for the slash command
     * @param cleanUp a function meant to represent any extra clean up that needs to take place.
     * */
    fun clickSlashCommandTextAutoChange(
        command: String,
    ) {
        val currentCharacterIndex = textFieldValue.value.selection.end

        val replacedString =
            textFieldValue.value.text.replaceRange(slashCommandIndex, currentCharacterIndex, "$command ")
        textFieldValue.value = textFieldValue.value.copy(
            text = replacedString,
            selection = TextRange(replacedString.length)
        )
        _forwardSlashCommands.clear()
        // filteredChatList.clear()
        clearFilteredChatterList()
        slashCommandIndex = 0
    }


    //
    fun parsingMethod(
        textFieldValue: TextFieldValue,
        allChatters: List<String>
    ) {
        try {
            val currentCharacter = textFieldValue.getTextBeforeSelection(1)  // this is the current text

            Log.d("newParsingAgainThing", "$currentCharacter")

            if (currentCharacter.toString() == "/") {
                Log.d("newParsingAgain", currentCharacter.toString())
                slashCommandState = true
                slashCommandIndex = textFieldValue.selection.start
                //_forwardSlashCommands.addAll(listOfCommands)
                addAllForwardSlashCommand(listOfCommands)
            }
            if (currentCharacter.toString() == " ") {
                Log.d("newParsingAgainThing", "currentCharacter.toString() == blank space")
                // _forwardSlashCommands.clear()
                clearForwardSlashCommand()
                slashCommandState = false
                slashCommandIndex = 0
            }
            if (currentCharacter.toString() == "" && slashCommandState) {
                Log.d("newParsingAgainThing", "end currentCharacter and slashCommandState true")
                // _forwardSlashCommands.clear()
                clearForwardSlashCommand()
                slashCommandState = false
                slashCommandIndex = 0
            }
            if (slashCommandState) {
                //todo here
                parseNFilterCommandList(textFieldValue)
            }

            if (currentCharacter.toString() == "") {
                Log.d("newParsingAgainThing", "end currentCharacter")
                endParsingNClearFilteredChatList()
                // _forwardSlashCommands.clear()
                clearForwardSlashCommand()
                slashCommandState = false
                slashCommandIndex = 0
            }


            if (textFieldValue.selection.start < parsingIndex && startParsing) {
                Log.d("newParsingAgainThing", "textFieldValue.selection.start < parsingIndex && startParsing")
                endParsingNClearFilteredChatList()
                // _forwardSlashCommands.clear()
                clearForwardSlashCommand()
                slashCommandState = false

            }

            if (currentCharacter.toString() == " " && startParsing) {
                Log.d("newParsingAgainThing", "currentCharacter.toString() == blankspace && startParsing")
                endParsingNClearFilteredChatList()
                // _forwardSlashCommands.clear()
                clearForwardSlashCommand()
                slashCommandState = false

            }
            /**---------set parsing to false should be above this line----------------*/
            if (startParsing) {
                parseNFilterChatList(textFieldValue)
            }

            if (currentCharacter.toString() == "@") {
                //  _forwardSlashCommands.clear()
                clearForwardSlashCommand()
                showFilteredChatListNStartParsing(textFieldValue, allChatters)
            }


        } catch (e: Exception) {
            endParsingNClearFilteredChatList()
            negateSlashCommandStateNClearForwardSlashCommands()
        }

    }

    /**
     * showFilteredChatListNStartParsing is a private function called when the current character the user is
     * typing is equal to ***@***. It sets [parsingIndex] to the current character index,[startParsing] to true
     * and adds all the current usernames in chat to [filteredChatList]
     *
     * @param textFieldValue a [TextFieldValue] that represents what the user is currently typing
     * */
    private fun showFilteredChatListNStartParsing(
        textFieldValue: TextFieldValue,
        allChatters: List<String>
    ) {
        Log.d("newParsingAgain", "-----------BEGIN PARSING----------")
        //  filteredChatList.clear()
        clearFilteredChatterList()
        addAllFilteredChatList(allChatters)
        // filteredChatList.addAll(allChatters)
        parsingIndex = textFieldValue.selection.start
        startParsing = true
    }

    /**
     * parseNFilterChatList is a private function called when [startParsing] is set to true. Its main
     * goal is to parse out the ***username*** from the [textFieldValue]. Then take that ***username***
     * and filter everything out of [filteredChatList] that does not match the ***username***
     *
     * @param textFieldValue a [TextFieldValue] that represents what the user is currently typing
     * */
    private fun parseNFilterChatList(textFieldValue: TextFieldValue) {
        val username = textFieldValue.text.subSequence(parsingIndex, textFieldValue.selection.end)

        val usernameRegex = Regex("^$username", RegexOption.IGNORE_CASE)
        filterChatListUsername(usernameRegex)
//        filteredChatList.removeIf{
//            !it.contains(usernameRegex)
//        }

    }

    /**BELOW IS THE COMMAND LIST!!!!!*/
    private fun parseNFilterCommandList(textFieldValue: TextFieldValue) {
        val command = "/" + textFieldValue.text.subSequence(slashCommandIndex, textFieldValue.selection.end)
        Log.d("parseNFilterCommandList", "command ----> $command")

        //val commandRegex = Regex("$command",RegexOption.IGNORE_CASE)
        val commandRegex = Regex("^$command", RegexOption.IGNORE_CASE)
        _forwardSlashCommands.removeIf {
            !it.title.contains(commandRegex)
        }

    }

    /**
     * endParsingNClearFilteredChatList is a private function meant to call ***.clear()*** on [filteredChatList] and
     * set [startParsing] to false
     * */
    private fun endParsingNClearFilteredChatList() {
        // filteredChatList.clear()
        clearFilteredChatterList()
        startParsing = false
    }

    /**
     * negateSlashCommandStateNClearForwardSlashCommands is a private function meant to call ***.clear()*** on [forwardSlashCommands] and
     * set [slashCommandState] to false
     * */
    private fun negateSlashCommandStateNClearForwardSlashCommands() {
        slashCommandState = false
    }

}

class DeletingEmotes(
    private val source: TextFieldValue,
    private val deleteEmotes: (String, Int) -> Unit,
    private val emoteMap: Map<String, InlineTextContent>
) {
    private var start = 0
    private var current = source.text.length - 1

    fun startScanningTokens() {
        if (source.text.isNotEmpty()) {
            start = current
            scanToken()
        }
    }

    private fun scanToken() {
        val c = source.text[current]
        Log.d("addToken", "current -> ${c}")
        when (c) {
            ' ' -> {
                // Log.d("deleteSpacered","c -> $c <---")
                deleteSpace()
            }

            else -> {
                while (notStartNullOrEmptySpace(peek())) {
                    Log.d("whileLooping", "current -> ${current}")
                    reverse()
                }
                deleteToken()
            }

        }
    }

    private fun isAtStart(): Boolean {
        return current <= 0
    }

    private fun reverse() {

        current = --current
    }

    private fun deleteSpace() {
        Log.d("addToken", "deleteSpace")

        val newString = source.text.removeRange(current, source.selection.start)

        val newCursorIndex = newString.length

        deleteEmotes(newString, newCursorIndex)
    }

    private fun deleteToken() {
        val text = source.text.substring(current, source.selection.start)
        val newString = source.text.removeRange(current, source.selection.start)
        Log.d("addToken", "deleteToken")


        if (emoteMap.containsKey(source.text.substring(current + 1, source.selection.start))) {
            Log.d("addToken", "true")
            val newCursorIndex = newString.length

            deleteEmotes(newString, newCursorIndex)
        } else {
            Log.d("addToken", "deleteSingleItem-> $text")
            deleteSingleItem()
        }
    }

    private fun deleteSingleItem() {
        val newString = source.text.removeRange(source.selection.start - 1, source.selection.start)
        val newCursorIndex = newString.length

        deleteEmotes(newString, newCursorIndex)
    }

    private fun peek(): Char {
        return if (isAtStart()) '\u0000' else source.text[current]
    }

    private fun notStartNullOrEmptySpace(c: Char): Boolean {
        Log.d("notStartNullOrEmptySpace", "c check -> ${c != ' '}")
        Log.d("notStartNullOrEmptySpace", "current check -> ${current <= 0}")
        return c != ' ' && current > 0
    }

}
