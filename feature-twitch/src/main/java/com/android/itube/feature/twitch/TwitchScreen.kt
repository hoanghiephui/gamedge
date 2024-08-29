package com.android.itube.feature.twitch

import android.graphics.Bitmap
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.kevinnzou.web.AccompanistWebViewClient
import com.kevinnzou.web.LoadingState
import com.kevinnzou.web.WebView
import com.kevinnzou.web.WebViewNavigator
import com.kevinnzou.web.WebViewState
import com.kevinnzou.web.rememberWebViewNavigator
import com.kevinnzou.web.rememberWebViewState
import com.paulrybitskyi.gamedge.common.domain.live.entities.StreamPlaybackAccessToken
import com.paulrybitskyi.gamedge.common.ui.CommandsHandler
import com.paulrybitskyi.gamedge.common.ui.v2.component.NiaButton
import com.paulrybitskyi.gamedge.common.ui.widgets.NetworkError
import com.paulrybitskyi.gamedge.common.ui.widgets.TwitchLogo
import com.paulrybitskyi.gamedge.core.Constants.LOGIN_URL
import com.paulrybitskyi.gamedge.core.utils.getAccessTokenFromURL
import com.paulrybitskyi.gamedge.core.R as coreR

@Composable
internal fun TwitchRoute(
    onStreamingClick: (StreamPlaybackAccessToken) -> Unit,
    viewModel: TwitchViewModel = hiltViewModel(),
) {
    val navigation: NavHostController = rememberNavController()
    val authorizationTokenTwitch by viewModel.authorizationTokenTwitch.collectAsStateWithLifecycle()
    val streamingUrlState by viewModel.streamingUrlState.collectAsStateWithLifecycle()
    CommandsHandler(viewModel = viewModel)
    TwitchScreen(
        onSaveToken = {
            viewModel.onSaveToken(it)
        },
        authorizationTokenTwitch = authorizationTokenTwitch,
        viewModel = viewModel,
        navigation = navigation,
    )
    LaunchedEffect(viewModel, authorizationTokenTwitch) {
        if (authorizationTokenTwitch?.isNotBlank() == true) {
            viewModel.initialLoadIfNeeded()
            viewModel.getMyProfile()//TODO phai xem laij logic
            viewModel.getGlobalEmote()
        }
    }
    LaunchedEffect(streamingUrlState) {
        if (streamingUrlState != null) {
            onStreamingClick(streamingUrlState!!)
            viewModel.onClearStreamUrl()
        }
    }
}

@Composable
internal fun TwitchScreen(
    onSaveToken: (String) -> Unit,
    authorizationTokenTwitch: String?,
    viewModel: TwitchViewModel,
    navigation: NavHostController,
) {
    LoginScreen(
        onSaveToken,
        authorizationTokenTwitch,
        viewModel,
        navigation
    )
}

@Composable
private fun LoginScreen(
    onSaveToken: (String) -> Unit,
    authorizationTokenTwitch: String?,
    viewModel: TwitchViewModel,
    navigation: NavHostController,
) {
    var state by remember {
        mutableStateOf<UiState>(UiState.Init)
    }
    LaunchedEffect(authorizationTokenTwitch) {
        state = if (authorizationTokenTwitch?.isNotBlank() == true) {
            UiState.Loaded(token = authorizationTokenTwitch)
        } else {
            UiState.Login
        }
    }
    val stateWeb = rememberWebViewState(LOGIN_URL)
    val navigator = rememberWebViewNavigator()
    val webClient = remember {
        object : AccompanistWebViewClient() {
            override fun onPageStarted(
                view: WebView,
                url: String?,
                favicon: Bitmap?
            ) {
                super.onPageStarted(view, url, favicon)
                if (url != null && url.contains("access_token") && url.contains("oauth_authorizing")) {
                    val token = url.getAccessTokenFromURL()
                    onSaveToken(token)
                    state = UiState.Loaded(token = token)
                    Log.d("TOKEN", ": $token")
                } else if (url != null && url.contains("The+user+denied+you+access")) {
                    state = UiState.Error
                }
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                if (LOGIN_URL.equals(request?.url)) {
                    state = UiState.Error
                }
            }

        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            state,
            transitionSpec = {
                fadeIn(
                    animationSpec = tween(1500)
                ) togetherWith fadeOut(animationSpec = tween(1500))
            },
            label = "Animated Content"
        ) { targetState ->
            when (targetState) {
                UiState.Login -> {
                    InitLogin(onLogin = {
                        state = UiState.Loading
                    })
                }

                UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        MakeWebView(
                            navigator, stateWeb, webClient
                        )
                        if (stateWeb.loadingState is LoadingState.Loading) {
                            LoadingScreen()
                        }
                    }
                }

                is UiState.Loaded -> {
                    TwitchBoard(viewModel, navigation)
                }

                UiState.Error -> {
                    ErrorScreen()
                }

                UiState.Init -> {

                }
            }
        }
    }

}

@Composable
private fun MakeWebView(
    navigator: WebViewNavigator,
    stateWeb: WebViewState,
    webClient: AccompanistWebViewClient
) {
    WebView(
        modifier = Modifier.fillMaxSize(),
        navigator = navigator,
        state = stateWeb,
        onCreated = { nativeWebView ->
            nativeWebView.apply {
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
        },
        client = webClient
    )
}

@Composable
private fun LoadingScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "infinite transition")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "scale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(painter = painterResource(id = coreR.drawable.twitch_logo), contentDescription = "",
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin.Center
                }
                .align(Alignment.Center)
        )
    }
}

@Composable
private fun InitLogin(onLogin: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TwitchLogo(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterHorizontally)
            )
            Text(
                text = stringResource(R.string.login_title),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.login_desc),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 24.dp, end = 24.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(30.dp))
            NiaButton(
                onClick = onLogin,
                modifier = Modifier
                    .padding(
                        horizontal = 24.dp,
                        vertical = 16.dp
                    )
                    .widthIn(364.dp)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.login_twitch),
                )
            }
        }
    }
}

@Composable
private fun ErrorScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        NetworkError(
            modifier = Modifier.align(Alignment.Center),
            onClickReload = {},
        )
    }

}
