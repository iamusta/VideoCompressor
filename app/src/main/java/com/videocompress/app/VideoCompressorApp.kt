package com.videocompress.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.videocompress.core.common.ThemeMode
import com.videocompress.core.resources.R
import com.videocompress.core.ui.locale.LocaleHelper
import com.videocompress.core.ui.theme.VideoCompressorTheme

@Composable
fun VideoCompressorApp(
    viewModel: MainViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val incoming by viewModel.incomingUris.collectAsStateWithLifecycle()
    val dark = when (settings.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    LaunchedEffect(settings.languageCode) {
        LocaleHelper.applyLanguage(settings.languageCode)
    }
    VideoCompressorTheme(darkTheme = dark, contentScale = settings.contentScale) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            val navController = rememberNavController()
            val backStack by navController.currentBackStackEntryAsState()
            val destination = backStack?.destination
            val showBar = destination?.hierarchy?.any {
                it.route == Destinations.HOME || it.route == Destinations.HISTORY || it.route == Destinations.SETTINGS
            } == true
            Scaffold(
                contentWindowInsets = if (showBar) {
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                } else {
                    WindowInsets(0, 0, 0, 0)
                },
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    if (showBar) {
                        NavigationBar {
                            NavigationBarItem(
                                selected = destination?.hierarchy?.any { it.route == Destinations.HOME } == true,
                                onClick = { navController.navigate(Destinations.HOME) { launchSingleTop = true; restoreState = true; popUpTo(Destinations.HOME) { saveState = true } } },
                                icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
                                label = { Text(stringResource(R.string.nav_home)) },
                            )
                            NavigationBarItem(
                                selected = destination?.hierarchy?.any { it.route == Destinations.HISTORY } == true,
                                onClick = { navController.navigate(Destinations.HISTORY) { launchSingleTop = true; restoreState = true; popUpTo(Destinations.HOME) { saveState = true } } },
                                icon = { Icon(Icons.Outlined.History, contentDescription = null) },
                                label = { Text(stringResource(R.string.nav_history)) },
                            )
                            NavigationBarItem(
                                selected = destination?.hierarchy?.any { it.route == Destinations.SETTINGS } == true,
                                onClick = { navController.navigate(Destinations.SETTINGS) { launchSingleTop = true; restoreState = true; popUpTo(Destinations.HOME) { saveState = true } } },
                                icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                                label = { Text(stringResource(R.string.nav_settings)) },
                            )
                        }
                    }
                },
            ) { padding ->
                AppNavHost(
                    navController = navController,
                    incomingUris = incoming,
                    onIncomingConsumed = viewModel::consumeIncoming,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}
