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
    id(libs.plugins.gamedgeAndroid.get().pluginId)
    id(libs.plugins.kotlinKapt.get().pluginId)
    id("kotlin-parcelize")
    alias(libs.plugins.jetpackCompose)
}

android {
    namespace = "com.paulrybitskyi.gamedge.common.domain"
}

dependencies {
    implementation(libs.coroutines)
    implementation(libs.kotlinResult)
    api(libs.kotlinx.collections.immutable)
    implementation(libs.daggerHiltCore)
    kapt(libs.daggerHiltCoreCompiler)
    api(project(":model"))

    implementation(platform(libs.composeBom))
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.navigationSuite)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui.util)
    implementation(libs.androidx.metrics)
    implementation(libs.compose.webview)
    implementation(libs.composeUi)
    implementation(libs.composeTooling)
    implementation(libs.composeFoundation)
    implementation(libs.composeRuntime)
    implementation(libs.composeMaterial)
    implementation(libs.accompanistSystemUi)

    implementation(libs.commonsCore)
    implementation(libs.commonsKtx)

    implementation(libs.coil)

    testImplementation(project(localModules.commonTestingDomain))
    testImplementation(libs.jUnit)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutinesTesting)
    testImplementation(libs.turbine)
}
