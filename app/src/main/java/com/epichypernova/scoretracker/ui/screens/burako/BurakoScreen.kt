package com.epichypernova.scoretracker.ui.screens.burako

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
import com.epichypernova.scoretracker.data.model.BurakoTeam
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
fun BurakoScreen(repo: Repository, state: AppState, onBack: () -> Unit) {
    LaunchedEffect(Unit) { repo.update { AppActions.burakoEnsureExists(it) } }
    val game = state.burakoGame ?: return
    val teams = game.teams
    if (teams.size < 2) return

    var showConfig by remember { mutableStateOf(false) }
    var addForIndex by remember { mutableIntStateOf(-1) }

    Box(Modifier.fillMaxSize().background(Palette.AppBgDeep)) {
        Column(Modifier.fillMaxSize()) {
            TeamPane(teams[0], game.target, repo, rotated = true, onAdd = { addForIndex = 0 }, modifier = Modifier.weight(1f).fillMaxWidth())
            CentralBar(game.target, onReset = { repo.update { AppActions.burakoReset(it) } }, onConfig = { showConfig = true })
            TeamPane(teams[1], game.target, repo, rotated = false, onAdd = { addForIndex = 1 }, modifier = Modifier.weight(1f).fillMaxWidth())
        }
    }

    if (addForIndex >= 0) {
        val idx = addForIndex
        NumberPadSheet(
            title = stringResource(R.string.burako_add_title),
            accent = Palette.Cyan,
            confirmLabel = stringResource(R.string.score_add),
            onConfirm = { amount -> repo.update { AppActions.burakoAdd(it, idx, amount) } },
            onClose = { addForIndex = -1 },
            quickAdds = listOf(200, 100),
            max = 9_999,
        )
    }
    if (showConfig) {
        ConfigSheet(
            target = game.target,
            onApply = { t -> repo.update { AppActions.burakoSetTarget(it, t) } },
            onFinish = { repo.update { AppActions.burakoFinish(it) } },
            onClose = { showConfig = false },
        )
    }
}

@Composable
private fun TeamPane(team: BurakoTeam, target: Int, repo: Repository, rotated: Boolean, onAdd: () -> Unit, modifier: Modifier = Modifier) {
    val grad = paneGradient(Color(team.color))
    val bg = Modifier.drawBehind {
        drawRect(brush = ShaderBrush(RadialGradientShader(Offset(size.width * 0.5f, size.height), size.height * 0.95f, grad, listOf(0f, 0.6f, 1f))))
    }
    Box(modifier.then(bg)) {
        Column(Modifier.fillMaxSize().rotate(if (rotated) 180f else 0f).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(12.dp).clip(CircleShape).background(Color(team.color)).border(1.dp, Color(0x33FFFFFF), CircleShape))
                Text(team.name.uppercase(), color = Color(team.color), style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.9.sp))
            }
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 4.dp)) {
                Text("${team.score}", color = Color.White, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.ExtraBold, fontSize = 56.sp, fontFeatureSettings = "tnum"))
                Text("/$target", color = Palette.TextMuted, modifier = Modifier.padding(bottom = 8.dp), style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 16.sp))
            }
            Box(
                Modifier.padding(top = 8.dp).height(46.dp).clip(RoundedCornerShape(999.dp)).background(Palette.Cyan).clickable { onAdd() }.padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) { Text(stringResource(R.string.score_add_short), color = Palette.OnAccent, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 15.sp)) }
        }
    }
}

@Composable
private fun CentralBar(target: Int, onReset: () -> Unit, onConfig: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Palette.AppBgDeep).border(1.dp, Color(0x47F27BA9), RoundedCornerShape(0.dp)).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(stringResource(R.string.score_target) + ": $target", color = Palette.TextMuted, modifier = Modifier.weight(1f), style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 11.sp, letterSpacing = 1.2.sp))
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
private fun ConfigSheet(target: Int, onApply: (Int) -> Unit, onFinish: () -> Unit, onClose: () -> Unit) {
    var t by remember { mutableIntStateOf(target) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val presets = listOf(2000, 3000, 1000)
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, containerColor = Palette.SheetSurface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(stringResource(R.string.score_target), color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.5.sp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                presets.forEach { p ->
                    val active = p == t
                    Box(Modifier.clip(RoundedCornerShape(999.dp)).background(if (active) Palette.Cyan else Color.Transparent).then(if (active) Modifier else Modifier.border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp))).clickable { t = p }.padding(horizontal = 16.dp, vertical = 9.dp)) {
                        Text("$p", color = if (active) Palette.OnAccent else Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp))
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(999.dp)).background(Palette.Cyan).clickable { onApply(t); onClose() }, contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.done), color = Palette.OnAccent, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 15.sp))
            }
            Box(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(999.dp)).border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)).clickable { onFinish(); onClose() }, contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.finish_game), color = Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp))
            }
        }
    }
}
