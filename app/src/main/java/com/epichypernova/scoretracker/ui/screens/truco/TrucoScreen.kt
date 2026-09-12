package com.epichypernova.scoretracker.ui.screens.truco

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.data.AppActions
import com.epichypernova.scoretracker.data.Repository
import com.epichypernova.scoretracker.data.model.AppState
import com.epichypernova.scoretracker.data.model.TrucoSide
import com.epichypernova.scoretracker.ui.components.ChamferCta
import com.epichypernova.scoretracker.ui.components.CompactHeader
import com.epichypernova.scoretracker.ui.components.FinishMenu
import com.epichypernova.scoretracker.ui.components.Segmented
import com.epichypernova.scoretracker.ui.components.rememberAdGate
import com.epichypernova.scoretracker.ui.components.rememberSoundEffect
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

@Composable
fun TrucoScreen(
    repo: Repository,
    state: AppState,
    onBack: () -> Unit,
) {
    val adGate = rememberAdGate(repo, state)
    val match = state.trucoMatch
    if (match == null || match.chooseTarget) {
        TrucoChooser(onBack = onBack, onStart = { target -> adGate { repo.update { AppActions.trucoChooseTarget(it, target) } } })
        return
    }
    val whoosh = rememberSoundEffect(R.raw.whoosh)
    var menu by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Palette.AppBgDeep)) {
        Box(Modifier.fillMaxWidth()) {
            CompactHeader(
                title = stringResource(R.string.game_truco_title),
                meta = {
                    Row {
                        Text("A ${match.target} · ", color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.sp))
                        Text("${match.us.gamesWon}", color = Palette.Cyan, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 12.sp))
                        Text(" – ", color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.sp))
                        Text("${match.them.gamesWon}", color = Palette.Magenta, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 12.sp))
                    }
                },
                onBack = onBack,
                onTrailing = { menu = true },
            )
            Box(Modifier.align(Alignment.TopEnd)) {
                FinishMenu(menu, { menu = false }, onFinish = { repo.update { AppActions.trucoFinish(it) } })
            }
        }

        Row(Modifier.weight(1f).fillMaxWidth()) {
            TrucoSideView(
                label = stringResource(R.string.truco_us),
                accent = Palette.TrucoUs,
                side = match.us,
                target = match.target,
                onAdd = { n -> whoosh(); repo.update { AppActions.trucoAdd(it, us = true, amount = n) } },
                onRemove = { repo.update { AppActions.trucoRemove(it, us = true) } },
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            Box(Modifier.width(1.dp).fillMaxHeight().background(Color(0x1AFFFFFF)))
            TrucoSideView(
                label = stringResource(R.string.truco_them),
                accent = Palette.TrucoThem,
                side = match.them,
                target = match.target,
                onAdd = { n -> whoosh(); repo.update { AppActions.trucoAdd(it, us = false, amount = n) } },
                onRemove = { repo.update { AppActions.trucoRemove(it, us = false) } },
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }

        Row(
            Modifier.fillMaxWidth().background(Palette.AppBgDeep).navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FooterButton(stringResource(R.string.truco_undo), filled = false, modifier = Modifier.weight(1f)) { repo.update { AppActions.trucoUndo(it) } }
            FooterButton(stringResource(R.string.truco_new_match), filled = true, modifier = Modifier.weight(1f)) { repo.update { AppActions.trucoNewMatch(it) } }
        }
    }
}

@Composable
private fun TrucoChooser(onBack: () -> Unit, onStart: (Int) -> Unit) {
    var idx by remember { mutableStateOf(1) } // 0 = 15, 1 = 30
    Column(Modifier.fillMaxSize().background(Palette.AppBgDeep)) {
        CompactHeader(title = stringResource(R.string.game_truco_title), meta = {}, onBack = onBack, trailing = null)
        Column(
            Modifier.weight(1f).fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(stringResource(R.string.truco_how_many), color = Palette.TextPrimary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 18.sp))
            Segmented(
                options = listOf(stringResource(R.string.truco_to_15), stringResource(R.string.truco_to_30)),
                selectedIndex = idx,
                onSelect = { idx = it },
                modifier = Modifier.padding(top = 18.dp).width(260.dp),
            )
            ChamferCta(
                text = stringResource(R.string.start),
                onClick = { onStart(if (idx == 0) 15 else 30) },
                modifier = Modifier.padding(top = 24.dp).width(220.dp),
            )
        }
    }
}

@Composable
private fun TrucoSideView(
    label: String,
    accent: Color,
    side: TrucoSide,
    target: Int,
    onAdd: (Int) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val to30 = target >= 30
    val malas = side.points.coerceIn(0, if (to30) 15 else target)
    val buenas = if (to30) (side.points - 15).coerceIn(0, 15) else 0
    val numberColor = if (accent == Palette.TrucoUs) Palette.CyanNumber else Palette.MagentaNumber

    Column(
        modifier
            .pointerInput(Unit) { detectTapGestures(onDoubleTap = { onAdd(1) }) }
            .pointerInput(Unit) {
                var acc = 0f; var fired = false
                detectVerticalDragGestures(
                    onDragStart = { acc = 0f; fired = false },
                    onVerticalDrag = { _, dy ->
                        acc += dy
                        if (!fired && acc < -50f) { onAdd(1); fired = true }
                    },
                )
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label.uppercase(), color = accent, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 2.0.sp))
        Text("${side.points}", color = numberColor, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 60.sp, fontFeatureSettings = "tnum"))

        Spacer8()
        if (to30) {
            // Malas on top, Buenas below (a 30)
            PorotoLabel(stringResource(R.string.truco_malas))
            PorotoField(count = malas, color = Palette.PorotoStick)
            Box(Modifier.fillMaxWidth().height(1.dp).padding(vertical = 4.dp).background(accent.copy(alpha = 0.55f)))
            PorotoLabel(stringResource(R.string.truco_buenas))
            PorotoField(count = buenas, color = Palette.PorotoStick)
        } else {
            PorotoLabel(stringResource(R.string.truco_malas))
            PorotoField(count = malas, color = Palette.PorotoStick)
        }

        Spacer8()
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1, 2, 3).forEach { n ->
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(13.dp)).background(accent).clickable { onAdd(n) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("+$n", color = if (accent == Palette.TrucoUs) Palette.OnAccent else Color(0xFF240426), style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 17.sp))
                }
            }
        }
        Box(
            Modifier.padding(top = 8.dp).fillMaxWidth().height(40.dp).clip(RoundedCornerShape(999.dp)).border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)).clickable { onRemove() },
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(R.string.truco_remove), color = Palette.TextSecondary, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 13.sp))
        }
    }
}

@Composable
private fun PorotoLabel(text: String) {
    Text(text.uppercase(), color = Palette.TextMuted, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 9.5.sp, letterSpacing = 1.7.sp), modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
private fun Spacer8() = Box(Modifier.height(10.dp))

@Composable
private fun PorotoField(count: Int, color: Color) {
    Box(Modifier.fillMaxWidth().heightIn(min = 84.dp), contentAlignment = Alignment.Center) {
        if (count == 0) {
            Text("—", color = Palette.PorotoEmpty, style = TextStyle(fontSize = 20.sp))
        } else {
            val fullGroups = count / 5
            val remainder = count % 5
            // One group of 5 per line, stacking downward (like a truco score sheet).
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                repeat(fullGroups) { PorotoGroup(5, complete = true, color = color) }
                if (remainder > 0) PorotoGroup(remainder, complete = false, color = color)
            }
        }
    }
}

@Composable
private fun PorotoGroup(bars: Int, complete: Boolean, color: Color) {
    Box(contentAlignment = Alignment.CenterStart) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(if (complete) 4 else bars) {
                Box(Modifier.size(width = 3.dp, height = 26.dp).clip(RoundedCornerShape(2.dp)).background(color))
            }
        }
        if (complete) {
            Box(
                Modifier
                    .padding(top = 11.dp)
                    .rotate(-24f)
                    .size(width = 30.dp, height = 3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color),
            )
        }
    }
}

@Composable
private fun FooterButton(text: String, filled: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.height(48.dp).clip(RoundedCornerShape(999.dp))
            .background(if (filled) Palette.ControlFill else Color.Transparent)
            .then(if (filled) Modifier else Modifier.border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp)))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Palette.TextPrimary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 14.sp))
    }
}
