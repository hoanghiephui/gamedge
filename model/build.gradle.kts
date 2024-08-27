import com.paulrybitskyi.gamedge.extensions.libs

plugins {
    id(libs.plugins.androidLibrary.get().pluginId)
    id(libs.plugins.gamedgeAndroid.get().pluginId)
    alias(libs.plugins.kotlinxSerialization)
    id(libs.plugins.gamedgeDaggerHilt.get().pluginId)
}

android {
    namespace = "com.android.model"
}

dependencies {
    implementation(libs.kotlinxSerialization)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.commonsCore)
    implementation(libs.commonsKtx)
    implementation(libs.commonsNetwork)
}
