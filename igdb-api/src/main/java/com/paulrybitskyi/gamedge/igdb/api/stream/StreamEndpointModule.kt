package com.paulrybitskyi.gamedge.igdb.api.stream

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
internal object StreamEndpointModule {
    @Provides
    @Singleton
    fun provideStreamEndpoint(
        streamService: StreamService,
        twitchConstantsProvider: TwitchConstantsProvider,
    ): StreamEndpoint {
        return StreamEndpointImpl(
            streamService = streamService,
        )
    }

    @Provides
    fun provideStreamService(@Endpoint(Endpoint.Type.STREAM) retrofit: Retrofit): StreamService {
        return retrofit.create(StreamService::class.java)
    }

    @Provides
    @Endpoint(Endpoint.Type.STREAM)
    fun provideRetrofit(
        @StreamApi okHttpClient: OkHttpClient,
        @IgdbApi callAdapterFactory: ApiResultCallAdapterFactory,
        json: Json,
        twitchConstantsProvider: TwitchConstantsProvider,
    ): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .addCallAdapterFactory(callAdapterFactory)
            .addConverterFactory(json.asConverterFactory())
            .baseUrl(twitchConstantsProvider.steamTwitchUrl)
            .build()
    }
}
