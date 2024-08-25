package com.game.feature.streaming

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatSettingsViewModel @Inject constructor() : ViewModel() {
    private val _badgeSize = mutableStateOf(20f)  // Initial value
    val badgeSize: State<Float> = _badgeSize


    private val _emoteSize = mutableStateOf(35f)  // Initial value
    val emoteSize: State<Float> = _emoteSize


    private val _usernameSize = mutableStateOf(15f)  // Initial value
    val usernameSize: State<Float> = _usernameSize


    private val _messageSize = mutableStateOf(15f)  // Initial value
    val messageSize: State<Float> = _messageSize


    private val _lineHeight = mutableStateOf((15f * 1.6f))  // Initial value
    val lineHeight: State<Float> = _lineHeight

    private val _customUsernameColor = mutableStateOf(true)  // Initial value
    val customUsernameColor: State<Boolean> = _customUsernameColor

    fun changeBadgeSize(newValue: Float) {
        Log.d("changeBadgeSizeViewMode", "newValue ->${newValue}")
        _badgeSize.value = newValue
    }

    fun changeEmoteSize(newValue: Float) {
        _emoteSize.value = newValue

    }

    fun changeUsernameSize(newValue: Float) {
        _usernameSize.value = newValue
    }

    fun changeMessageSize(newValue: Float) {
        _messageSize.value = newValue
    }

    fun changeLineHeight(newValue: Float) {
        _lineHeight.value = newValue
    }

    fun changeCustomUsernameColor(newValue: Boolean) {
        _customUsernameColor.value = newValue
    }

}
