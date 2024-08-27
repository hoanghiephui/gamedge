package com.game.feature.streaming

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.paulrybitskyi.gamedge.common.domain.ChatSettingsDataStore
import com.paulrybitskyi.gamedge.common.domain.live.usecases.TwitchEmoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatSettingsViewModel @Inject constructor(
    private val chatSettingsDataStore: ChatSettingsDataStore,
    private val twitchEmoteUseCase: TwitchEmoteUseCase,
) : ViewModel() {
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

    fun changeBadgeSize(newValue: Float) {
        Log.d("changeBadgeSizeViewMode", "newValue ->${newValue}")
        _badgeSize.floatValue = newValue
    }

    fun changeEmoteSize(newValue: Float) {
        _emoteSize.floatValue = newValue

    }

    fun changeUsernameSize(newValue: Float) {
        _usernameSize.floatValue = newValue
    }

    fun changeMessageSize(newValue: Float) {
        _messageSize.floatValue = newValue
    }

    fun changeLineHeight(newValue: Float) {
        _lineHeight.floatValue = newValue
    }

    fun changeCustomUsernameColor(newValue: Boolean) {
        _customUsernameColor.value = newValue
    }

}
