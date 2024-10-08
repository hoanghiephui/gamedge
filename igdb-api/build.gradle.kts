/*
 * Copyright 2020 Paul Rybitskyi, paul.rybitskyi.work@gmail.com
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

import com.paulrybitskyi.gamedge.extensions.property
import com.paulrybitskyi.gamedge.extensions.stringField
import java.util.Properties

plugins {
    id(libs.plugins.gamedgeRemoteApi.get().pluginId)
    id("kotlin-parcelize")
}
val localProperties = Properties()
val localFile = rootProject.file("local.properties")
if (localFile.exists()) {
    localProperties.load(localFile.inputStream())
}

val twitchAppClientId: String = localProperties.getProperty("TWITCH_APP_CLIENT_ID", "")
val twitchAppClientSecret: String = localProperties.getProperty("TWITCH_APP_CLIENT_SECRET", "")
val twitchHash: String = localProperties.getProperty("TWITCH_HASH_VIDEO", "")
val twitchHashChat: String = localProperties.getProperty("TWITCH_HASH_CHAT", "")
val twitchGRAPHQL: String = localProperties.getProperty("TWITCH_GRAPHQL_ID", "")


android {
    namespace = "com.paulrybitskyi.gamedge.igdb.api"

    defaultConfig {
        stringField("TWITCH_APP_CLIENT_ID", twitchAppClientId)
        stringField("TWITCH_APP_CLIENT_SECRET", twitchAppClientSecret)
        stringField("TWITCH_GRAPHQL_ID", twitchGRAPHQL)
        stringField("TWITCH_HASH_VIDEO", twitchHash)
        stringField("TWITCH_HASH_CHAT", twitchHashChat)
    }
}

dependencies {
    implementation(project(localModules.igdbApicalypse))
    implementation(project(":model"))
    implementation(libs.retrofitScalarsConverter)
}
