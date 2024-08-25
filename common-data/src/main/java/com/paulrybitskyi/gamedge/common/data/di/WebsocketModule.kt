package com.paulrybitskyi.gamedge.common.data.di

import com.paulrybitskyi.gamedge.common.domain.websockets.AutoModMessageParsing
import com.paulrybitskyi.gamedge.common.data.websockets.ChatSettingsParsing
import com.paulrybitskyi.gamedge.common.data.websockets.ModActionParsing
import com.paulrybitskyi.gamedge.common.data.websockets.ParsingEngine
import com.paulrybitskyi.gamedge.common.data.websockets.TwitchEventSubWebSocket
import com.paulrybitskyi.gamedge.common.domain.websockets.TwitchEventSubscriptionWebSocket
import com.paulrybitskyi.gamedge.common.domain.websockets.TwitchSocket
import com.paulrybitskyi.gamedge.common.data.websockets.TwitchWebSocket
import com.paulrybitskyi.gamedge.common.domain.auth.datastores.AuthLocalDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object WebsocketModule {

    @Provides
    fun provideTwitchWebSocket(
        authLocalDataStore: AuthLocalDataStore,
        twitchParsingEngine: ParsingEngine
    ): TwitchSocket {
        return TwitchWebSocket(authLocalDataStore, twitchParsingEngine)
    }

    @Provides
    fun providesTwitchEventSubscriptionWebSocket(): TwitchEventSubscriptionWebSocket {
        val modActionParsing = ModActionParsing()
        val channelSettingsParsing = ChatSettingsParsing()
        val autoModMessageParsing = AutoModMessageParsing()
        return TwitchEventSubWebSocket(modActionParsing, channelSettingsParsing, autoModMessageParsing)
    }

    /*@Provides
    fun providesTwitchEventSubscriptions(
        twitchClient: TwitchClient
    ): TwitchEventSubscriptions{
        return TwitchEventSub(twitchClient)
    }*/
}
