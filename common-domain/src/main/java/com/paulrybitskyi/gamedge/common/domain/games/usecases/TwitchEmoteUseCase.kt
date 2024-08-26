package com.paulrybitskyi.gamedge.common.domain.games.usecases

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.android.model.websockets.EmoteData
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteListMap
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteNameUrl
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteNameUrlEmoteTypeList
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteNameUrlList
import com.paulrybitskyi.gamedge.common.domain.chat.IndivBetterTTVEmoteList
import com.paulrybitskyi.gamedge.common.domain.common.DispatcherProvider
import com.paulrybitskyi.gamedge.common.domain.common.DomainResult
import com.paulrybitskyi.gamedge.common.domain.common.extensions.resultOrError
import com.paulrybitskyi.gamedge.common.domain.games.datastores.GamesDataStores
import com.paulrybitskyi.gamedge.common.domain.repository.util.EmoteParsing
import com.paulrybitskyi.gamedge.core.Logger
import com.paulrybitskyi.gamedge.core.Response
import com.paulrybitskyi.gamedge.core.markers.Loggable
import com.paulrybitskyi.gamedge.core.utils.onError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

interface TwitchEmoteUseCase {
    val emoteList: State<EmoteListMap>
    val emoteBoardGlobalList: State<EmoteNameUrlList>
    val emoteBoardChannelList: State<EmoteNameUrlEmoteTypeList>
    val globalBetterTTVEmotes: State<IndivBetterTTVEmoteList>
    val channelBetterTTVEmotes: State<IndivBetterTTVEmoteList>
    val sharedBetterTTVEmotes: State<IndivBetterTTVEmoteList>
    val globalChatBadges: State<EmoteListMap>
    val combinedEmoteList: StateFlow<List<EmoteNameUrl>>
    val channelEmoteList: StateFlow<List<EmoteNameUrl>>
    val globalBetterTTVEmoteList: StateFlow<List<EmoteNameUrl>>
    val channelBetterTTVEmoteList: StateFlow<List<EmoteNameUrl>>
    val sharedBetterTTVEmoteList: StateFlow<List<EmoteNameUrl>>
    fun getGlobalEmotes(): Flow<Response<Boolean>>
}

@Singleton
class TwitchEmoteUseCaseImpl @Inject constructor(
    private val gamesDataStores: GamesDataStores,
    private val dispatcherProvider: DispatcherProvider,
    private val logger: Logger,
    private val emoteParsing: EmoteParsing = EmoteParsing()
) : TwitchEmoteUseCase, Loggable {
    private val modBadge = "https://static-cdn.jtvnw.net/badges/v1/3267646d-33f0-4b17-b3df-f923a41db1d0/1"
    private val subBadge = "https://static-cdn.jtvnw.net/badges/v1/5d9f2208-5dd8-11e7-8513-2ff4adfae661/1"
    private val feelsGood = "https://static-cdn.jtvnw.net/emoticons/v2/64138/static/light/1.0"
    private val feelsGoodId = "SeemsGood"

    //moderator subscriber
    private val modId = "moderator"
    private val subId = "subscriber"
    private val monitorId = "monitorIcon"
    private val badgeSize: Float = 20f

    //todo: this needs to be moved to the badgeListMap
    /** - inlineContentMap represents the inlineConent for the sub,mod and SeemsGood icons.
     * This is created before the [getGlobalEmotes] method is called so that there can still be mod and sub icons as soon as the
     * user loads into chat
     * - This value is hardcoded, so that even if all the other requests fail, the user will still be able to see the sub and mod badges
     *
     * */
    private val inlineContentMap = mapOf(
        Pair( //todo: This one can stay
            feelsGoodId,
            InlineTextContent(

                Placeholder(
                    width = 35.sp,
                    height = 35.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                AsyncImage(
                    model = feelsGood,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                )
            }
        ),

        )

    /***********************END OF THE inlineContentMap****************************************/
    private val inlineContentMapGlobalBadgeList = mapOf(
        Pair( //todo: This should get moved
            modId,
            InlineTextContent(

                Placeholder(
                    width = badgeSize.sp,
                    height = badgeSize.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                AsyncImage(
                    model = modBadge,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                )
            }
        ),
        Pair(
            subId,
            InlineTextContent(
                Placeholder(
                    width = badgeSize.sp,
                    height = badgeSize.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                AsyncImage(
                    model = subBadge,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                )
            }
        ),

        )
    private val _emoteList: MutableState<EmoteListMap> = mutableStateOf(EmoteListMap(inlineContentMap))

    override val emoteList: State<EmoteListMap> = _emoteList //this is what is shown in the chat UI(not emote box UI but chat UI )

    private val _globalChatBadges: MutableState<EmoteListMap> = mutableStateOf(EmoteListMap(inlineContentMapGlobalBadgeList))

    override val globalChatBadges: State<EmoteListMap> = _globalChatBadges

    private val _emoteBoardGlobalList = mutableStateOf<EmoteNameUrlList>(EmoteNameUrlList())
    override val emoteBoardGlobalList:State<EmoteNameUrlList> = _emoteBoardGlobalList

    private val _emoteBoardChannelList = mutableStateOf<EmoteNameUrlEmoteTypeList>(EmoteNameUrlEmoteTypeList())
    override val emoteBoardChannelList:State<EmoteNameUrlEmoteTypeList> = _emoteBoardChannelList

    /**Below are the parameters for the global emotes*/
    private val _globalBetterTTVEmotes = mutableStateOf<IndivBetterTTVEmoteList>(IndivBetterTTVEmoteList())
    override val globalBetterTTVEmotes:State<IndivBetterTTVEmoteList> = _globalBetterTTVEmotes
    private val _channelBetterTTVEmotes = mutableStateOf<IndivBetterTTVEmoteList>(IndivBetterTTVEmoteList())
    override val channelBetterTTVEmotes:State<IndivBetterTTVEmoteList> = _channelBetterTTVEmotes

    private val _sharedBetterTTVEmotes = mutableStateOf<IndivBetterTTVEmoteList>(IndivBetterTTVEmoteList())
    override val sharedBetterTTVEmotes:State<IndivBetterTTVEmoteList> = _sharedBetterTTVEmotes

    //this is used to hold the list for the chat UI states
    private val _combinedEmoteList = MutableStateFlow(listOf<EmoteNameUrl>())
    override val combinedEmoteList: StateFlow<List<EmoteNameUrl>> = _combinedEmoteList

    private val _channelEmoteList = MutableStateFlow(listOf<EmoteNameUrl>())
    override val channelEmoteList: StateFlow<List<EmoteNameUrl>> = _channelEmoteList

    private val _globalBetterTTVEmoteList = MutableStateFlow(listOf<EmoteNameUrl>())
    override val globalBetterTTVEmoteList: StateFlow<List<EmoteNameUrl>> = _globalBetterTTVEmoteList

    private val _channelBetterTTVEmoteList = MutableStateFlow(listOf<EmoteNameUrl>())
    override val channelBetterTTVEmoteList: StateFlow<List<EmoteNameUrl>> = _channelBetterTTVEmoteList

    private val _sharedBetterTTVEmoteList = MutableStateFlow(listOf<EmoteNameUrl>())
    override val sharedBetterTTVEmoteList: StateFlow<List<EmoteNameUrl>> = _sharedBetterTTVEmoteList

    override val logTag: String
        get() = javaClass.simpleName

    override fun getGlobalEmotes(): Flow<Response<Boolean>> = flow {
        getGlobalEmotesFlow()
            .resultOrError()
            .onError {
                logger.debug(logTag, "FAIL")
                logger.debug(logTag, "MESSAGE --> $it")
                emit(Response.Failure(Exception("Unable to delete message")))
            }
            .onStart {
                emit(Response.Loading)
            }
            .onCompletion {
                emit(Response.Success(true))
            }
            .distinctUntilChanged()
            .onEach { emittedUiState ->
                val newInnerInlineContentMap: MutableMap<String, InlineTextContent> = mutableMapOf()
                val data = emittedUiState.data
                val parsedEmoteData = data.map {
                    EmoteNameUrl(it.name, it.images.url_1x)
                }
                globalEmoteParsing(
                    newInnerInlineContentMap = newInnerInlineContentMap,
                    parsedEmoteData = parsedEmoteData,
                    updateEmoteListMap = { item ->
                        _emoteList.value = emoteList.value.copy(
                            map = item
                        )
                    },
                    updateEmoteList = { item ->
                        _emoteBoardGlobalList.value = _emoteBoardGlobalList.value.copy(
                            list = item
                        )
                    },
                    createMapValueForCompose = { emoteValue, innerInlineContentMapThinger ->
                        createMapValue(
                            emoteValue,
                            innerInlineContentMapThinger
                        )
                    },
                    updateInlineContent = {
                        //this is copying over inlineContentMap values to newInnerInlineContentMap
                        inlineContentMap.forEach {
                            newInnerInlineContentMap[it.key] = it.value
                        }
                    }
                )
            }
    }.flowOn(dispatcherProvider.main)

    private fun getGlobalEmotesFlow(): Flow<DomainResult<EmoteData>> = flow {
        emit(gamesDataStores.streamRepository.getGlobalEmotes())
    }.flowOn(dispatcherProvider.main)


    /**
     * globalEmoteParsing() is a private function used to update the [emoteList], [emoteBoardGlobalList]
     * and the [inlineContentMap].
     *
     * @param innerInlineContentMap is a [MutableMap] used to hold values used by the [InlineTextContent] objects showing the emotes
     * in the text chat
     * @param parsedEmoteData is a nullable List of [EmoteNameUrl] objects that is parsed from the request.
     * @param updateEmoteListMap a function used to update the local [emoteList] object
     * @param updateEmoteList a function used to update the local [emoteBoardGlobalList] object
     * @param createMapValueForCompose a function that is used to take [EmoteNameUrl] objects and add them to the [innerInlineContentMap]
     * @param updateInlineContent a function used to transfer the objects inside of [inlineContentMap] to the newly created [innerInlineContentMap]
     * */
    private fun globalEmoteParsing(
        newInnerInlineContentMap: MutableMap<String, InlineTextContent>,
        parsedEmoteData: List<EmoteNameUrl>?,
        updateEmoteListMap: (newInnerInlineContentMap: MutableMap<String, InlineTextContent>) -> Unit,
        updateEmoteList: (item: List<EmoteNameUrl>) -> Unit,
        createMapValueForCompose: (emoteValue: EmoteNameUrl, innerInlineContentMap: MutableMap<String, InlineTextContent>) -> Unit,
        updateInlineContent: () -> Unit,
    ) {
        updateInlineContent()
        if (parsedEmoteData !== null) {
            parsedEmoteData.forEach { emoteValue ->
                createMapValueForCompose(emoteValue, newInnerInlineContentMap)
            }
            updateEmoteListMap(newInnerInlineContentMap)
            updateEmoteList(parsedEmoteData)
        }
    }

    /**
     * createMapValue is a private function that creates the a [InlineTextContent] object and adds it to the
     * [innerInlineContentMap] parameter
     *
     * @param emoteValue a [EmoteNameUrl] object used to represent a Twitch emote
     * @param innerInlineContentMap a map used to represent what items are to be shown to the user
     * */
    private fun createMapValue(
        emoteValue: EmoteNameUrl,
        innerInlineContentMap: MutableMap<String, InlineTextContent>
    ) {
        //todo: I need to create a version of this that has the EmoteNameUrlEmoteType specifically for channel emotes
        emoteParsing.createMapValueForComposeChat(
            emoteValue,
            innerInlineContentMap
        )

    }
}
