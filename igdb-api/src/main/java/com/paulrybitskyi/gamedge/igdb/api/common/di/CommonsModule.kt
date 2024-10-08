/*
 * Copyright 2021 Paul Rybitskyi, paul.rybitskyi.work@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paulrybitskyi.gamedge.igdb.api.common.di

import com.paulrybitskyi.gamedge.common.api.ErrorMessageExtractor
import com.paulrybitskyi.gamedge.common.api.addInterceptorAsFirstInChain
import com.paulrybitskyi.gamedge.common.api.calladapter.ApiResultCallAdapterFactory
import com.paulrybitskyi.gamedge.igdb.api.auth.Authorizer
import com.paulrybitskyi.gamedge.igdb.api.common.AuthorizationGraphInterceptor
import com.paulrybitskyi.gamedge.igdb.api.common.AuthorizationInterceptor
import com.paulrybitskyi.gamedge.igdb.api.common.AuthorizationStreamInterceptor
import com.paulrybitskyi.gamedge.igdb.api.common.CredentialsStore
import com.paulrybitskyi.gamedge.igdb.api.common.TwitchConstantsProvider
import com.paulrybitskyi.gamedge.igdb.api.common.di.qualifiers.IgdbApi
import com.paulrybitskyi.gamedge.igdb.api.common.di.qualifiers.LiveApi
import com.paulrybitskyi.gamedge.igdb.api.common.di.qualifiers.StreamApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object CommonsModule {

    @Provides
    @Singleton
    @IgdbApi
    fun provideOkHttpClient(
        okHttpClient: OkHttpClient,
        authorizationInterceptor: AuthorizationInterceptor,
        authenticator: Authenticator,
    ): OkHttpClient {
        return okHttpClient.newBuilder()
            .addInterceptorAsFirstInChain(authorizationInterceptor)
            .authenticator(authenticator)
            .build()
    }

    @Provides
    @Singleton
    @StreamApi
    fun provideOkHttpClientStream(
        okHttpClient: OkHttpClient,
        authorizationInterceptor: AuthorizationStreamInterceptor,
        authenticator: Authenticator,
    ): OkHttpClient {
        return okHttpClient.newBuilder()
            .addInterceptorAsFirstInChain(authorizationInterceptor)
            .authenticator(authenticator)
            .build()
    }

    @Provides
    @Singleton
    @LiveApi
    fun provideOkHttpClientLive(
        okHttpClient: OkHttpClient,
        authorizationInterceptor: AuthorizationGraphInterceptor,
        authenticator: Authenticator,
    ): OkHttpClient {
        return okHttpClient.newBuilder()
            .addInterceptorAsFirstInChain(authorizationInterceptor)
            .authenticator(authenticator)
            .build()
    }

    @Provides
    @IgdbApi
    fun provideApiResultCallAdapterFactory(
        @IgdbApi errorMessageExtractor: ErrorMessageExtractor,
    ): ApiResultCallAdapterFactory {
        return ApiResultCallAdapterFactory(errorMessageExtractor)
    }

    @Provides
    fun provideAuthorizationInterceptor(
        credentialsStore: CredentialsStore,
        authorizer: Authorizer,
        twitchConstantsProvider: TwitchConstantsProvider,
    ): AuthorizationInterceptor {
        return AuthorizationInterceptor(
            credentialsStore = credentialsStore,
            authorizer = authorizer,
            clientId = twitchConstantsProvider.clientId,
        )
    }

    @Provides
    fun provideAuthorizationStreamInterceptor(
        credentialsStore: CredentialsStore,
        authorizer: Authorizer,
        twitchConstantsProvider: TwitchConstantsProvider,
    ): AuthorizationStreamInterceptor {
        return AuthorizationStreamInterceptor(
            credentialsStore = credentialsStore,
            authorizer = authorizer,
            clientId = twitchConstantsProvider.clientId,
        )
    }

    @Provides
    fun provideAuthorizationGraphInterceptor(
        twitchConstantsProvider: TwitchConstantsProvider,
    ): AuthorizationGraphInterceptor {
        return AuthorizationGraphInterceptor(
            clientId = twitchConstantsProvider.twitchGRAP,
        )
    }
}
