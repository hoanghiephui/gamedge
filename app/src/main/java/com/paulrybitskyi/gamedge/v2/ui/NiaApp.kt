package com.paulrybitskyi.gamedge.v2.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration.Indefinite
import androidx.compose.material3.SnackbarDuration.Short
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult.ActionPerformed
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.paulrybitskyi.gamedge.common.ui.v2.component.NiaBackground
import com.paulrybitskyi.gamedge.common.ui.v2.component.NiaGradientBackground
import com.paulrybitskyi.gamedge.common.ui.v2.component.NiaNavigationSuiteScaffold
import com.paulrybitskyi.gamedge.common.ui.v2.component.NiaTopAppBar
import com.paulrybitskyi.gamedge.common.ui.v2.icon.NiaIcons
import com.paulrybitskyi.gamedge.common.ui.v2.theme.GradientColors
import com.paulrybitskyi.gamedge.common.ui.v2.theme.LocalGradientColors
import com.paulrybitskyi.gamedge.v2.navigation.NiaNavHost
import com.paulrybitskyi.gamedge.v2.navigation.TopLevelDestination
import com.paulrybitskyi.gamedge.core.R as coreR
import com.paulrybitskyi.gamedge.feature.settings.R as settingsR

@Composable
fun NiaApp(
    appState: NiaAppState,
    modifier: Modifier = Modifier,
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(),
) {
    val shouldShowGradientBackground =
        appState.currentTopLevelDestination == TopLevelDestination.FOR_YOU
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }

    NiaBackground(modifier = modifier) {
        NiaGradientBackground(
            gradientColors = if (shouldShowGradientBackground) {
                LocalGradientColors.current
            } else {
                GradientColors()
            },
        ) {
            val snackbarHostState = remember { SnackbarHostState() }

            val isOffline by appState.isOffline.collectAsStateWithLifecycle()

            // If user is not connected to the internet show a snack bar to inform them.
            val notConnectedMessage = stringResource(coreR.string.not_connected)
            LaunchedEffect(isOffline) {
                if (isOffline) {
                    snackbarHostState.showSnackbar(
                        message = notConnectedMessage,
                        duration = Indefinite,
                    )
                }
            }

            NiaApp(
                appState = appState,
                snackbarHostState = snackbarHostState,
                showSettingsDialog = showSettingsDialog,
                onSettingsDismissed = { showSettingsDialog = false },
                onTopAppBarActionClick = { showSettingsDialog = true },
                windowAdaptiveInfo = windowAdaptiveInfo,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NiaApp(
    appState: NiaAppState,
    snackbarHostState: SnackbarHostState,
    showSettingsDialog: Boolean,
    onSettingsDismissed: () -> Unit,
    onTopAppBarActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(),
) {
    val currentDestination = appState.currentDestination
    if (appState.currentTopLevelDestination != null) {
        NiaNavigationSuiteScaffold(
            navigationSuiteItems = {
                appState.topLevelDestinations.forEach { destination ->
                    val selected = currentDestination
                        .isTopLevelDestinationInHierarchy(destination)
                    item(
                        selected = selected,
                        onClick = { appState.navigateToTopLevelDestination(destination) },
                        icon = {
                            Icon(
                                imageVector = destination.unselectedIcon,
                                contentDescription = null,
                            )
                        },
                        selectedIcon = {
                            Icon(
                                imageVector = destination.selectedIcon,
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(destination.iconTextId)) },
                        modifier =
                        Modifier
                            .testTag("NiaNavItem"),
                    )
                }
            },
            windowAdaptiveInfo = windowAdaptiveInfo,
        ) {
            ContentScreen(modifier, snackbarHostState, appState, onTopAppBarActionClick)
        }
    } else {
        ContentScreen(modifier, snackbarHostState, appState, onTopAppBarActionClick)
    }

}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
private fun ContentScreen(
    modifier: Modifier,
    snackbarHostState: SnackbarHostState,
    appState: NiaAppState,
    onTopAppBarActionClick: () -> Unit
) {
    Scaffold(
        modifier = modifier.semantics {
            testTagsAsResourceId = true
        },
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
            .exclude(WindowInsets.navigationBars)
            .exclude(WindowInsets.ime),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)

        ) {
            // Show the top app bar on top level destinations.
            val destination = appState.currentTopLevelDestination
            val shouldShowTopAppBar = destination != null
            if (destination != null) {
                NiaTopAppBar(
                    titleRes = destination.titleTextId,
                    navigationIcon = NiaIcons.Search,
                    navigationIconContentDescription = stringResource(
                        id = settingsR.string.settings_toolbar_title,
                    ),
                    actionIcon = NiaIcons.Settings,
                    actionIconContentDescription = stringResource(
                        id = settingsR.string.settings_toolbar_title,
                    ),
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                    ),
                    onActionClick = { onTopAppBarActionClick() },
                    onNavigationClick = { appState.navigateToSearch() },
                )
            }

            Box(
                modifier = Modifier.consumeWindowInsets(
                    if (shouldShowTopAppBar) {
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
                    } else {
                        WindowInsets(0, 0, 0, 0)
                    },
                ),
            ) {
                NiaNavHost(
                    appState = appState,
                    onShowSnackbar = { message, action ->
                        snackbarHostState.showSnackbar(
                            message = message,
                            actionLabel = action,
                            duration = Short,
                        ) == ActionPerformed
                    },
                )
            }
        }
    }
}

private fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination) =
    this?.hierarchy?.any {
        it.route?.contains(destination.name, true) ?: false
    } ?: false
