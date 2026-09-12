package com.epichypernova.scoretracker.ui.screens.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.data.Derivations
import com.epichypernova.scoretracker.data.model.AppState
import com.epichypernova.scoretracker.data.model.GameType
import com.epichypernova.scoretracker.data.model.SavedConfig
import com.epichypernova.scoretracker.ui.GameCatalog
import com.epichypernova.scoretracker.ui.components.AppTab
import com.epichypernova.scoretracker.ui.components.CircleIconButton
import com.epichypernova.scoretracker.ui.components.GameRow
import com.epichypernova.scoretracker.ui.components.SectionLabel
import com.epichypernova.scoretracker.ui.components.TabScaffold
import com.epichypernova.scoretracker.ui.components.cardSurface
import com.epichypernova.scoretracker.ui.components.menuBackdrop
import com.epichypernova.scoretracker.ui.theme.Cinzel
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

@Composable
fun MenuScreen(
    state: AppState,
    tabLabels: Map<AppTab, String>,
    onSelectTab: (AppTab) -> Unit,
    onOpenCurrent: () -> Unit,
    onStartGeneric: (GameType) -> Unit,
    onOpenSpecific: (GameType) -> Unit,
    onStartConfig: (SavedConfig) -> Unit,
    onToggleFavorite: (GameType) -> Unit,
    onDeleteConfig: (String) -> Unit,
) {
    var grid by remember { mutableStateOf(false) }
    var configToDelete by remember { mutableStateOf<SavedConfig?>(null) }

    // Launch a game by type, dispatching generic vs specific screens.
    val launch: (GameType) -> Unit = { gt -> if (gt.isGeneric) onStartGeneric(gt) else onOpenSpecific(gt) }
    val favoriteEntries = (GameCatalog.generics + GameCatalog.specifics)
        .filter { it.available && it.gameType in state.favoriteGames }
    TabScaffold(
        selected = AppTab.JUEGOS,
        tabLabels = tabLabels,
        onSelectTab = onSelectTab,
    ) { padding ->
        Box(Modifier.fillMaxSize().menuBackdrop()) {
            if (grid) {
                MenuGridContent(
                    state = state,
                    padding = padding,
                    onToggleView = { grid = false },
                    onOpenCurrent = onOpenCurrent,
                    onStartGeneric = onStartGeneric,
                    onOpenSpecific = onOpenSpecific,
                )
                return@Box
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp, end = 20.dp,
                    top = padding.calculateTopPadding() + 14.dp,
                    bottom = padding.calculateBottomPadding() + 20.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { MenuHeader(onToggleView = { grid = true }) }

                if (favoriteEntries.isNotEmpty()) {
                    item { SectionLabel(stringResource(R.string.menu_section_favorites), modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)) }
                    items(favoriteEntries, key = { "fav_" + it.gameType.name }) { e ->
                        GameRow(
                            e.glyph, e.tint, stringResource(e.titleRes), stringResource(e.subtitleRes),
                            onClick = { launch(e.gameType) },
                            favorite = true,
                            onToggleFavorite = { onToggleFavorite(e.gameType) },
                        )
                    }
                }

                state.currentGame?.let { game ->
                    item {
                        EnCursoCard(state, onOpenCurrent)
                    }
                }

                item { SectionLabel(stringResource(R.string.menu_section_generics), modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)) }
                items(GameCatalog.generics) { e ->
                    GameRow(
                        e.glyph, e.tint, stringResource(e.titleRes), stringResource(e.subtitleRes),
                        onClick = { onStartGeneric(e.gameType) },
                        favorite = e.gameType in state.favoriteGames,
                        onToggleFavorite = { onToggleFavorite(e.gameType) },
                    )
                }

                if (state.savedConfigs.isNotEmpty()) {
                    item {
                        SectionLabel(
                            stringResource(R.string.menu_section_saved),
                            modifier = Modifier.padding(top = 12.dp, bottom = 2.dp),
                        )
                    }
                    item {
                        Row(
                            Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            state.savedConfigs.forEach { cfg ->
                                SavedConfigCard(cfg, onClick = { onStartConfig(cfg) }, onDelete = { configToDelete = cfg })
                            }
                        }
                    }
                }

                item { SectionLabel(stringResource(R.string.menu_section_specific), modifier = Modifier.padding(top = 12.dp, bottom = 2.dp)) }
                items(GameCatalog.specifics) { e ->
                    GameRow(
                        e.glyph, e.tint, stringResource(e.titleRes),
                        if (e.available) stringResource(e.subtitleRes) else stringResource(R.string.coming_soon),
                        onClick = { if (e.available) onOpenSpecific(e.gameType) },
                        enabled = e.available,
                        favorite = e.gameType in state.favoriteGames,
                        onToggleFavorite = if (e.available) ({ onToggleFavorite(e.gameType) }) else null,
                    )
                }
            }
        }
    }

    configToDelete?.let { cfg ->
        DeleteConfigDialog(
            name = cfg.name,
            onConfirm = { onDeleteConfig(cfg.id); configToDelete = null },
            onDismiss = { configToDelete = null },
        )
    }
}

@Composable
private fun MenuHeader(onToggleView: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "Epic Hypernova",
            modifier = Modifier.width(254.dp).aspectRatio(1993f / 789f),
        )
        CircleIconButton("▦", onToggleView)
    }
}

@Composable
private fun EnCursoCard(state: AppState, onClick: () -> Unit) {
    val game = state.currentGame ?: return
    val best = Derivations.bestTotal(game)
    val leaderId = Derivations.leaders(game).firstOrNull()
    val leaderName = state.users.firstOrNull { it.id == leaderId }?.name ?: "—"
    val mano = Derivations.currentRoundNumber(game)

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF21406F), Color(0xFF16294F))))
            .border(1.dp, Color(0x732FD3F0), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                SectionLabel(stringResource(R.string.en_curso), color = Palette.Cyan)
                Text(
                    (game.name ?: stringResource(R.string.game_manos_title)),
                    color = Palette.TextPrimary,
                    style = TextStyle(fontFamily = Cinzel, fontWeight = FontWeight.Bold, fontSize = 23.sp, letterSpacing = 0.7.sp),
                )
                Row {
                    Text(
                        stringResource(R.string.hand_n, mano) + " · ",
                        color = Color(0xFFA9BCE0),
                        style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 13.sp),
                    )
                    Text(
                        stringResource(R.string.leading_with, leaderName) + " ",
                        color = Color(0xFFA9BCE0),
                        style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 13.sp),
                    )
                    Text(
                        "$best",
                        color = Palette.Mint,
                        style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                    )
                }
            }
            Box(
                Modifier.size(48.dp).clip(CircleShape).background(Palette.Cyan),
                contentAlignment = Alignment.Center,
            ) {
                Text("▸", color = Palette.OnAccent, style = TextStyle(fontSize = 20.sp))
            }
        }
    }
}

@Composable
private fun SavedConfigCard(cfg: SavedConfig, onClick: () -> Unit, onDelete: () -> Unit) {
    Box(
        Modifier
            .width(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0x592FD3F0), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Column(Modifier.padding(end = 22.dp)) {
            Text(
                cfg.name,
                color = Palette.TextPrimary,
                style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
            )
            Text(
                stringResource(
                    R.string.saved_config_detail,
                    cfg.playerIds.size,
                    cfg.rules.targetScore,
                    stringResource(if (cfg.rules.lowWins) R.string.wins_menor_word else R.string.wins_mayor_word),
                ),
                color = Palette.TextTertiary,
                style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.sp),
            )
        }
        Box(
            Modifier.align(Alignment.TopEnd).size(26.dp).clip(CircleShape).clickable { onDelete() },
            contentAlignment = Alignment.Center,
        ) {
            Text("✕", color = Palette.TextTertiary, style = TextStyle(fontSize = 13.sp))
        }
    }
}

@Composable
private fun DeleteConfigDialog(name: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.delete), color = Palette.Magenta)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = Palette.TextSecondary)
            }
        },
        title = { Text(stringResource(R.string.delete_config_title, name), color = Palette.TextPrimary) },
        containerColor = Palette.SheetSurface,
    )
}
