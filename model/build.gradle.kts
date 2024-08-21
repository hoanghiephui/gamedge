plugins {
    id(libs.plugins.kotlinJvm.get().pluginId)
    id(libs.plugins.kotlinKapt.get().pluginId)
    alias(libs.plugins.kotlinxSerialization)
}

dependencies {
    implementation(libs.kotlinxSerialization)
}
