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

package com.paulrybitskyi.gamedge.core

object Constants {
    const val SOURCE_CODE_LINK = "https://github.com/mars885/gamedge"
    private val TWITCH_SCOPES: Array<String> = arrayOf(
        "user:read:email",
        "user:edit:follows",
        "user:read:subscriptions",
        "chat:edit",
        "chat:read",
        "user:read:follows"
    )
    val LOGIN_URL = "https://id.twitch.tv/oauth2/authorize" +
            "?client_id=cs8ydzg1a67v3uau41e0f50zgit93q" +
            "&redirect_uri=http%3A%2F%2Flocalhost/oauth_authorizing" +
            "&response_type=token" +
            "&scope=${TWITCH_SCOPES.joinToString("%20")}"

    const val STREAM_URL = "https://usher.ttvnw.net/api/channel/hls/%s.m3u8" +
            "?player=twitchweb&" +
            "&token=%s" +
            "&sig=%s" +
            "&allow_audio_only=true" +
            "&allow_source=true" +
            "&type=any" +
            "&fast_bread=true" +
            "&p=%s"
}
