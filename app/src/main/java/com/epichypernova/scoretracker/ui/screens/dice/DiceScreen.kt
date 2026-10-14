package com.epichypernova.scoretracker.ui.screens.dice

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.data.AppActions
import com.epichypernova.scoretracker.data.Repository
import com.epichypernova.scoretracker.data.model.AppState
import com.epichypernova.scoretracker.ui.components.AppTab
import com.epichypernova.scoretracker.ui.components.ChamferCta
import com.epichypernova.scoretracker.ui.components.SectionLabel
import com.epichypernova.scoretracker.ui.components.Stepper
import com.epichypernova.scoretracker.ui.components.TabScaffold
import com.epichypernova.scoretracker.ui.components.cardSurface
import com.epichypernova.scoretracker.ui.components.rememberSoundEffect
import com.epichypernova.scoretracker.ui.theme.Cinzel
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Each kind of die gets its own color so a tray of mixed dice reads at a glance. */
private fun kindColor(sides: Int): Color = when (sides) {
    4 -> Palette.GameDnd
    6 -> Palette.Cyan
    8 -> Palette.Mint
    10 -> Palette.GameYugioh
    12 -> Palette.GameMagic
    20 -> Palette.GameUno
    else -> Palette.GameWarhammer
}

/** Vertices and rotation that give each kind a recognisable silhouette. */
private fun kindShape(sides: Int): Pair<Int, Float> = when (sides) {
    4 -> 3 to -90f      // triangle
    6 -> 4 to -45f      // square
    8 -> 4 to -90f      // diamond
    10 -> 5 to 90f      // pentagon, point down
    12 -> 5 to -90f     // pentagon, point up
    20 -> 6 to -90f     // hexagon, point up
    else -> 6 to 0f     // hexagon, flat top
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiceScreen(
    repo: Repository,
    state: AppState,
    tabLabels: Map<AppTab, String>,
    onSelectTab: (AppTab) -> Unit,
) {
    val tray = state.dice
    val dice = tray.sides
    var pickFor by remember { mutableIntStateOf(-1) }
    var rollKey by remember { mutableIntStateOf(0) }
    var rolling by remember { mutableStateOf(false) }
    var spin by remember { mutableStateOf(emptyList<Int>()) }
    val diceSound = rememberSoundEffect(R.raw.dice)

    // The real values are already committed to state; this just tumbles the faces first.
    LaunchedEffect(rollKey) {
        if (rollKey == 0) return@LaunchedEffect
        rolling = true
        diceSound()
        var step = 45L
        repeat(11) {
            spin = dice.map { (1..it).random() }
            delay(step)
            step += 9
        }
        rolling = false
    }

    TabScaffold(AppTab.DADOS, tabLabels, onSelectTab) { padding ->
        Column(Modifier.fillMaxSize().background(Palette.AppBg).padding(padding)) {
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    stringResource(R.string.dice_title),
                    color = Palette.TextPrimary,
                    style = TextStyle(fontFamily = Cinzel, fontWeight = FontWeight.Bold, fontSize = 29.sp, letterSpacing = 1.16.sp),
                    modifier = Modifier.padding(top = 22.dp, bottom = 2.dp),
                )

                Row(
                    Modifier.fillMaxWidth().cardSurface(16).padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.dice_count), color = Palette.TextPrimary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 15.sp))
                        Text(stringResource(R.string.dice_count_sub, AppActions.DICE_MAX), color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.5.sp))
                    }
                    Stepper(
                        dice.size,
                        // Counted off the state being updated, not the composed snapshot, so a
                        // held press keeps stepping instead of re-applying the same target.
                        onDec = { repo.update { AppActions.diceSetCount(it, it.dice.sides.size - 1) } },
                        onInc = { repo.update { AppActions.diceSetCount(it, it.dice.sides.size + 1) } },
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionLabel(stringResource(R.string.dice_label))
                    Text(
                        stringResource(R.string.dice_hint),
                        color = Palette.TextTertiary,
                        style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.5.sp),
                    )
                    FlowRow(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        dice.forEachIndexed { i, sides ->
                            DieCard(
                                sides = sides,
                                value = if (rolling) spin.getOrNull(i) else tray.results.getOrNull(i),
                                settled = !rolling && tray.results.size == dice.size,
                                onClick = { pickFor = i },
                            )
                        }
                    }
                }

                if (tray.results.size == dice.size && tray.results.isNotEmpty()) {
                    TotalCard(tray.results, rolling)
                }

                Box(Modifier.height(4.dp))
            }

            Box(Modifier.fillMaxWidth().background(Palette.AppBg).padding(horizontal = 20.dp, vertical = 14.dp)) {
                ChamferCta(
                    text = stringResource(if (rolling) R.string.dice_rolling else R.string.dice_roll),
                    onClick = {
                        if (!rolling) {
                            repo.update { AppActions.diceRoll(it) }
                            rollKey += 1
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    if (pickFor >= 0) {
        val idx = pickFor
        KindDialog(
            current = dice.getOrNull(idx) ?: AppActions.DICE_DEFAULT,
            onPick = { sides -> repo.update { AppActions.diceSetKind(it, idx, sides) } },
            onClose = { pickFor = -1 },
        )
    }
}

@Composable
private fun DieCard(sides: Int, value: Int?, settled: Boolean, onClick: () -> Unit) {
    val tint = kindColor(sides)
    val shape = RoundedCornerShape(18.dp)
    val pop by animateFloatAsState(if (settled) 1f else 0.94f, label = "die")
    Column(
        Modifier.width(96.dp).clip(shape).background(Palette.CardSurface)
            .border(1.dp, tint.copy(alpha = 0.45f), shape)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(Modifier.size(64.dp).scale(pop), contentAlignment = Alignment.Center) {
            val (corners, rotation) = kindShape(sides)
            Canvas(Modifier.fillMaxSize()) { polygon(corners, rotation, tint) }
            Text(
                value?.toString() ?: "–",
                color = if (value == null) Palette.TextMuted else Palette.TextPrimary,
                maxLines = 1,
                // Three digits only happen on a d100, and they need the smaller size to fit.
                style = TextStyle(
                    fontFamily = Orbitron,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if ((value ?: 0) >= 100) 19.sp else 24.sp,
                    fontFeatureSettings = "tnum",
                ),
            )
        }
        Text(
            "D$sides",
            color = tint,
            style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp),
        )
    }
}

@Composable
private fun TotalCard(results: List<Int>, rolling: Boolean) {
    Row(
        Modifier.fillMaxWidth().cardSurface(16).padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            SectionLabel(stringResource(R.string.dice_total))
            Text(
                results.joinToString(" + "),
                color = Palette.TextTertiary,
                style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.5.sp),
            )
        }
        Text(
            "${results.sum()}",
            color = if (rolling) Palette.TextMuted else Palette.Mint,
            style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, fontFeatureSettings = "tnum"),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KindDialog(current: Int, onPick: (Int) -> Unit, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.cancel), color = Palette.TextSecondary) } },
        title = { Text(stringResource(R.string.dice_pick_title), color = Palette.TextPrimary) },
        text = {
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppActions.DICE_KINDS.forEach { sides ->
                    val tint = kindColor(sides)
                    val active = sides == current
                    Box(
                        Modifier.width(72.dp).height(44.dp).clip(RoundedCornerShape(14.dp))
                            .background(if (active) tint.copy(alpha = 0.25f) else Color.Transparent)
                            .border(1.dp, if (active) tint else Palette.ButtonBorder, RoundedCornerShape(14.dp))
                            .clickable { onPick(sides); onClose() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "D$sides",
                            color = if (active) tint else Palette.TextSecondary,
                            style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                        )
                    }
                }
            }
        },
        containerColor = Palette.SheetSurface,
    )
}

/** Regular polygon, filled faintly and outlined in [color]. */
private fun DrawScope.polygon(corners: Int, rotationDeg: Float, color: Color) {
    val r = size.minDimension * 0.46f
    val cx = size.width / 2f
    val cy = size.height / 2f
    val path = Path().apply {
        repeat(corners) { k ->
            val a = (rotationDeg + k * 360f / corners) * PI.toFloat() / 180f
            val x = cx + r * cos(a)
            val y = cy + r * sin(a)
            if (k == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    drawPath(path, color.copy(alpha = 0.16f))
    drawPath(path, color.copy(alpha = 0.85f), style = Stroke(width = size.minDimension * 0.055f))
}
