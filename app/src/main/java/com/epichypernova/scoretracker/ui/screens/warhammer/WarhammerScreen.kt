package com.epichypernova.scoretracker.ui.screens.warhammer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.data.AppActions
import com.epichypernova.scoretracker.data.Repository
import com.epichypernova.scoretracker.data.model.AppState
import com.epichypernova.scoretracker.data.model.WarhammerPlayer
import com.epichypernova.scoretracker.ui.components.GamePill
import com.epichypernova.scoretracker.ui.components.LabeledCounter
import com.epichypernova.scoretracker.ui.components.MiniStep
import com.epichypernova.scoretracker.ui.components.PlayerNameRow
import com.epichypernova.scoretracker.ui.components.SheetPrimaryButton
import com.epichypernova.scoretracker.ui.components.SheetSecondaryButton
import com.epichypernova.scoretracker.ui.components.gamePaneGradient
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

private val ACCENT get() = Palette.GameWarhammer
private val CP_COLOR = Color(0xFFFFD98A)

@Composable
fun WarhammerScreen(repo: Repository, state: AppState, onBack: () -> Unit) {
    LaunchedEffect(Unit) { repo.update { AppActions.warhammerEnsureExists(it) } }
    val game = state.warhammerGame ?: return
    if (game.players.size < 2) return
    var showConfig by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(Palette.AppBgDeep)) {
        Column(Modifier.fillMaxSize()) {
            Pane(game.players[0], 0, repo, rotated = true, modifier = Modifier.weight(1f).fillMaxWidth())
            Row(
                Modifier.fillMaxWidth().background(Palette.AppBgDeep).border(1.dp, ACCENT.copy(alpha = 0.35f), RoundedCornerShape(0.dp)).padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.wh_round), color = Palette.TextMuted, maxLines = 1, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 10.sp, letterSpacing = 1.2.sp))
                    MiniStep("−") { repo.update { AppActions.warhammerRound(it, -1) } }
                    Text("${game.round}/5", color = Palette.TextPrimary, modifier = Modifier.widthIn(min = 30.dp), textAlign = TextAlign.Center, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 15.sp, fontFeatureSettings = "tnum"))
                    MiniStep("+") { repo.update { AppActions.warhammerRound(it, +1) } }
                }
                GamePill(stringResource(R.string.score_reset)) { repo.update { AppActions.warhammerReset(it) } }
                GamePill("⚙") { showConfig = true }
            }
            Pane(game.players[1], 1, repo, rotated = false, modifier = Modifier.weight(1f).fillMaxWidth())
        }
    }

    if (showConfig) {
        ConfigSheet(onFinish = { repo.update { AppActions.warhammerFinish(it) } }, onClose = { showConfig = false })
    }
}

@Composable
private fun Pane(p: WarhammerPlayer, index: Int, repo: Repository, rotated: Boolean, modifier: Modifier = Modifier) {
    val tint = Color(p.color)
    val grad = gamePaneGradient(tint)
    val bg = Modifier.drawBehind {
        drawRect(brush = ShaderBrush(RadialGradientShader(Offset(size.width * 0.5f, size.height), size.height * 0.95f, grad, listOf(0f, 0.6f, 1f))))
    }
    Box(modifier.then(bg)) {
        Column(Modifier.fillMaxSize().rotate(if (rotated) 180f else 0f).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            PlayerNameRow(p.name, tint)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("${AppActions.warhammerTotal(p)}", color = Color.White, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.ExtraBold, fontSize = 60.sp, fontFeatureSettings = "tnum"))
                Text("VP", color = Palette.TextTertiary, modifier = Modifier.padding(bottom = 12.dp), style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.5.sp))
            }
            Column(Modifier.padding(top = 6.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LabeledCounter(stringResource(R.string.wh_primary), p.primary, Palette.Cyan, big = true,
                    onDec = { repo.update { AppActions.warhammerPrimary(it, index, -1) } }, onInc = { repo.update { AppActions.warhammerPrimary(it, index, +1) } })
                LabeledCounter(stringResource(R.string.wh_secondary), p.secondary, Palette.Mint, big = true,
                    onDec = { repo.update { AppActions.warhammerSecondary(it, index, -1) } }, onInc = { repo.update { AppActions.warhammerSecondary(it, index, +1) } })
                LabeledCounter(stringResource(R.string.wh_cp), p.cp, CP_COLOR, big = true,
                    onDec = { repo.update { AppActions.warhammerCp(it, index, -1) } }, onInc = { repo.update { AppActions.warhammerCp(it, index, +1) } })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigSheet(onFinish: () -> Unit, onClose: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, containerColor = Palette.SheetSurface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SheetPrimaryButton(stringResource(R.string.finish_game), ACCENT) { onFinish(); onClose() }
            SheetSecondaryButton(stringResource(R.string.cancel)) { onClose() }
        }
    }
}
