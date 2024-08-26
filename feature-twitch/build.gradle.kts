plugins {
    id(libs.plugins.gamedgeFeature.get().pluginId)
}

android {
    namespace = "com.android.itube.feature.twitch"
}

dependencies {
    implementation(project(localModules.igdbApi))
}
