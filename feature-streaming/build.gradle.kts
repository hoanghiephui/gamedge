plugins {
    id(libs.plugins.androidLibrary.get().pluginId)
    id(libs.plugins.gamedgeAndroid.get().pluginId)
    id(libs.plugins.kotlinKapt.get().pluginId)

    alias(libs.plugins.jetpackCompose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.daggerHilt)
}

android {
    namespace = "com.game.feature.streaming"
}

dependencies {
    implementation(project(localModules.commonDomain))
    implementation(project(localModules.core))
    implementation(project(localModules.commonUi))
    implementation(project(localModules.commonUiWidgets))
    implementation(project(localModules.igdbApi))
    implementation(libs.coroutines)

    implementation(libs.composeUi)
    implementation(libs.composeTooling)
    implementation(libs.composeFoundation)
    implementation(libs.composeMaterial)
    implementation(libs.composeRuntime)
    implementation(libs.composeHilt)

    implementation(libs.commonsCore)
    implementation(libs.commonsKtx)

    implementation(libs.kotlinResult)
    implementation(libs.coil)

    implementation(libs.daggerHiltAndroid)
    kapt(libs.daggerHiltAndroidCompiler)

    implementation(libs.hiltBinder)
    ksp(libs.hiltBinderCompiler)
    implementation("io.sanghun:compose-video:1.2.0")
    implementation("androidx.media3:media3-exoplayer:1.4.0") // [Required] androidx.media3 ExoPlayer dependency
    implementation("androidx.media3:media3-session:1.4.0") // [Required] MediaSession Extension dependency
    implementation("androidx.media3:media3-ui:1.4.0") // [Required] Base Player UI

    implementation("androidx.media3:media3-exoplayer-dash:1.4.0") // [Optional] If your media item is DASH
    implementation("androidx.media3:media3-exoplayer-hls:1.4.0") // [Optional] If your media item is HLS (m3u8..)
}
