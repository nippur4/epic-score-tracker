package com.epichypernova.scoretracker.ui.screens.swu

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
import com.epichypernova.scoretracker.data.model.SwuPlayer
import com.epichypernova.scoretracker.ui.components.GameIcon
import com.epichypernova.scoretracker.ui.components.GamePill
import com.epichypernova.scoretracker.ui.components.gameInsets
import com.epichypernova.scoretracker.ui.components.NumberPadSheet
import com.epichypernova.scoretracker.ui.components.PlayerNameRow
import com.epichypernova.scoretracker.ui.components.PresetChips
import com.epichypernova.scoretracker.ui.components.SheetLabel
import com.epichypernova.scoretracker.ui.components.SheetPrimaryButton
import com.epichypernova.scoretracker.ui.components.SheetSecondaryButton
import com.epichypernova.scoretracker.ui.components.gamePaneGradient
import com.epichypernova.scoretracker.ui.components.repeatingClickable
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

private val ACCENT get() = Palette.GameSwu

@Composable
fun SwuScreen(repo: Repository, state: AppState, onBack: () -> Unit) {
    LaunchedEffect(Unit) { repo.update { AppActions.swuEnsureExists(it) } }
    val game = state.swuGame ?: return
    if (game.players.size < 2) return
    var showConfig by remember { mutableStateOf(false) }
    var damageFor by remember { mutableIntStateOf(-1) }

    Box(Modifier.fillMaxSize().background(Palette.AppBgDeep)) {
        Column(Modifier.fillMaxSize().gameInsets()) {
            Pane(game.players[0], 0, repo, hasInitiative = game.initiative == 0, rotated = true, onDamage = { damageFor = 0 }, modifier = Modifier.weight(1f).fillMaxWidth())
            Row(
                Modifier.fillMaxWidth().background(Palette.AppBgDeep).border(1.dp, ACCENT.copy(alpha = 0.35f), RoundedCornerShape(0.dp)).padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GameIcon(R.drawable.ic_shield, ACCENT, 16)
                Text("${stringResource(R.string.swu_base_hp).uppercase()} ${game.startingHp}", color = Palette.TextMuted, modifier = Modifier.weight(1f), style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 11.sp, letterSpacing = 1.4.sp))
                GamePill(stringResource(R.string.score_reset)) { repo.update { AppActions.swuReset(it) } }
                GamePill("⚙") { showConfig = true }
            }
            Pane(game.players[1], 1, repo, hasInitiative = game.initiative == 1, rotated = false, onDamage = { damageFor = 1 }, modifier = Modifier.weight(1f).fillMaxWidth())
        }
    }

    if (damageFor >= 0) {
        val idx = damageFor
        NumberPadSheet(
            title = stringResource(R.string.dnd_damage_title),
            subtitle = "${game.players[idx].name} · ${game.players[idx].hp}",
            accent = Color(0xFFEB5757),
            confirmLabel = stringResource(R.string.dnd_damage),
            onConfirm = { n -> repo.update { AppActions.swuHp(it, idx, -n) } },
            onClose = { damageFor = -1 },
            quickAdds = listOf(2, 4, 6, 10),
            max = 99,
        )
    }
    if (showConfig) {
        ConfigSheet(
            hp = game.startingHp,
            onApply = { v -> repo.update { AppActions.swuSetStarting(it, v) } },
            onFinish = { repo.update { AppActions.swuFinish(it) } },
            onClose = { showConfig = false },
        )
    }
}

@Composable
private fun Pane(p: SwuPlayer, index: Int, repo: Repository, hasInitiative: Boolean, rotated: Boolean, onDamage: () -> Unit, modifier: Modifier = Modifier) {
    val tint = Color(p.color)
    val grad = gamePaneGradient(tint)
    val bg = Modifier.drawBehind {
        if (p.defeated) drawRect(Color(0xFF131A2E))
        else drawRect(brush = ShaderBrush(RadialGradientShader(Offset(size.width * 0.5f, size.height), size.height * 0.95f, grad, listOf(0f, 0.6f, 1f))))
    }
    Box(modifier.then(bg)) {
        Column(Modifier.fillMaxSize().rotate(if (rotated) 180f else 0f).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            PlayerNameRow(p.name, tint)
            if (p.defeated) {
                Text("${p.hp}", color = Color(0xFF3E4A6B), style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 66.sp, fontFeatureSettings = "tnum"))
                Text(stringResource(R.string.onepiece_defeated), color = Palette.Cyan, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp, letterSpacing = 1.6.sp))
            } else {
                Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    HpButton("−") { repo.update { AppActions.swuHp(it, index, -1) } }
                    Text("${p.hp}", color = if (p.hp <= 5) Color(0xFFEB5757) else Color.White, maxLines = 1, softWrap = false, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.ExtraBold, fontSize = 66.sp, fontFeatureSettings = "tnum"))
                    HpButton("＋") { repo.update { AppActions.swuHp(it, index, +1) } }
                }
                Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        Modifier.height(40.dp).clip(RoundedCornerShape(999.dp)).background(Color(0x38EB5757)).border(1.dp, Color(0x99EB5757), RoundedCornerShape(999.dp)).clickable { onDamage() }.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        GameIcon(R.drawable.ic_burst, Color(0xFFEB5757), 16)
                        Text(stringResource(R.string.dnd_damage), color = Palette.TextPrimary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 13.sp))
                    }
                    val initFg = if (hasInitiative) Palette.OnAccent else Palette.TextSecondary
                    Row(
                        Modifier.height(40.dp).clip(RoundedCornerShape(999.dp)).background(if (hasInitiative) ACCENT else Color(0x17FFFFFF))
                            .then(if (hasInitiative) Modifier else Modifier.border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)))
                            .clickable { repo.update { AppActions.swuInitiative(it, index) } }.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        GameIcon(R.drawable.ic_bolt, initFg, 16)
                        Text(stringResource(R.string.swu_initiative), color = initFg, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.2.sp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HpButton(symbol: String, onClick: () -> Unit) {
    Box(Modifier.size(56.dp).clip(CircleShape).border(1.dp, Color(0x2EFFFFFF), CircleShape).repeatingClickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(symbol, color = Color.White, style = TextStyle(fontSize = 26.sp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigSheet(hp: Int, onApply: (Int) -> Unit, onFinish: () -> Unit, onClose: () -> Unit) {
    var v by remember { mutableIntStateOf(hp) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, containerColor = Palette.SheetSurface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SheetLabel(stringResource(R.string.swu_base_hp))
            PresetChips(listOf(25, 28, 30), v, ACCENT) { v = it }
            SheetPrimaryButton(stringResource(R.string.done), ACCENT) { onApply(v); onClose() }
            SheetSecondaryButton(stringResource(R.string.finish_game)) { onFinish(); onClose() }
        }
    }
}
