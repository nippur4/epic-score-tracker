package com.epichypernova.scoretracker.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.epichypernova.scoretracker.R
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.epichypernova.scoretracker.data.Repository
import com.epichypernova.scoretracker.data.model.GameType
import com.epichypernova.scoretracker.ui.components.AppTab
import com.epichypernova.scoretracker.ui.screens.history.HistoryScreen
import com.epichypernova.scoretracker.ui.screens.menu.MenuScreen
import com.epichypernova.scoretracker.ui.screens.players.EditPlayerScreen
import com.epichypernova.scoretracker.ui.screens.players.PlayersScreen

@Composable
fun AppNavGraph(repo: Repository, navController: NavHostController = rememberNavController()) {
    val state by repo.state.collectAsState()
    val tabLabels = mapOf(
        AppTab.JUEGOS to stringResource(R.string.tab_games),
        AppTab.HISTORIAL to stringResource(R.string.tab_history),
        AppTab.JUGADORES to stringResource(R.string.tab_players),
    )

    fun goTab(tab: AppTab) {
        val route = when (tab) {
            AppTab.JUEGOS -> Routes.MENU
            AppTab.HISTORIAL -> Routes.HISTORY
            AppTab.JUGADORES -> Routes.PLAYERS
        }
        navController.navigate(route) {
            popUpTo(Routes.MENU) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(navController = navController, startDestination = Routes.MENU) {
        composable(Routes.MENU) {
            MenuScreen(
                state = state,
                tabLabels = tabLabels,
                onSelectTab = ::goTab,
                onOpenCurrent = { navController.navigate(Routes.GENERIC_TABLE) },
                onStartGeneric = { gt -> navController.navigate(Routes.genericSetup(gt.name)) },
                onOpenSpecific = { gt ->
                    when (gt) {
                        GameType.TRUCO -> navController.navigate(Routes.TRUCO)
                        GameType.MAGIC -> navController.navigate(Routes.MAGIC_1V1)
                        else -> Unit
                    }
                },
                onEditConfigs = { },
                onStartConfig = { navController.navigate(Routes.GENERIC_TABLE) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(state = state, tabLabels = tabLabels, onSelectTab = ::goTab)
        }

        composable(Routes.PLAYERS) {
            PlayersScreen(
                state = state,
                tabLabels = tabLabels,
                onSelectTab = ::goTab,
                onAddPlayer = { navController.navigate(Routes.editPlayer("new")) },
                onEditPlayer = { id -> navController.navigate(Routes.editPlayer(id)) },
            )
        }

        composable(Routes.EDIT_PLAYER) { entry ->
            val userId = entry.arguments?.getString("userId") ?: "new"
            EditPlayerScreen(
                repo = repo,
                state = state,
                userId = userId,
                onClose = { navController.popBackStack() },
            )
        }

        composable(Routes.SETTINGS) {
            com.epichypernova.scoretracker.ui.screens.settings.SettingsScreen(
                repo = repo, state = state, onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.GENERIC_SETUP) { entry ->
            val gt = runCatching { GameType.valueOf(entry.arguments?.getString("gameType") ?: "") }
                .getOrDefault(GameType.MANOS_Y_PUNTOS)
            com.epichypernova.scoretracker.ui.screens.generic.GenericSetupScreen(
                repo = repo, state = state, gameType = gt,
                onBack = { navController.popBackStack() },
                onStarted = {
                    navController.navigate(Routes.GENERIC_TABLE) {
                        popUpTo(Routes.MENU)
                    }
                },
            )
        }

        composable(Routes.GENERIC_TABLE) {
            com.epichypernova.scoretracker.ui.screens.generic.GenericTableScreen(
                repo = repo, state = state,
                onBack = { navController.popBackStack() },
                onFinished = { navController.popBackStack(Routes.MENU, false) },
            )
        }

        composable(Routes.TRUCO) {
            com.epichypernova.scoretracker.ui.screens.truco.TrucoScreen(
                repo = repo, state = state, onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.MAGIC_1V1) {
            com.epichypernova.scoretracker.ui.screens.magic.MagicScreen(
                repo = repo, state = state, commander = false,
                onBack = { navController.popBackStack() },
                onSwitchMode = { navController.navigate(Routes.MAGIC_COMMANDER) { popUpTo(Routes.MENU) } },
            )
        }

        composable(Routes.MAGIC_COMMANDER) {
            com.epichypernova.scoretracker.ui.screens.magic.MagicScreen(
                repo = repo, state = state, commander = true,
                onBack = { navController.popBackStack() },
                onSwitchMode = { navController.navigate(Routes.MAGIC_1V1) { popUpTo(Routes.MENU) } },
            )
        }
    }
}
