package com.paulrybitskyi.gamedge.common.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.paulrybitskyi.gamedge.common.domain.ChatSettingsDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_config")

class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext
    private val context: Context
) : ChatSettingsDataStore {
    /**below are all the variables used to store data about the chat settings*/
    private val badgeSizeIdKey = floatPreferencesKey("badge_size_id")
    private val usernameSizeIdKey = floatPreferencesKey("username_size_id")
    private val messageSizeIdKey = floatPreferencesKey("message_size_id")
    private val emoteSizeIdKey = floatPreferencesKey("emote_size_id")
    private val lineHeightIdKey = floatPreferencesKey("line_height_id")
    private val customUsernameColorIdKey = booleanPreferencesKey("custom_username_color_id")
    override suspend fun setBadgeSize(badgeSize: Float) {
        try {
            context.dataStore.edit { tokens ->
                tokens[badgeSizeIdKey] = badgeSize
            }
            Log.d("UserPreferencesDataStore", "SUCCESS")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getBadgeSize(): Flow<Float> {
        val badgeSize: Flow<Float> = context.dataStore.data
            .map { preferences ->
                preferences[badgeSizeIdKey] ?: 20f
            }
        return badgeSize
    }

    override suspend fun setUsernameSize(usernameSize: Float) {
        try {
            context.dataStore.edit { tokens ->
                tokens[usernameSizeIdKey] = usernameSize
            }
            Log.d("UserPreferencesDataStore", "SUCCESS")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getUsernameSize(): Flow<Float> {
        val badgeSize: Flow<Float> = context.dataStore.data
            .map { preferences ->
                preferences[usernameSizeIdKey] ?: 15f
            }
        return badgeSize
    }

    override suspend fun setMessageSize(messageSize: Float) {
        try {
            context.dataStore.edit { tokens ->
                tokens[messageSizeIdKey] = messageSize
            }
            Log.d("UserPreferencesDataStore", "SUCCESS")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getMessageSize(): Flow<Float> {
        val badgeSize: Flow<Float> = context.dataStore.data
            .map { preferences ->
                preferences[messageSizeIdKey] ?: 15f
            }
        return badgeSize
    }

    override suspend fun setEmoteSize(emoteSize: Float) {
        try {
            context.dataStore.edit { tokens ->
                tokens[emoteSizeIdKey] = emoteSize
            }
            Log.d("UserPreferencesDataStore", "SUCCESS")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getEmoteSize(): Flow<Float> {
        val emoteSize: Flow<Float> = context.dataStore.data
            .map { preferences ->
                preferences[emoteSizeIdKey] ?: 35f
            }
        return emoteSize
    }

    override suspend fun setLineHeight(lineHeight: Float) {
        context.dataStore.edit { tokens ->
            tokens[lineHeightIdKey] = lineHeight
        }
    }

    override fun getLineHeight(): Flow<Float> {
        val lineHeight: Flow<Float> = context.dataStore.data
            .map { preferences ->
                preferences[lineHeightIdKey] ?: (15f * 1.6f)
            }
        return lineHeight
    }

    override suspend fun setCustomUsernameColor(showCustomUsernameColor: Boolean) {
        context.dataStore.edit { tokens ->
            tokens[customUsernameColorIdKey] = showCustomUsernameColor
        }
    }

    override fun getCustomUsernameColor(): Flow<Boolean> {
        val showCustomUsernameColor: Flow<Boolean> = context.dataStore.data
            .map { preferences ->
                preferences[customUsernameColorIdKey] ?: true
            }
        return showCustomUsernameColor
    }
}
