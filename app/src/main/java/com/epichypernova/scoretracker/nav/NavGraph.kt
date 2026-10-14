package com.epichypernova.scoretracker.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.epichypernova.scoretracker.R
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.epichypernova.scoretracker.data.AppActions
import com.epichypernova.scoretracker.data.Repository
import com.epichypernova.scoretracker.data.model.GameType
import com.epichypernova.scoretracker.ui.components.AppTab
import com.epichypernova.scoretracker.ui.components.KeepScreenOn
import com.epichypernova.scoretracker.ui.screens.history.HistoryScreen
import com.epichypernova.scoretracker.ui.screens.menu.MenuScreen
import com.epichypernova.scoretracker.ui.screens.players.EditPlayerScreen
import com.epichypernova.scoretracker.ui.screens.players.PlayersScreen

@Composable
fun AppNavGraph(repo: Repository, navController: NavHostController = rememberNavController()) {
    val state by repo.state.collectAsState()
    val adGate = com.epichypernova.scoretracker.ui.components.rememberAdGate(repo, state)
    val tabLabels = mapOf(
        AppTab.JUEGOS to stringResource(R.string.tab_games),
        AppTab.HISTORIAL to stringResource(R.string.tab_history),
        AppTab.JUGADORES to stringResource(R.string.tab_players),
        AppTab.DADOS to stringResource(R.string.tab_dice),
        AppTab.AJUSTES to stringResource(R.string.settings),
    )

    // Any game that finishes sets pendingResult → show the winner screen.
    LaunchedEffect(state.pendingResult) {
        if (state.pendingResult != null) {
            navController.navigate(Routes.END_GAME) { launchSingleTop = true }
        }
    }

    fun goTab(tab: AppTab) {
        val route = when (tab) {
            AppTab.JUEGOS -> Routes.MENU
            AppTab.HISTORIAL -> Routes.HISTORY
            AppTab.JUGADORES -> Routes.PLAYERS
            AppTab.DADOS -> Routes.DICE
            AppTab.AJUSTES -> Routes.SETTINGS
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
                onOpenSpecific = { gt -> navController.navigate(Routes.forGame(gt)) },
                onStartConfig = { cfg ->
                    adGate {
                        repo.update { AppActions.startFromConfig(it, cfg) }
                        navController.navigate(Routes.GENERIC_TABLE)
                    }
                },
                onToggleFavorite = { gt -> repo.update { AppActions.toggleFavoriteGame(it, gt) } },
                onToggleSection = { key -> repo.update { AppActions.toggleSection(it, key) } },
                onSetGrid = { g -> repo.update { AppActions.setMenuGrid(it, g) } },
                onDeleteConfig = { id -> repo.update { AppActions.deleteConfig(it, id) } },
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(state = state, tabLabels = tabLabels, onSelectTab = ::goTab)
        }

        composable(Routes.PLAYERS) {
            PlayersScreen(
                repo = repo,
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

        composable(Routes.DICE) {
            com.epichypernova.scoretracker.ui.screens.dice.DiceScreen(
                repo = repo, state = state, tabLabels = tabLabels, onSelectTab = ::goTab,
            )
        }

        composable(Routes.SETTINGS) {
            com.epichypernova.scoretracker.ui.screens.settings.SettingsScreen(
                repo = repo, state = state, tabLabels = tabLabels, onSelectTab = ::goTab,
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
            KeepScreenOn(state.settings.keepScreenOn)
            com.epichypernova.scoretracker.ui.screens.generic.GenericTableScreen(
                repo = repo, state = state,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.END_GAME) {
            com.epichypernova.scoretracker.ui.screens.generic.EndGameScreen(
                state = state,
                onDone = {
                    repo.update { AppActions.clearResult(it) }
                    navController.popBackStack(Routes.MENU, false)
                },
                onRematch = {
                    val type = state.pendingResult?.gameType
                    repo.update { AppActions.rematch(it) }
                    navController.popBackStack(Routes.MENU, false)
                    type?.let { navController.navigate(Routes.forGame(it)) }
                },
            )
        }

        composable(Routes.TRUCO) {
            KeepScreenOn(state.settings.keepScreenOn)
            com.epichypernova.scoretracker.ui.screens.truco.TrucoScreen(
                repo = repo, state = state, onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.MAGIC) {
            KeepScreenOn(state.settings.keepScreenOn)
            com.epichypernova.scoretracker.ui.screens.magic.MagicScreen(
                repo = repo, state = state,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.YUGIOH) {
            KeepScreenOn(state.settings.keepScreenOn)
            com.epichypernova.scoretracker.ui.screens.yugioh.YuGiOhScreen(
                repo = repo, state = state,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.POKEMON) {
            KeepScreenOn(state.settings.keepScreenOn)
            com.epichypernova.scoretracker.ui.screens.pokemon.PokemonScreen(
                repo = repo, state = state,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.DIGIMON) {
            KeepScreenOn(state.settings.keepScreenOn)
            com.epichypernova.scoretracker.ui.screens.digimon.DigimonScreen(
                repo = repo, state = state,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.LORCANA) {
            KeepScreenOn(state.settings.keepScreenOn)
            com.epichypernova.scoretracker.ui.screens.lorcana.LorcanaScreen(
                repo = repo, state = state,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.ONEPIECE) {
            KeepScreenOn(state.settings.keepScreenOn)
            com.epichypernova.scoretracker.ui.screens.onepiece.OnePieceScreen(
                repo = repo, state = state,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.CHINCHON) {
            KeepScreenOn(state.settings.keepScreenOn)
            com.epichypernova.scoretracker.ui.screens.chinchon.ChinchonScreen(
                repo = repo, state = state,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.BURAKO) {
            KeepScreenOn(state.settings.keepScreenOn)
            com.epichypernova.scoretracker.ui.screens.burako.BurakoScreen(
                repo = repo, state = state,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.DARTS) {
            KeepScreenOn(state.settings.keepScreenOn)
            com.epichypernova.scoretracker.ui.screens.darts.DartsScreen(
                repo = repo, state = state,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.DND) {
            KeepScreenOn(state.settings.keepScreenOn)
            com.epichypernova.scoretracker.ui.screens.dnd.DndScreen(
                repo = repo, state = state,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.GENERALA) {
            KeepScreenOn(state.settings.keepScreenOn)
            com.epichypernova.scoretracker.ui.screens.generala.GeneralaScreen(
                repo = repo, state = state,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.BOWLING) {
            KeepScreenOn(state.settings.keepScreenOn)
            com.epichypernova.scoretracker.ui.screens.bowling.BowlingScreen(
                repo = repo, state = state,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.UNO) {
            KeepScreenOn(state.settings.keepScreenOn)
            com.epichypernova.scoretracker.ui.screens.uno.UnoScreen(
                repo = repo, state = state,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.WARHAMMER) {
            KeepScreenOn(state.settings.keepScreenOn)
            com.epichypernova.scoretracker.ui.screens.warhammer.WarhammerScreen(
                repo = repo, state = state,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.POKER) {
            KeepScreenOn(state.settings.keepScreenOn)
            com.epichypernova.scoretracker.ui.screens.poker.PokerScreen(
                repo = repo, state = state,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SWU) {
            KeepScreenOn(state.settings.keepScreenOn)
            com.epichypernova.scoretracker.ui.screens.swu.SwuScreen(
                repo = repo, state = state,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.ESCOBA) {
            KeepScreenOn(state.settings.keepScreenOn)
            com.epichypernova.scoretracker.ui.screens.escoba.EscobaScreen(
                repo = repo, state = state,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.MUS) {
            KeepScreenOn(state.settings.keepScreenOn)
            com.epichypernova.scoretracker.ui.screens.mus.MusScreen(
                repo = repo, state = state,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
