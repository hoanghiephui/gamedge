/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paulrybitskyi.gamedge.feature.likes.navigation

import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.kevinnzou.web.WebView
import com.kevinnzou.web.rememberWebViewNavigator
import com.kevinnzou.web.rememberWebViewState
import com.paulrybitskyi.gamedge.core.Constants

const val BOOKMARKS_ROUTE = "bookmarks_route"

fun NavController.navigateToBookmarks(navOptions: NavOptions) = navigate(BOOKMARKS_ROUTE, navOptions)

fun NavGraphBuilder.bookmarksScreen(
    onTopicClick: (String) -> Unit,
    onShowSnackbar: suspend (String, String?) -> Boolean,
) {
    composable(route = BOOKMARKS_ROUTE) {
        //BookmarksRoute(onTopicClick, onShowSnackbar)
        Login()
    }
}

private val LOGIN_URL = "https://id.twitch.tv/oauth2/authorize" +
        "?client_id=cs8ydzg1a67v3uau41e0f50zgit93q" +
        "&redirect_uri=http%3A%2F%2Flocalhost/oauth_authorizing" +
        "&response_type=token" +
        "&scope=${Constants.TWITCH_SCOPES.joinToString("%20")}"

@Composable
fun Login() {
    val state = rememberWebViewState(LOGIN_URL)
    val navigator = rememberWebViewNavigator()
    WebView(
        modifier = Modifier.fillMaxSize(),
        navigator = navigator,
        state = state,
        onCreated = { nativeWebView ->
            nativeWebView.apply {
                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()
                clipToOutline = true
                setBackgroundColor(Color.Transparent.toArgb())
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadsImagesAutomatically = true
                    allowFileAccess = true
                    javaScriptCanOpenWindowsAutomatically = true
                    mediaPlaybackRequiresUserGesture = true
                    cacheMode = WebSettings.LOAD_NO_CACHE
                }
            }
        }
    )
}

private fun customWebChromeClient(): WebChromeClient {
    val webChromeClient = object : WebChromeClient() {
        override fun onShowFileChooser(
            webView: WebView,
            filePathCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: FileChooserParams
        ): Boolean {
            //openFileChooser(filePathCallback)
            return true
        }
    }
    return webChromeClient
}
