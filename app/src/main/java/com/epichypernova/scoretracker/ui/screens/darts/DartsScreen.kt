package com.epichypernova.scoretracker.ui.screens.darts

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
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
import com.epichypernova.scoretracker.data.model.DartsPlayer
import com.epichypernova.scoretracker.ui.components.NumberPadSheet
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

private fun paneGradient(base: Color): List<Color> {
    val deep = Color(0xFF0E1B33)
    fun mix(t: Float) = Color(base.red * (1 - t) + deep.red * t, base.green * (1 - t) + deep.green * t, base.blue * (1 - t) + deep.blue * t, 1f)
    return listOf(mix(0.42f), mix(0.72f), deep)
}

@Composable
fun DartsScreen(repo: Repository, state: AppState, onBack: () -> Unit) {
    LaunchedEffect(Unit) { repo.update { AppActions.dartsEnsureExists(it) } }
    val game = state.dartsGame ?: return
    val players = game.players
    if (players.size < 2) return

    var showConfig by remember { mutableStateOf(false) }
    var throwForIndex by remember { mutableIntStateOf(-1) }

    Box(Modifier.fillMaxSize().background(Palette.AppBgDeep)) {
        Column(Modifier.fillMaxSize()) {
            DartPane(players[0], game.startScore, rotated = true, onThrow = { throwForIndex = 0 }, modifier = Modifier.weight(1f).fillMaxWidth())
            CentralBar(onReset = { repo.update { AppActions.dartsReset(it) } }, onConfig = { showConfig = true })
            DartPane(players[1], game.startScore, rotated = false, onThrow = { throwForIndex = 1 }, modifier = Modifier.weight(1f).fillMaxWidth())
        }
    }

    if (throwForIndex >= 0) {
        val idx = throwForIndex
        NumberPadSheet(
            title = stringResource(R.string.darts_throw_title),
            subtitle = "${players.getOrNull(idx)?.remaining ?: 0} → ?",
            accent = Palette.GameDarts,
            confirmLabel = stringResource(R.string.darts_throw),
            onConfirm = { amount -> repo.update { AppActions.dartsThrow(it, idx, amount) } },
            onClose = { throwForIndex = -1 },
            quickAdds = listOf(100, 60, 40, 20),
            max = 180,
        )
    }
    if (showConfig) {
        ConfigSheet(
            start = game.startScore,
            onApply = { st -> repo.update { AppActions.dartsSetStart(it, st) } },
            onFinish = { repo.update { AppActions.dartsFinish(it) } },
            onClose = { showConfig = false },
        )
    }
}

@Composable
private fun DartPane(player: DartsPlayer, start: Int, rotated: Boolean, onThrow: () -> Unit, modifier: Modifier = Modifier) {
    val grad = paneGradient(Color(player.color))
    val bg = Modifier.drawBehind {
        drawRect(brush = ShaderBrush(RadialGradientShader(Offset(size.width * 0.5f, size.height), size.height * 0.95f, grad, listOf(0f, 0.6f, 1f))))
    }
    Box(modifier.then(bg)) {
        Column(Modifier.fillMaxSize().rotate(if (rotated) 180f else 0f).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(12.dp).clip(CircleShape).background(Color(player.color)).border(1.dp, Color(0x33FFFFFF), CircleShape))
                Text(player.name.uppercase(), color = Color(player.color), style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.9.sp))
            }
            Text("${player.remaining}", color = Color.White, modifier = Modifier.padding(top = 2.dp), style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.ExtraBold, fontSize = 66.sp, fontFeatureSettings = "tnum"))
            Box(
                Modifier.padding(top = 6.dp).height(46.dp).clip(RoundedCornerShape(999.dp)).background(Palette.GameDarts).clickable { onThrow() }.padding(horizontal = 28.dp),
                contentAlignment = Alignment.Center,
            ) { Text(stringResource(R.string.darts_throw), color = Color.White, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 15.sp)) }
        }
    }
}

@Composable
private fun CentralBar(onReset: () -> Unit, onConfig: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Palette.AppBgDeep).border(1.dp, Color(0x47EB5757), RoundedCornerShape(0.dp)).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.weight(1f))
        Pill(stringResource(R.string.score_reset), onReset)
        Pill("⚙", onConfig)
    }
}

@Composable
private fun Pill(text: String, onClick: () -> Unit) {
    Box(Modifier.height(36.dp).clip(RoundedCornerShape(999.dp)).border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)).clickable { onClick() }.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
        Text(text, color = Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 12.sp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigSheet(start: Int, onApply: (Int) -> Unit, onFinish: () -> Unit, onClose: () -> Unit) {
    var st by remember { mutableIntStateOf(start) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val presets = listOf(501, 301, 701)
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, containerColor = Palette.SheetSurface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(stringResource(R.string.darts_start), color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.5.sp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                presets.forEach { p ->
                    val active = p == st
                    Box(Modifier.clip(RoundedCornerShape(999.dp)).background(if (active) Palette.Cyan else Color.Transparent).then(if (active) Modifier else Modifier.border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp))).clickable { st = p }.padding(horizontal = 18.dp, vertical = 9.dp)) {
                        Text("$p", color = if (active) Palette.OnAccent else Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp))
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(999.dp)).background(Palette.Cyan).clickable { onApply(st); onClose() }, contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.done), color = Palette.OnAccent, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 15.sp))
            }
            Box(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(999.dp)).border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)).clickable { onFinish(); onClose() }, contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.finish_game), color = Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp))
            }
        }
    }
}
