package com.videocompress.app

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.videocompress.core.common.VideoTool
import com.videocompress.feature.editor.EditorRoute
import com.videocompress.feature.history.HistoryRoute
import com.videocompress.feature.home.HomeScreen
import com.videocompress.feature.settings.SettingsRoute

object Destinations {
    const val HOME = "home"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val EDITOR = "editor/{tool}"
    fun editor(tool: VideoTool) = "editor/${tool.name}"
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    incomingUris: List<Uri>,
    onIncomingConsumed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(incomingUris) {
        if (incomingUris.isNotEmpty()) {
            navController.navigate(Destinations.editor(VideoTool.COMPRESS))
        }
    }
    NavHost(
        navController = navController,
        startDestination = Destinations.HOME,
        modifier = modifier,
    ) {
        composable(Destinations.HOME) {
            HomeScreen(onOpenTool = { tool -> navController.navigate(Destinations.editor(tool)) })
        }
        composable(Destinations.HISTORY) {
            HistoryRoute()
        }
        composable(Destinations.SETTINGS) {
            SettingsRoute()
        }
        composable(
            route = Destinations.EDITOR,
            arguments = listOf(navArgument("tool") { type = NavType.StringType }),
        ) { entry ->
            val tool = runCatching {
                VideoTool.valueOf(entry.arguments?.getString("tool") ?: VideoTool.COMPRESS.name)
            }.getOrDefault(VideoTool.COMPRESS)
            val extras = if (incomingUris.isNotEmpty()) incomingUris else emptyList()
            EditorRoute(
                tool = tool,
                incomingUris = extras,
                onBack = {
                    onIncomingConsumed()
                    navController.popBackStack()
                },
            )
            if (extras.isNotEmpty()) {
                LaunchedEffect(extras) { onIncomingConsumed() }
            }
        }
    }
}
