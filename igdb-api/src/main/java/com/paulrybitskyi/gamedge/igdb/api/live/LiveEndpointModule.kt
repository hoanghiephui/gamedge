package com.paulrybitskyi.gamedge.igdb.api.live

import com.paulrybitskyi.gamedge.common.api.asConverterFactory
import com.paulrybitskyi.gamedge.common.api.calladapter.ApiResultCallAdapterFactory
import com.paulrybitskyi.gamedge.igdb.api.common.TwitchConstantsProvider
import com.paulrybitskyi.gamedge.igdb.api.common.di.qualifiers.Endpoint
import com.paulrybitskyi.gamedge.igdb.api.common.di.qualifiers.IgdbApi
import com.paulrybitskyi.gamedge.igdb.api.common.di.qualifiers.LiveApi
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
internal object LiveEndpointModule {
    @Provides
    @Singleton
    fun provideLiveEndpoint(
        liveService: LiveService,
    ): LiveEndpoint {
        return LiveEndpointImpl(
            liveService = liveService
        )
    }

    @Provides
    fun provideLiveService(@Endpoint(Endpoint.Type.LIVE) retrofit: Retrofit): LiveService {
        return retrofit.create(LiveService::class.java)
    }

    @Provides
    @Endpoint(Endpoint.Type.LIVE)
    fun provideRetrofit(
        @LiveApi okHttpClient: OkHttpClient,
        @IgdbApi callAdapterFactory: ApiResultCallAdapterFactory,
        json: Json,
        twitchConstantsProvider: TwitchConstantsProvider,
    ): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .addCallAdapterFactory(callAdapterFactory)
            .addConverterFactory(json.asConverterFactory())
            .baseUrl(twitchConstantsProvider.graphUrl)
            .build()
    }
}
