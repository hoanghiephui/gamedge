package com.paulrybitskyi.gamedge.igdb.api.emote

import com.paulrybitskyi.gamedge.common.api.asConverterFactory
import com.paulrybitskyi.gamedge.common.api.calladapter.ApiResultCallAdapterFactory
import com.paulrybitskyi.gamedge.igdb.api.common.TwitchConstantsProvider
import com.paulrybitskyi.gamedge.igdb.api.common.di.qualifiers.Endpoint
import com.paulrybitskyi.gamedge.igdb.api.common.di.qualifiers.IgdbApi
import com.paulrybitskyi.gamedge.igdb.api.common.di.qualifiers.StreamApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BetterTTVEmoteModule {
    @Provides
    @Singleton
    fun provideEmoteEndpoint(
        betterTTVEmoteService: BetterTTVEmoteService
    ): BetterTTVEmoteEndpoint {
        return BetterTTVEmoteEndpointImpl(
            betterTTVEmoteService = betterTTVEmoteService
        )
    }

    @Provides
    fun provideEmoteService(@Endpoint(Endpoint.Type.EMOTE) retrofit: Retrofit): BetterTTVEmoteService {
        return retrofit.create(BetterTTVEmoteService::class.java)
    }

    @Provides
    @Endpoint(Endpoint.Type.EMOTE)
    fun provideEmoteRetrofit(
        @StreamApi okHttpClient: OkHttpClient,
        @IgdbApi callAdapterFactory: ApiResultCallAdapterFactory,
        json: Json,
        twitchConstantsProvider: TwitchConstantsProvider,
    ): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .addCallAdapterFactory(callAdapterFactory)
            .addConverterFactory(json.asConverterFactory())
            .baseUrl(twitchConstantsProvider.apiBetterTtvUrl)
            .build()
    }
}
