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

plugins {
    id(libs.plugins.androidLibrary.get().pluginId)
    id(libs.plugins.gamedgeKotlinCoroutines.get().pluginId)
    id(libs.plugins.gamedgeKotlinKapt.get().pluginId)
    id(libs.plugins.gamedgeJetpackCompose.get().pluginId)
    id("kotlin-parcelize")
    alias(libs.plugins.jetpackCompose)
}

android {
    namespace = "com.paulrybitskyi.gamedge.common.domain"
}

dependencies {
    implementation(libs.kotlinResult)
    api(libs.kotlinx.collections.immutable)
    implementation(libs.daggerHiltCore)
    kapt(libs.daggerHiltCoreCompiler)
    api(project(":model"))
    testImplementation(project(localModules.commonTestingDomain))
    testImplementation(libs.bundles.testing)
}
