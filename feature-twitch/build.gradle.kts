plugins {
    id(libs.plugins.androidLibrary.get().pluginId)
    id(libs.plugins.gamedgeAndroid.get().pluginId)
    id(libs.plugins.kotlinKapt.get().pluginId)

    alias(libs.plugins.jetpackCompose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.daggerHilt)
}

android {
    namespace = "com.android.itube.feature.twitch"
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

    testImplementation(project(localModules.commonTesting))
    testImplementation(libs.jUnit)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutinesTesting)
    testImplementation(libs.turbine)

    androidTestImplementation(libs.testRunner)
    androidTestImplementation(libs.jUnitExt)
}
