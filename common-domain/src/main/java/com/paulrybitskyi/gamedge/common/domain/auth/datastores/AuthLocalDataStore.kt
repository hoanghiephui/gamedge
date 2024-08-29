/*
 * Copyright 2022 Paul Rybitskyi, paul.rybitskyi.work@gmail.com
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

package com.paulrybitskyi.gamedge.common.domain.auth.datastores

import com.android.model.UserDataModel
import com.paulrybitskyi.gamedge.common.domain.auth.entities.OauthCredentials
import kotlinx.coroutines.flow.Flow

interface AuthLocalDataStore {
    suspend fun saveOauthCredentials(oauthCredentials: OauthCredentials)
    suspend fun getOauthCredentials(): OauthCredentials?
    suspend fun saveAuthorizationTokenTwitch(token: String)
    val authorizationTokenTwitch: Flow<String>
    suspend fun saveMyProfile(userDataModel: UserDataModel)
    suspend fun getMyProfile(): UserDataModel?
    val profileFlow: Flow<UserDataModel>
}
