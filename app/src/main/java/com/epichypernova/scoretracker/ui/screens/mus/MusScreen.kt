@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.epichypernova.scoretracker.ui.screens.mus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.data.AppActions
import com.epichypernova.scoretracker.data.Repository
import com.epichypernova.scoretracker.data.model.AppState
import com.epichypernova.scoretracker.data.model.MusTeam
import com.epichypernova.scoretracker.ui.components.GameIcon
import com.epichypernova.scoretracker.ui.components.GamePill
import com.epichypernova.scoretracker.ui.components.gameInsets
import com.epichypernova.scoretracker.ui.components.NumberPadSheet
import com.epichypernova.scoretracker.ui.components.PlayerNameRow
import com.epichypernova.scoretracker.ui.components.Segmented
import com.epichypernova.scoretracker.ui.components.SheetLabel
import com.epichypernova.scoretracker.ui.components.SheetPrimaryButton
import com.epichypernova.scoretracker.ui.components.SheetSecondaryButton
import com.epichypernova.scoretracker.ui.components.gamePaneGradient
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

private val ACCENT get() = Palette.GameMus

@Composable
fun MusScreen(repo: Repository, state: AppState, onBack: () -> Unit) {
    LaunchedEffect(Unit) { repo.update { AppActions.musEnsureExists(it) } }
    val game = state.musGame ?: return
    if (game.teams.size < 2) return
    var showConfig by remember { mutableStateOf(false) }
    var addFor by remember { mutableIntStateOf(-1) }

    Column(Modifier.fillMaxSize().background(Palette.AppBgDeep).gameInsets()) {
        Row(
            Modifier.fillMaxWidth().border(1.dp, ACCENT.copy(alpha = 0.28f), RoundedCornerShape(0.dp)).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GameIcon(R.drawable.ic_cow, ACCENT, 16)
            Text("${game.juegosPerVaca} ${stringResource(R.string.mus_juegos).uppercase()} · ${game.vacasToWin} ${stringResource(R.string.mus_vacas).uppercase()}", color = Palette.TextMuted, modifier = Modifier.weight(1f), maxLines = 1, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 11.sp, letterSpacing = 1.2.sp))
            GamePill(stringResource(R.string.mus_undo)) { repo.update { AppActions.musUndo(it) } }
            GamePill("⚙") { showConfig = true }
        }
        Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            game.teams.forEachIndexed { i, t ->
                TeamPane(t, i, game.juegosPerVaca, game.vacasToWin, repo, onAdd = { addFor = i }, modifier = Modifier.weight(1f).fillMaxHeight())
            }
        }
    }

    if (addFor >= 0) {
        val idx = addFor
        NumberPadSheet(
            title = stringResource(R.string.mus_add),
            subtitle = "${game.teams[idx].name} · ${game.teams[idx].piedras}",
            accent = ACCENT,
            confirmLabel = stringResource(R.string.mus_add),
            onConfirm = { n -> repo.update { AppActions.musAdd(it, idx, n) } },
            onClose = { addFor = -1 },
            quickAdds = listOf(1, 2, 3, 5),
            max = 40,
        )
    }
    if (showConfig) {
        ConfigSheet(
            juegos = game.juegosPerVaca,
            vacas = game.vacasToWin,
            onApply = { j, v -> repo.update { AppActions.musSetRules(it, j, v) } },
            onReset = { repo.update { AppActions.musReset(it) } },
            onFinish = { repo.update { AppActions.musFinish(it) } },
            onClose = { showConfig = false },
        )
    }
}

@Composable
private fun TeamPane(t: MusTeam, index: Int, juegosPerVaca: Int, vacasToWin: Int, repo: Repository, onAdd: () -> Unit, modifier: Modifier = Modifier) {
    val tint = Color(t.color)
    val grad = gamePaneGradient(tint)
    val bg = Modifier.drawBehind {
        drawRect(brush = ShaderBrush(RadialGradientShader(Offset(size.width * 0.5f, size.height), size.height * 0.95f, grad, listOf(0f, 0.6f, 1f))))
    }
    Box(modifier.then(bg)) {
        Column(Modifier.fillMaxSize().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            PlayerNameRow(t.name, tint)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${t.piedras}", color = Color.White, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.ExtraBold, fontSize = 54.sp, fontFeatureSettings = "tnum"))
                Text("/40", color = Palette.TextTertiary, modifier = Modifier.padding(bottom = 10.dp), style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 13.sp))
            }
            // amarrakos: 8 groups of 5 stones
            FlowRow(Modifier.padding(top = 4.dp).width(140.dp), horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(8) { g ->
                    val filled = (t.piedras - g * 5).coerceIn(0, 5)
                    Amarrako(filled, tint)
                }
            }
            Text("${t.piedras / 5} amarrakos", color = Palette.TextTertiary, modifier = Modifier.padding(top = 4.dp), style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 11.sp))
            // juegos + vacas
            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.mus_juegos).uppercase(), color = Palette.TextMuted, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 9.sp, letterSpacing = 1.sp))
                repeat(juegosPerVaca) { j ->
                    Box(Modifier.size(10.dp).clip(CircleShape).background(if (j < t.juegos) Palette.Mint else Color.Transparent).border(1.dp, Palette.Mint.copy(alpha = 0.7f), CircleShape))
                }
            }
            // vacas: one cow per vaca needed to win, filled as they are earned
            Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.mus_vacas).uppercase(), color = Palette.Mint, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.4.sp))
                repeat(vacasToWin) { v -> GameIcon(R.drawable.ic_cow, if (v < t.vacas) Palette.Mint else Palette.Mint.copy(alpha = 0.3f), 16) }
            }
            // actions
            Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionBtn("+1", Color(0x1FFFFFFF)) { repo.update { AppActions.musAdd(it, index, 1) } }
                ActionBtn("+2", Color(0x1FFFFFFF)) { repo.update { AppActions.musAdd(it, index, 2) } }
                ActionBtn("+N", Color(0x1FFFFFFF)) { onAdd() }
            }
            Box(Modifier.padding(top = 8.dp).height(40.dp).clip(RoundedCornerShape(999.dp)).background(ACCENT).clickable { repo.update { AppActions.musOrdago(it, index) } }.padding(horizontal = 22.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.mus_ordago), color = Palette.OnAccent, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 13.sp))
            }
        }
    }
}

/** Four stones in a row plus a fifth laid across = one amarrako (5 piedras). */
@Composable
private fun Amarrako(filled: Int, tint: Color) {
    Box(Modifier.size(width = 30.dp, height = 14.dp)) {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            repeat(4) { i ->
                Box(Modifier.size(6.dp).clip(CircleShape).background(if (i < filled) tint else Color(0x33FFFFFF)))
            }
        }
        if (filled >= 5) Box(Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(999.dp)).background(tint).align(Alignment.TopCenter))
    }
}

@Composable
private fun ActionBtn(text: String, bg: Color, onClick: () -> Unit) {
    Box(Modifier.height(40.dp).clip(RoundedCornerShape(999.dp)).background(bg).border(1.dp, Color(0x2EFFFFFF), RoundedCornerShape(999.dp)).clickable { onClick() }.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
        Text(text, color = Palette.TextPrimary, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 13.sp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigSheet(juegos: Int, vacas: Int, onApply: (Int, Int) -> Unit, onReset: () -> Unit, onFinish: () -> Unit, onClose: () -> Unit) {
    var j by remember { mutableIntStateOf(juegos) }
    var v by remember { mutableIntStateOf(vacas) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, containerColor = Palette.SheetSurface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SheetLabel(stringResource(R.string.mus_juegos_per_vaca))
            Segmented(options = listOf("1", "2", "3", "5"), selectedIndex = listOf(1, 2, 3, 5).indexOf(j).coerceAtLeast(0), onSelect = { j = listOf(1, 2, 3, 5)[it] })
            SheetLabel(stringResource(R.string.mus_vacas_to_win))
            Segmented(options = listOf("1", "2", "3"), selectedIndex = (v - 1).coerceIn(0, 2), onSelect = { v = it + 1 })
            SheetPrimaryButton(stringResource(R.string.done), ACCENT) { onApply(j, v); onClose() }
            SheetSecondaryButton(stringResource(R.string.score_reset)) { onReset(); onClose() }
            SheetSecondaryButton(stringResource(R.string.finish_game)) { onFinish(); onClose() }
        }
    }
}
