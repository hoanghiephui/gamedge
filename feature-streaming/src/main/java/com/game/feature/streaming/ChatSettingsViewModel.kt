package com.game.feature.streaming

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import coil.imageLoader
import com.android.model.websockets.ChatBadgePair
import com.paulrybitskyi.gamedge.common.domain.ChatSettingsDataStore
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteListMap
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteNameUrl
import com.paulrybitskyi.gamedge.common.domain.common.extensions.resultOrError
import com.paulrybitskyi.gamedge.common.domain.live.usecases.TwitchEmoteUseCase
import com.paulrybitskyi.gamedge.common.ui.base.BaseViewModel
import com.paulrybitskyi.gamedge.common.ui.base.events.common.GeneralCommand
import com.paulrybitskyi.gamedge.core.ErrorMapper
import com.paulrybitskyi.gamedge.core.Logger
import com.paulrybitskyi.gamedge.core.utils.onError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatSettingsViewModel @Inject constructor(
    private val chatSettingsDataStore: ChatSettingsDataStore,
    private val twitchEmoteUseCase: TwitchEmoteUseCase,
    private val logger: Logger,
    private val errorMapper: ErrorMapper,
) : BaseViewModel() {
    private val _badgeSize = mutableFloatStateOf(20f)  // Initial value
    val badgeSize: State<Float> = _badgeSize


    private val _emoteSize = mutableFloatStateOf(35f)  // Initial value
    val emoteSize: State<Float> = _emoteSize


    private val _usernameSize = mutableFloatStateOf(15f)  // Initial value
    val usernameSize: State<Float> = _usernameSize


    private val _messageSize = mutableFloatStateOf(15f)  // Initial value
    val messageSize: State<Float> = _messageSize


    private val _lineHeight = mutableFloatStateOf((15f * 1.6f))  // Initial value
    val lineHeight: State<Float> = _lineHeight

    private val _customUsernameColor = mutableStateOf(true)  // Initial value
    val customUsernameColor: State<Boolean> = _customUsernameColor


    val chatBadgeList = mutableStateListOf(
        // HARD CODED SO EVEN IF REQUEST TO GET BADGES FAILS, USER CAN STILL SEE SUBS AND MODS
        ChatBadgePair(
            url = "https://static-cdn.jtvnw.net/badges/v1/5d9f2208-5dd8-11e7-8513-2ff4adfae661/1",
            id = "subscriber"
        ),
        ChatBadgePair(
            url = "https://static-cdn.jtvnw.net/badges/v1/3267646d-33f0-4b17-b3df-f923a41db1d0/1",
            id = "moderator"
        )
    )

    private val globalEmoteList = mutableListOf<EmoteNameUrl>()
    private val channelEmoteList = mutableListOf<EmoteNameUrl>()
    private val globalBetterTTVEmoteList = mutableListOf<EmoteNameUrl>()
    val channelBetterTTVEmoteList = mutableListOf<EmoteNameUrl>()
    val sharedBetterTTVEmoteList = mutableListOf<EmoteNameUrl>()

    /***********EMOTE AND BADGE RELATED THINGS***************/
    private val inlineContentMapGlobalBadgeList = mutableStateOf(EmoteListMap(createNewBadgeMap()))
    val globalChatBadgesMap: State<EmoteListMap> = inlineContentMapGlobalBadgeList

    private val _globalEmoteInlineContentMap = mutableStateOf(EmoteListMap(mapOf()))
    val globalEmoteMap: State<EmoteListMap> = _globalEmoteInlineContentMap

    private val _channelEmoteInlineContentMap = mutableStateOf(EmoteListMap(mapOf()))
    val inlineContentMapChannelEmoteList: State<EmoteListMap> = _channelEmoteInlineContentMap

    private val _betterTTVGlobalInlineContentMapChannelEmoteList = mutableStateOf(EmoteListMap(mapOf()))
    val betterTTVGlobalInlineContentMapChannelEmoteList: State<EmoteListMap> =
        _betterTTVGlobalInlineContentMapChannelEmoteList

    private val _betterTTVChannelInlineContentMapChannelEmoteList = mutableStateOf(EmoteListMap(mapOf()))
    val betterTTVChannelInlineContentMapChannelEmoteList: State<EmoteListMap> =
        _betterTTVChannelInlineContentMapChannelEmoteList

    private val _betterTTVSharedInlineContentMapChannelEmoteList = mutableStateOf(EmoteListMap(mapOf()))
    val betterTTVSharedInlineContentMapChannelEmoteList: State<EmoteListMap> =
        _betterTTVSharedInlineContentMapChannelEmoteList

    init {
        getStoredBadgeSize()
        getUsernameSize()
        getMessageSize()
        getLineHeight()
        getShowCustomUsernameColor()
        getEmoteSize()
    }

    init {
        monitorForChannelTwitchEmotes()
    }

    init {
        monitorForGlobalTwitchEmotes()
    }

    init {
        monitorForGlobalBetterTTVEmotes()
    }

    init {
        monitorForChannelBetterTTVEmotes()
    }

    init {
        monitorForSharedBetterTTVEmotes()
    }

    fun getGlobalChatBadges() {
        if (chatBadgeList.toList().size == 2) {
            twitchEmoteUseCase.getGlobalChatBadges()
                .resultOrError()
                .onError {
                    logger.error(logTag, "Failed to MyProfile", it)
                    dispatchCommand(GeneralCommand.ShowLongToast(errorMapper.mapToMessage(it)))
                }.distinctUntilChanged()
                .onEach { emittedUiState ->
                    logger.info(logTag, "My Profile ${emittedUiState.first().id}")
                    if (emittedUiState.isNotEmpty()) {
                        chatBadgeList.clear()
                        chatBadgeList.addAll(emittedUiState)
                        inlineContentMapGlobalBadgeList.value = EmoteListMap(createNewBadgeMap())
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    private fun getStoredBadgeSize() {
        viewModelScope.launch {
            chatSettingsDataStore.getBadgeSize().collect { storedBadgeSize ->
                _badgeSize.floatValue = storedBadgeSize
            }
            createNewBadgeMap()
        }
    }

    private fun getUsernameSize() = viewModelScope.launch {
        chatSettingsDataStore.getUsernameSize().collect { storedUsernameSize ->
            _usernameSize.floatValue = storedUsernameSize
        }
    }

    private fun getMessageSize() = viewModelScope.launch {
        chatSettingsDataStore.getMessageSize().collect { storedMessageSize ->
            _messageSize.floatValue = storedMessageSize
        }
    }

    private fun getLineHeight() = viewModelScope.launch {
        chatSettingsDataStore.getLineHeight().collect { storedLineHeight ->
            _lineHeight.floatValue = storedLineHeight
        }
    }

    private fun getShowCustomUsernameColor() = viewModelScope.launch {
        chatSettingsDataStore.getCustomUsernameColor().collect { showCustomUsernameColor ->
            _customUsernameColor.value = showCustomUsernameColor
        }
    }

    private fun getEmoteSize() = viewModelScope.launch {
        chatSettingsDataStore.getEmoteSize().collect { storedEmoteSize ->
            _emoteSize.floatValue = storedEmoteSize
        }
    }

    private fun storeBadgeSizeLocally(newValue: Float) = viewModelScope.launch(Dispatchers.IO) {
        chatSettingsDataStore.setBadgeSize(newValue)
    }

    private fun monitorForChannelTwitchEmotes() {
        viewModelScope.launch {
            twitchEmoteUseCase.channelEmoteList.collect { response ->
                if (channelEmoteList.isEmpty()) {
                    channelEmoteList.addAll(response)
                    _channelEmoteInlineContentMap.value = EmoteListMap(createNewGlobalEmoteMap())
                }
                Log.d("combinedEmoteListing", "response ->${response}")
            }
        }
    }

    private fun monitorForGlobalTwitchEmotes() {
        viewModelScope.launch {
            twitchEmoteUseCase.combinedEmoteList.collect { response ->
                if (globalEmoteList.isEmpty()) {
                    globalEmoteList.addAll(response)
                    _globalEmoteInlineContentMap.value = EmoteListMap(createNewGlobalEmoteMap())
                }
                Log.d("channelEmoteList", "response ->${response}")
            }
        }
    }

    private fun monitorForGlobalBetterTTVEmotes() {
        viewModelScope.launch {
            twitchEmoteUseCase.globalBetterTTVEmoteList.collect { response ->
                if (globalBetterTTVEmoteList.isEmpty()) {
                    globalBetterTTVEmoteList.addAll(response)
                    _betterTTVGlobalInlineContentMapChannelEmoteList.value =
                        EmoteListMap(createBetterTTVGlobalEmoteMap())
                }

                Log.d("globalBetterTTVEmoteList", "response ->${response}")
            }
        }
    }

    private fun monitorForChannelBetterTTVEmotes() {
        viewModelScope.launch {
            twitchEmoteUseCase.channelBetterTTVEmoteList.collect { response ->
                if (channelBetterTTVEmoteList.isEmpty()) {
                    channelBetterTTVEmoteList.addAll(response)
                    _betterTTVChannelInlineContentMapChannelEmoteList.value =
                        EmoteListMap(createBetterTTVChanelEmoteMap())
                }

                Log.d("globalBetterTTVEmoteList", "response ->${response}")
            }
        }
    }

    private fun monitorForSharedBetterTTVEmotes() {
        viewModelScope.launch {
            twitchEmoteUseCase.sharedBetterTTVEmoteList.collect { response ->
                if (sharedBetterTTVEmoteList.isEmpty()) {
                    sharedBetterTTVEmoteList.addAll(response)
                    //the map creator needs to be changed
                    _betterTTVSharedInlineContentMapChannelEmoteList.value =
                        EmoteListMap(createBetterTTVSharedEmoteMap())
                }
                Log.d("sharedBetterTTVEmoteList", "response ->${response}")
            }
        }
    }

    private fun createNewGlobalEmoteMap(): Map<String, InlineTextContent> {
        val newMap = globalEmoteList.associate { emote ->
            Pair(
                emote.name,
                InlineTextContent(
                    Placeholder(
                        width = _emoteSize.floatValue.sp,
                        height = _emoteSize.floatValue.sp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                    )
                ) {
                    val imageLoader = LocalContext.current.imageLoader
                    AsyncImage(
                        imageLoader = imageLoader,
                        model = emote.url,
                        contentDescription = "${emote.name} badge",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                    )
                }
            )

        }
        return newMap
    }

    private fun createNewBadgeMap(): Map<String, InlineTextContent> {
        val newMap = chatBadgeList.associate { chatBadgeValue ->
            Pair(
                chatBadgeValue.id,
                InlineTextContent(

                    Placeholder(
                        width = _badgeSize.floatValue.sp,
                        height = _badgeSize.floatValue.sp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                    )
                ) {
                    val imageLoader = LocalContext.current.imageLoader
                    AsyncImage(
                        imageLoader = imageLoader,
                        model = chatBadgeValue.url,
                        contentDescription = "${chatBadgeValue.id} badge",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                    )
                }
            )
        }
        return newMap
    }

    private fun createNewChannelEmoteMap(): Map<String, InlineTextContent> {
        val newMap = channelEmoteList.associate { emote ->
            Pair(
                emote.name,
                InlineTextContent(

                    Placeholder(
                        width = _emoteSize.floatValue.sp,
                        height = _emoteSize.floatValue.sp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                    )
                ) {
                    val imageLoader = LocalContext.current.imageLoader
                    AsyncImage(
                        imageLoader = imageLoader,
                        model = emote.url,
                        contentDescription = "${emote.name} badge",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                    )
                }
            )

        }
        return newMap
    }

    private fun createBetterTTVGlobalEmoteMap(): Map<String, InlineTextContent> {
        val newMap = globalBetterTTVEmoteList.associate { emote ->
            Pair(
                emote.name,
                InlineTextContent(
                    Placeholder(
                        width = _emoteSize.floatValue.sp,
                        height = _emoteSize.floatValue.sp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                    )
                ) {
                    val imageLoader = LocalContext.current.imageLoader
                    AsyncImage(
                        imageLoader = imageLoader,
                        model = emote.url,
                        contentDescription = "${emote.name} badge",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                    )
                }
            )

        }
        return newMap
    }

    private fun createBetterTTVChanelEmoteMap(): Map<String, InlineTextContent> {
        val newMap = channelBetterTTVEmoteList.associate { emote ->
            Pair(
                emote.name,
                InlineTextContent(
                    Placeholder(
                        width = _emoteSize.floatValue.sp,
                        height = _emoteSize.floatValue.sp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                    )
                ) {
                    val imageLoader = LocalContext.current.imageLoader
                    AsyncImage(
                        imageLoader = imageLoader,
                        model = emote.url,
                        contentDescription = "${emote.name} badge",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                    )
                }
            )

        }
        return newMap
    }

    private fun createBetterTTVSharedEmoteMap(): Map<String, InlineTextContent> {
        val newMap = sharedBetterTTVEmoteList.associate { emote ->
            Pair(
                emote.name,
                InlineTextContent(
                    Placeholder(
                        width = _emoteSize.floatValue.sp,
                        height = _emoteSize.floatValue.sp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                    )
                ) {
                    val imageLoader = LocalContext.current.imageLoader
                    AsyncImage(
                        imageLoader = imageLoader,
                        model = emote.url,
                        contentDescription = "${emote.name} badge",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                    )
                }
            )
        }
        return newMap
    }


    fun changeBadgeSize(newValue: Float) {
        _badgeSize.floatValue = newValue
        inlineContentMapGlobalBadgeList.value = EmoteListMap(createNewBadgeMap())
        storeBadgeSizeLocally(newValue)
    }

    fun changeEmoteSize(newValue: Float) {
        _emoteSize.floatValue = newValue
        _globalEmoteInlineContentMap.value = EmoteListMap(createNewGlobalEmoteMap())
        _channelEmoteInlineContentMap.value = EmoteListMap(createNewChannelEmoteMap())
        _betterTTVGlobalInlineContentMapChannelEmoteList.value = EmoteListMap(createBetterTTVGlobalEmoteMap())
        _betterTTVChannelInlineContentMapChannelEmoteList.value = EmoteListMap(createBetterTTVChanelEmoteMap())
        _betterTTVSharedInlineContentMapChannelEmoteList.value = EmoteListMap(createBetterTTVSharedEmoteMap())
        storeEmoteSizeLocally(newValue)
    }

    fun changeUsernameSize(newValue: Float) {
        _usernameSize.floatValue = newValue
        storeUsernameSizeLocally(newValue)
    }

    fun changeMessageSize(newValue: Float) {
        _messageSize.floatValue = newValue
        storeMessageSizeLocally(newValue)
    }

    fun changeLineHeight(newValue: Float) {
        _lineHeight.floatValue = newValue
        storeLineHeightLocally(newValue)
    }

    fun changeCustomUsernameColor(newValue: Boolean) {
        _customUsernameColor.value = newValue
        storeCustomUsernameLocally(newValue)
    }

    private fun storeEmoteSizeLocally(newValue: Float) = viewModelScope.launch(Dispatchers.IO) {
        chatSettingsDataStore.setEmoteSize(newValue)
    }

    private fun storeUsernameSizeLocally(newValue: Float) = viewModelScope.launch(Dispatchers.IO) {
        chatSettingsDataStore.setUsernameSize(newValue)
    }

    private fun storeMessageSizeLocally(newValue: Float) = viewModelScope.launch(Dispatchers.IO) {
        chatSettingsDataStore.setMessageSize(newValue)
    }

    private fun storeLineHeightLocally(newValue: Float) = viewModelScope.launch(Dispatchers.IO) {
        chatSettingsDataStore.setLineHeight(newValue)
    }

    private fun storeCustomUsernameLocally(newValue: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        chatSettingsDataStore.setCustomUsernameColor(newValue)
    }

}
