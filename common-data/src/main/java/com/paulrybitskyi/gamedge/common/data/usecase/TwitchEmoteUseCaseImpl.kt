package com.paulrybitskyi.gamedge.common.data.usecase

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.imageLoader
import com.android.model.BetterTTVChannelEmotes
import com.android.model.IndivBetterTTVEmote
import com.android.model.Response
import com.android.model.websockets.ChatBadgePair
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteListMap
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteNameUrl
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteNameUrlEmoteType
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteNameUrlEmoteTypeList
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteNameUrlList
import com.paulrybitskyi.gamedge.common.domain.chat.EmoteTypes
import com.paulrybitskyi.gamedge.common.domain.chat.IndivBetterTTVEmoteList
import com.paulrybitskyi.gamedge.common.domain.common.DispatcherProvider
import com.paulrybitskyi.gamedge.common.domain.common.DomainResult
import com.paulrybitskyi.gamedge.common.domain.games.datastores.GamesDataStores
import com.paulrybitskyi.gamedge.common.domain.live.usecases.TwitchEmoteUseCase
import com.paulrybitskyi.gamedge.common.domain.repository.util.EmoteParsing
import com.paulrybitskyi.gamedge.core.Logger
import com.paulrybitskyi.gamedge.core.handleException
import com.paulrybitskyi.gamedge.core.markers.Loggable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TwitchEmoteUseCaseImpl @Inject constructor(
    private val gamesDataStores: GamesDataStores,
    private val dispatcherProvider: DispatcherProvider,
    private val logger: Logger,
    private val emoteParsing: EmoteParsing
) : TwitchEmoteUseCase, Loggable {
    override val logTag: String
        get() = javaClass.simpleName

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
                val imageLoader = LocalContext.current.imageLoader
                AsyncImage(
                    imageLoader = imageLoader,
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
                val imageLoader = LocalContext.current.imageLoader
                AsyncImage(
                    imageLoader = imageLoader,
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
                val imageLoader = LocalContext.current.imageLoader
                AsyncImage(
                    imageLoader = imageLoader,
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

    override val emoteList: State<EmoteListMap> =
        _emoteList //this is what is shown in the chat UI(not emote box UI but chat UI )

    private val _globalChatBadges: MutableState<EmoteListMap> =
        mutableStateOf(EmoteListMap(inlineContentMapGlobalBadgeList))

    override val globalChatBadges: State<EmoteListMap> = _globalChatBadges

    private val _emoteBoardGlobalList = mutableStateOf(EmoteNameUrlList())
    override val emoteBoardGlobalList: State<EmoteNameUrlList> = _emoteBoardGlobalList

    private val _emoteBoardChannelList = mutableStateOf(EmoteNameUrlEmoteTypeList())
    override val emoteBoardChannelList: State<EmoteNameUrlEmoteTypeList> = _emoteBoardChannelList

    /**Below are the parameters for the global emotes*/
    private val _globalBetterTTVEmotes = mutableStateOf(IndivBetterTTVEmoteList())
    override val globalBetterTTVEmotes: State<IndivBetterTTVEmoteList> = _globalBetterTTVEmotes
    private val _channelBetterTTVEmotes = mutableStateOf(IndivBetterTTVEmoteList())
    override val channelBetterTTVEmotes: State<IndivBetterTTVEmoteList> = _channelBetterTTVEmotes

    private val _sharedBetterTTVEmotes = mutableStateOf(IndivBetterTTVEmoteList())
    override val sharedBetterTTVEmotes: State<IndivBetterTTVEmoteList> = _sharedBetterTTVEmotes

    //this is used to hold the list for the chat UI states
    private val _combinedEmoteList = MutableStateFlow(persistentListOf<EmoteNameUrl>())
    override val combinedEmoteList: StateFlow<ImmutableList<EmoteNameUrl>> = _combinedEmoteList

    private val _channelEmoteList = MutableStateFlow(persistentListOf<EmoteNameUrl>())
    override val channelEmoteList: StateFlow<ImmutableList<EmoteNameUrl>> = _channelEmoteList

    private val _globalBetterTTVEmoteList = MutableStateFlow(persistentListOf<EmoteNameUrl>())
    override val globalBetterTTVEmoteList: StateFlow<ImmutableList<EmoteNameUrl>> = _globalBetterTTVEmoteList

    private val _channelBetterTTVEmoteList = MutableStateFlow(persistentListOf<EmoteNameUrl>())
    override val channelBetterTTVEmoteList: StateFlow<ImmutableList<EmoteNameUrl>> = _channelBetterTTVEmoteList

    private val _sharedBetterTTVEmoteList = MutableStateFlow(persistentListOf<EmoteNameUrl>())
    override val sharedBetterTTVEmoteList: StateFlow<ImmutableList<EmoteNameUrl>> = _sharedBetterTTVEmoteList


    override fun getGlobalEmotes(): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        val response = gamesDataStores.streamRepository.getGlobalEmotes()
        val newInnerInlineContentMap: MutableMap<String, InlineTextContent> = mutableMapOf()
        if (response.isOk) {
            val data = response.value.data
            val parsedEmoteData = data.map {
                EmoteNameUrl(it.id, it.name, it.images.url_2x)
            }
            _combinedEmoteList.tryEmit(parsedEmoteData.toPersistentList())
            //todo: this function signature is terrible, confusing  and needs to be changed
            globalEmoteParsing(
                newInnerInlineContentMap = newInnerInlineContentMap,
                parsedEmoteData = parsedEmoteData.distinctBy { it.id }.toImmutableList(),
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

            emit(Response.Success(true))
        } else {
            logger.debug(logTag, "FAIL")
            logger.debug(logTag, "MESSAGE --> ${response.error}")
            emit(Response.Failure(Exception("Unable to delete message")))
        }
    }.catch { cause ->
        logger.debug(logTag, "caught error message --> ${cause.message}")
        handleException(cause)
    }

    override fun getChannelEmotes(broadcasterId: String): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        val response = gamesDataStores.streamRepository.getChannelEmotes(
            broadcasterId = broadcasterId
        )
        if (response.isOk) {
            val data = response.value.data
            val parsedEmoteData = data.map {// getting data from the request
                val emoteType = if (it.emote_type == "subscriptions") EmoteTypes.SUBS else EmoteTypes.FOLLOWERS
                Log.d("getChannelEmotesId", "emote_type -->$emoteType")
                EmoteNameUrlEmoteType(it.id, it.name, it.images.url_2x, emoteType)
            }
            val followerEmotes = parsedEmoteData.filter { it.emoteType == EmoteTypes.FOLLOWERS }
            val subscriberEmotes = parsedEmoteData.filter { it.emoteType == EmoteTypes.SUBS }
            val sortedEmoteData = followerEmotes + subscriberEmotes

            val innerInlineContentMap: MutableMap<String, InlineTextContent> = mutableMapOf()
            val newChannelEmoteList = parsedEmoteData.map {
                EmoteNameUrl(
                    id = it.id,
                    name = it.name,
                    url = it.url
                )
            }
            _channelEmoteList.tryEmit(newChannelEmoteList.distinctBy { it.id }.toPersistentList())


            //todo: this function signature is terrible, confusing  and needs to be changed
            newChannelEmoteParsing(
                parsedEmoteData = sortedEmoteData.distinctBy { it.id }.toImmutableList(),
                innerInlineContentMap = innerInlineContentMap,
                convertResponseDataToGlobalEmoteMap = { emoteValue, innerMap ->
                    createChannelEmoteMapValue(
                        emoteValue,
                        innerMap
                    )
                },
                updateGlobalEmoteMap = { innerMap ->
                    _emoteList.value =
                        emoteList.value.copy( // update _emoteList with the newly updated innerInlineContentMap

                            map = _emoteList.value.map + innerMap
                        )

                },
                updateChannelEmoteBoard = { channelEmoteList ->
                    _emoteBoardChannelList.value = _emoteBoardChannelList.value.copy(
                        list = channelEmoteList
                    )
                }
            )
            Log.d("getChannelEmotes", "body--> ${response.value}")

        } else {
            Log.d("getChannelEmotes", "FAIL")
            Log.d("getChannelEmotes", "MESSAGE --> ${response.error}")
            emit(Response.Failure(Exception("Unable to get emotes")))
        }
    }.catch { cause ->
        Log.d("getChannelEmotes", "EXCEPTION error message ->${cause.message}")
        Log.d("getChannelEmotes", "EXCEPTION error cause ->${cause.cause}")
        emit(Response.Failure(Exception("Unable to get emotes")))
    }

    override fun getBetterTTVGlobalEmotes(): Flow<DomainResult<List<IndivBetterTTVEmote>>> = flow {
        emit(gamesDataStores.tvEmoteRepository.getGlobalEmotes())
    }.flowOn(dispatcherProvider.main)

    override fun getGlobalChatBadges(): Flow<DomainResult<List<ChatBadgePair>>> = flow {
        emit(gamesDataStores.streamRepository.getGlobalChatBadges())
    }.flowOn(dispatcherProvider.main)

    override fun getBetterTTVChannelEmotes(broadcasterId: String): Flow<Response<BetterTTVChannelEmotes>> = flow {
        emit(Response.Loading)
        Log.d("getBetterTTVChannelEmotes", "LOADING")
        val response = gamesDataStores.tvEmoteRepository.getChannelEmotes(broadcasterId)
        if (response.isOk) {
            Log.d("getBetterTTVChannelEmotes", "SUCCESS")
            emit(Response.Success(BetterTTVChannelEmotes()))
            val sharedEmotes = response.value.sharedEmotes
            val channelEmotes = response.value.channelEmotes
            Log.d("getBetterTTVChannelEmotes", "sharedEmotes ->$sharedEmotes")
            Log.d("getBetterTTVChannelEmotes", "channelEmotes ->$channelEmotes")

            val channelBetterTTVEmoteList = channelEmotes.map {
                EmoteNameUrl(
                    id = it.id,
                    name = it.code,
                    url = "https://cdn.betterttv.net/emote/${it.id}/2x"
                )
            }
            val sharedBetterTTVEmoteList = sharedEmotes.map {
                EmoteNameUrl(
                    id = it.id,
                    name = it.code,
                    url = "https://cdn.betterttv.net/emote/${it.id}/2x"
                )
            }
            _channelBetterTTVEmoteList.tryEmit(channelBetterTTVEmoteList.toPersistentList())
            _sharedBetterTTVEmoteList.tryEmit(sharedBetterTTVEmoteList.toPersistentList())
            val sharedAndChannelList = mutableListOf<EmoteNameUrlEmoteType>()

            channelEmotes.also { listOfChannelEmotes ->
                val parsedChannelEmotes = listOfChannelEmotes.map { channelEmote ->
                    IndivBetterTTVEmote(
                        id = channelEmote.id,
                        code = channelEmote.code,
                        imageType = channelEmote.imageType,
                        animated = channelEmote.animated,
                        userId = channelEmote.userId,
                        modifier = false
                    )
                }
                Log.d("getBetterTTVChannelEmotes", "parsedData ->$parsedChannelEmotes")
                _channelBetterTTVEmotes.value = _channelBetterTTVEmotes.value.copy(
                    list = parsedChannelEmotes.toImmutableList()
                )
                listOfChannelEmotes.forEach {
                    sharedAndChannelList.add(
                        EmoteNameUrlEmoteType(
                            id = it.id,
                            name = it.code,
                            url = "https://cdn.betterttv.net/emote/${it.id}/2x",
                            emoteType = EmoteTypes.FOLLOWERS
                        )
                    )
                }
            }

            sharedEmotes.also { listOfChannelEmotes ->
                val parsedSharedEmotes = listOfChannelEmotes.map { channelEmote ->
                    IndivBetterTTVEmote(
                        id = channelEmote.id,
                        code = channelEmote.code,
                        imageType = channelEmote.imageType,
                        animated = channelEmote.animated,
                        userId = channelEmote.id,
                        modifier = false
                    )
                }
                _sharedBetterTTVEmotes.value = _sharedBetterTTVEmotes.value.copy(
                    list = parsedSharedEmotes.toImmutableList()
                )
                listOfChannelEmotes.forEach {
                    sharedAndChannelList.add(
                        EmoteNameUrlEmoteType(
                            id = it.id,
                            name = it.code,
                            url = "https://cdn.betterttv.net/emote/${it.id}/2x",
                            emoteType = EmoteTypes.FOLLOWERS
                        )
                    )
                }
            }
            val innerInlineContentMap: MutableMap<String, InlineTextContent> = mutableMapOf()
            sharedAndChannelList.forEach { emoteValue -> // convert the parsed data into values that can be stored into _emoteList
                createChannelEmoteMapValue(emoteValue, innerInlineContentMap)
            }
            _emoteList.value = emoteList.value.copy(
                map = _emoteList.value.map + innerInlineContentMap
            )
            Log.d("getBetterTTVChannelEmotes", "DONE")
        } else {
            Log.d("getBetterTTVChannelEmotes", "FAILED")
        }
    }.catch { cause ->
        Log.d("getChannelEmotes", "EXCEPTION error message ->${cause.message}")
        Log.d("getChannelEmotes", "EXCEPTION error cause ->${cause.cause}")
        emit(Response.Failure(Exception("Unable to get emotes")))
    }

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
        parsedEmoteData: ImmutableList<EmoteNameUrl>?,
        updateEmoteListMap: (newInnerInlineContentMap: MutableMap<String, InlineTextContent>) -> Unit,
        updateEmoteList: (item: ImmutableList<EmoteNameUrl>) -> Unit,
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

    /**
     * channelEmoteParsing() is a private function that is used for parsing out the emotes from the request asking Twitch servers
     * to get Channel specific emotes
     * @param innerInlineContentMap is a [MutableMap] used to hold values used by the [InlineTextContent] objects showing the emotes
     * in the text chat
     * @param parsedEmoteData is a nullable List of [EmoteNameUrl] objects that is parsed from the request.
     * @param convertResponseDataToGlobalEmoteMap a function used to convert the data coming from the request to a map that can be added to the emote map
     * @param updateGlobalEmoteMap a function that takes the newly updated [innerInlineContentMap] and adds it to the [emoteList]
     * @param updateChannelEmoteBoard a function that takes the [parsedEmoteData] and updates the [emoteBoardChannelList]
     * */
    private fun newChannelEmoteParsing(
        parsedEmoteData: ImmutableList<EmoteNameUrlEmoteType>?,
        innerInlineContentMap: MutableMap<String, InlineTextContent>,
        convertResponseDataToGlobalEmoteMap: (emoteValue: EmoteNameUrlEmoteType, innerInlineContentMap: MutableMap<String, InlineTextContent>) -> Unit,
        updateGlobalEmoteMap: (innerMap: MutableMap<String, InlineTextContent>) -> Unit,
        updateChannelEmoteBoard: (channelEmoteList: ImmutableList<EmoteNameUrlEmoteType>) -> Unit
    ) {
        if (parsedEmoteData !== null) {
            parsedEmoteData.forEach { emoteValue -> // convert the parsed data into values that can be stored into _emoteList
                convertResponseDataToGlobalEmoteMap(emoteValue, innerInlineContentMap)
            }
            updateGlobalEmoteMap(innerInlineContentMap)
            updateChannelEmoteBoard(parsedEmoteData)

        }
    }

    private fun createChannelEmoteMapValue(
        emoteValue: EmoteNameUrlEmoteType,
        innerInlineContentMap: MutableMap<String, InlineTextContent>
    ) {
//        emoteParsing.createMapValueForComposeChatChannelEmotes(
//            emoteValue,
//            innerInlineContentMap
//        )

    }
}
