package com.android.itube.feature.twitch

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.android.itube.feature.twitch.state.StreamUiState
import com.android.itube.feature.twitch.state.disableLoading
import com.android.itube.feature.twitch.state.enableLoading
import com.android.itube.feature.twitch.state.toErrorState
import com.android.itube.feature.twitch.state.toLoadMoreSuccessState
import com.android.itube.feature.twitch.state.toSuccessState
import com.android.model.StreamItem
import com.paulrybitskyi.gamedge.common.domain.auth.datastores.AuthLocalDataStore
import com.paulrybitskyi.gamedge.common.domain.common.extensions.resultOrError
import com.paulrybitskyi.gamedge.common.domain.games.usecases.StreamUseCase
import com.paulrybitskyi.gamedge.common.domain.live.entities.StreamPlaybackAccessToken
import com.paulrybitskyi.gamedge.common.domain.live.usecases.LiveUseCase
import com.paulrybitskyi.gamedge.common.ui.base.BaseViewModel
import com.paulrybitskyi.gamedge.common.ui.base.events.common.GeneralCommand
import com.paulrybitskyi.gamedge.common.ui.di.qualifiers.TransitionAnimationDuration
import com.paulrybitskyi.gamedge.core.ErrorMapper
import com.paulrybitskyi.gamedge.core.Logger
import com.paulrybitskyi.gamedge.core.utils.onError
import com.paulrybitskyi.gamedge.igdb.api.maper.GraphMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TwitchViewModel @Inject constructor(
    private val authLocalDataStore: AuthLocalDataStore,
    private val streamUseCase: StreamUseCase,
    private val liveUseCase: LiveUseCase,
    private val errorMapper: ErrorMapper,
    private val logger: Logger,
    private val graphMapper: GraphMapper,
    @TransitionAnimationDuration
    private val transitionAnimationDuration: Long,
) : BaseViewModel() {
    private var hasMoreGamesToLoad = false

    var isRefreshLoading by mutableStateOf(false)
        private set

    var isInitialLoading by mutableStateOf(false)
        private set
    var isFetchLiveStreamURLLoading by mutableStateOf(false)
        private set

    private val _uiState = MutableStateFlow(createEmptyUiState())

    private val currentUiState: StreamUiState
        get() = _uiState.value

    val uiState: StateFlow<StreamUiState> = _uiState.asStateFlow()
    private val _streamingUrlState: MutableSharedFlow<StreamPlaybackAccessToken?> = MutableSharedFlow()
    val streamingUrlState = _streamingUrlState.asSharedFlow().stateIn(
        scope = viewModelScope,
        initialValue = null,
        started = SharingStarted.WhileSubscribed(5_000),
    )

    val authorizationTokenTwitch = authLocalDataStore.authorizationTokenTwitch.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    fun getLiveStreamURL(data: StreamItem) {
        if (isFetchLiveStreamURLLoading) {
            return
        }
        liveUseCase.getGraphQL(graphMapper.mapToGraphQLRequest(data), data)
            .resultOrError()
            .onError {
                logger.error(logTag, "Failed to GetLiveStreamURL", it)
                dispatchCommand(GeneralCommand.ShowLongToast(errorMapper.mapToMessage(it)))
            }.onStart {
                isFetchLiveStreamURLLoading = true
            }.onCompletion {
                isFetchLiveStreamURLLoading = false
            }.distinctUntilChanged()
            .onEach {
                logger.info(logTag, it.streamUrl)
                _streamingUrlState.emit(it)
            }.launchIn(viewModelScope)
    }

    fun onClearStreamUrl() {
        viewModelScope.launch {
            _streamingUrlState.emit(null)
        }
    }

    fun onSaveToken(token: String) {
        viewModelScope.launch {
            authLocalDataStore.saveAuthorizationTokenTwitch(token)
        }
    }

    fun initialLoadIfNeeded(resultEmissionDelay: Long = 0L) {
        if (isInitialLoading || isRefreshLoading) {
            return
        }
        streamUseCase.execute(true)
            .resultOrError()
            .map { data -> currentUiState.toSuccessState(data) }
            .onError {
                logger.error(logTag, "Failed to GetStreamItems", it)
                dispatchCommand(GeneralCommand.ShowLongToast(errorMapper.mapToMessage(it)))
                emit(currentUiState.toErrorState())
            }
            .onStart {
                isInitialLoading = true
                emit(currentUiState.enableLoading())
                // Show loading state for some time since it can be too quick
                delay(resultEmissionDelay)
            }
            .onCompletion {
                isInitialLoading = false
                // Delay disabling loading to avoid quick state changes like
                // empty, loading, empty, success
                delay(resultEmissionDelay)
                emit(currentUiState.disableLoading())
            }
            .distinctUntilChanged()
            .onEach { emittedUiState ->
                configureNextLoad(emittedUiState)
                _uiState.update { emittedUiState }
            }
            .launchIn(viewModelScope)
    }

    fun refreshGames(isClearPage: Boolean) {
        if (isInitialLoading || isRefreshLoading) {
            return
        }
        streamUseCase.execute(isClearPage)
            .resultOrError()
            .map { data ->
                if (isClearPage) {
                    currentUiState.toSuccessState(data)
                } else {
                    currentUiState.toLoadMoreSuccessState(data)
                }
            }
            .onError {
                logger.error(logTag, "Failed to refreshGames", it)
                dispatchCommand(GeneralCommand.ShowLongToast(errorMapper.mapToMessage(it)))
                emit(currentUiState.toErrorState())
            }
            .onStart {
                isRefreshLoading = true
                emit(currentUiState.enableLoading())
                // Show loading state for some time since it can be too quick
                val time = if (isClearPage) transitionAnimationDuration else 0L
                delay(time)
            }
            .onCompletion {
                // Delay disabling loading to avoid quick state changes like
                // empty, loading, empty, success
                val time = if (isClearPage) transitionAnimationDuration else 0L
                delay(time)
                isRefreshLoading = false
                emit(currentUiState.disableLoading())
            }
            .distinctUntilChanged()
            .onEach { emittedUiState ->
                configureNextLoad(emittedUiState)
                _uiState.update { emittedUiState }
            }
            .launchIn(viewModelScope)
    }

    fun onBottomReached() {
        loadMoreGames()
    }

    private fun loadMoreGames() {
        if (!hasMoreGamesToLoad) return
        Log.d("AAA", "${currentUiState.items.size}")
        fetchNextGamesBatch()
    }

    private fun fetchNextGamesBatch() {
        refreshGames(false)
    }

    private fun configureNextLoad(uiState: StreamUiState) {
        if (!uiState.hasLoadedNewGames()) return

        val paginationLimit = 20
        val gameCount = uiState.countSize
        Log.d("AAA", "Count: $gameCount")
        hasMoreGamesToLoad = (paginationLimit == gameCount)
    }

    private fun StreamUiState.hasLoadedNewGames(): Boolean {
        return (!isLoading && items.isNotEmpty())
    }

    private fun createEmptyUiState(): StreamUiState {
        return StreamUiState(
            isLoading = false,
            isError = false,
            title = "",
            items = emptyList(),
            countSize = 0
        )
    }
}
