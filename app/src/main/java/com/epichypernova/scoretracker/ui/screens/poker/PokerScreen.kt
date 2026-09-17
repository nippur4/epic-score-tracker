package com.epichypernova.scoretracker.ui.screens.poker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.data.AppActions
import com.epichypernova.scoretracker.data.Repository
import com.epichypernova.scoretracker.data.model.AppState
import com.epichypernova.scoretracker.data.model.GameType
import com.epichypernova.scoretracker.ui.components.GameIcon
import com.epichypernova.scoretracker.ui.components.GamePill
import com.epichypernova.scoretracker.ui.components.gameInsets
import com.epichypernova.scoretracker.ui.screens.setup.PlayerSetupScreen
import com.epichypernova.scoretracker.ui.components.LabeledCounter
import com.epichypernova.scoretracker.ui.components.PresetChips
import com.epichypernova.scoretracker.ui.components.SheetLabel
import com.epichypernova.scoretracker.ui.components.SheetPrimaryButton
import com.epichypernova.scoretracker.ui.components.SheetSecondaryButton
import com.epichypernova.scoretracker.ui.components.rememberSoundEffect
import com.epichypernova.scoretracker.ui.theme.Cinzel
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk
import kotlinx.coroutines.delay

private val ACCENT get() = Palette.GamePoker

@Composable
fun PokerScreen(repo: Repository, state: AppState, onBack: () -> Unit) {
    val game = state.pokerGame
    // Finished games go back through setup, except while the winner screen is on its way.
    if (game == null || (game.finished && state.pendingResult == null)) {
        var minutes by remember { mutableIntStateOf(15) }
        PlayerSetupScreen(
            repo = repo, state = state, gameType = GameType.POKER,
            minPlayers = 2, maxPlayers = 30, defaultCount = 8, onBack = onBack, countOnly = true,
            options = {
                SheetLabel(stringResource(R.string.poker_minutes))
                PresetChips(listOf(10, 15, 20, 30), minutes, ACCENT) { minutes = it }
            },
            onStart = { players -> repo.update { AppActions.pokerStart(it, minutes, players.size) } },
        )
        return
    }
    val level = game.levels[game.level.coerceIn(0, game.levels.size - 1)]
    val next = game.levels.getOrNull(game.level + 1)
    var showConfig by remember { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val levelSound = rememberSoundEffect(R.raw.victory)

    // Tick while running; when the clock hits zero jump to the next level and restart it.
    LaunchedEffect(game.running, game.level) {
        while (game.running) {
            now = System.currentTimeMillis()
            if (AppActions.pokerRemaining(game, now) <= 0L) {
                levelSound()
                repo.update { s -> AppActions.pokerSetLevel(s, game.level + 1, System.currentTimeMillis()) }
                break
            }
            delay(250)
        }
    }
    val remaining = AppActions.pokerRemaining(game, now)
    val mm = remaining / 60_000; val ss = (remaining / 1000) % 60
    val urgent = game.running && remaining < 60_000

    Column(Modifier.fillMaxSize().background(Palette.AppBgDeep).gameInsets()) {
        Row(
            Modifier.fillMaxWidth().border(1.dp, ACCENT.copy(alpha = 0.28f), RoundedCornerShape(0.dp)).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GameIcon(R.drawable.ic_clock, ACCENT, 16)
            Text("${stringResource(R.string.poker_level)} ${game.level + 1}/${game.levels.size}", color = Palette.TextMuted, modifier = Modifier.weight(1f), style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 11.sp, letterSpacing = 1.4.sp))
            GamePill(stringResource(R.string.score_reset)) { repo.update { AppActions.pokerReset(it) } }
            GamePill("⚙") { showConfig = true }
        }

        Column(Modifier.weight(1f).fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                GameIcon(R.drawable.ic_chip, ACCENT, 16)
                Text(stringResource(R.string.poker_blinds), color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 2.sp))
                GameIcon(R.drawable.ic_chip, ACCENT, 16)
            }
            Text("${level.small} / ${level.big}", color = ACCENT, style = TextStyle(fontFamily = Cinzel, fontWeight = FontWeight.Black, fontSize = 40.sp))
            Text(
                String.format("%02d:%02d", mm, ss),
                color = if (urgent) Color(0xFFEB5757) else Color.White,
                modifier = Modifier.padding(top = 10.dp).clickable { repo.update { AppActions.pokerToggle(it, System.currentTimeMillis()) } },
                style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.ExtraBold, fontSize = 82.sp, fontFeatureSettings = "tnum"),
            )
            if (next != null) {
                Text("${stringResource(R.string.poker_next)}: ${next.small} / ${next.big}", color = Palette.TextTertiary, modifier = Modifier.padding(top = 6.dp), style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 13.sp))
            }
            Row(Modifier.padding(top = 26.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                RoundBtn("⏮") { repo.update { AppActions.pokerSetLevel(it, game.level - 1, System.currentTimeMillis()) } }
                Box(
                    Modifier.height(56.dp).clip(RoundedCornerShape(999.dp)).background(if (game.running) Color(0x22FFFFFF) else ACCENT)
                        .then(if (game.running) Modifier.border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)) else Modifier)
                        .clickable { repo.update { AppActions.pokerToggle(it, System.currentTimeMillis()) } }.padding(horizontal = 30.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(if (game.running) R.string.poker_pause else R.string.poker_start), color = if (game.running) Palette.TextPrimary else Palette.OnAccent, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 16.sp))
                }
                RoundBtn("⏭") { repo.update { AppActions.pokerSetLevel(it, game.level + 1, System.currentTimeMillis()) } }
            }
            Box(Modifier.padding(top = 26.dp)) {
                LabeledCounter(stringResource(R.string.poker_players_left), game.playersLeft, Palette.Cyan, big = true, icon = R.drawable.ic_chip,
                    onDec = { repo.update { AppActions.pokerPlayersLeft(it, -1) } }, onInc = { repo.update { AppActions.pokerPlayersLeft(it, +1) } })
            }
        }
    }

    if (showConfig) {
        ConfigSheet(
            minutes = game.levelMinutes,
            onApply = { m -> repo.update { AppActions.pokerSetMinutes(it, m, System.currentTimeMillis()) } },
            onNew = { repo.update { AppActions.pokerNew(it) } },
            onFinish = { repo.update { AppActions.pokerFinish(it) } },
            onClose = { showConfig = false },
        )
    }
}

@Composable
private fun RoundBtn(symbol: String, onClick: () -> Unit) {
    Box(Modifier.size(50.dp).clip(CircleShape).border(1.dp, Color(0x2EFFFFFF), CircleShape).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Text(symbol, color = Color.White, style = TextStyle(fontSize = 20.sp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigSheet(minutes: Int, onApply: (Int) -> Unit, onNew: () -> Unit, onFinish: () -> Unit, onClose: () -> Unit) {
    var m by remember { mutableIntStateOf(minutes) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, containerColor = Palette.SheetSurface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SheetLabel(stringResource(R.string.poker_minutes))
            PresetChips(listOf(10, 15, 20, 30), m, ACCENT) { m = it }
            SheetPrimaryButton(stringResource(R.string.done), ACCENT) { onApply(m); onClose() }
            SheetSecondaryButton(stringResource(R.string.setup_new_game)) { onNew(); onClose() }
            SheetSecondaryButton(stringResource(R.string.finish_game)) { onFinish(); onClose() }
        }
    }
}
