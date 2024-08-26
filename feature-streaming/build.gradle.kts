plugins {
    id(libs.plugins.gamedgeFeature.get().pluginId)
}

android {
    namespace = "com.game.feature.streaming"
}

dependencies {
    implementation(project(localModules.igdbApi))
    implementation(libs.androidx.media3.exoplayer) // [Required] androidx.media3 ExoPlayer dependency
    implementation(libs.androidx.media3.session) // [Required] MediaSession Extension dependency
    implementation(libs.androidx.media3.ui) // [Required] Base Player UI

    implementation(libs.androidx.media3.exoplayer.dash) // [Optional] If your media item is DASH
    implementation(libs.androidx.media3.exoplayer.hls) // [Optional] If your media item is HLS (m3u8..)
}
