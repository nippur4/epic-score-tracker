package com.epichypernova.scoretracker.ui.screens.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import com.epichypernova.scoretracker.ui.GameCatalog
import com.epichypernova.scoretracker.ui.GameEntry
import com.epichypernova.scoretracker.ui.components.CircleIconButton
import com.epichypernova.scoretracker.ui.components.GlyphBox
import com.epichypernova.scoretracker.ui.components.dashedBorder
import com.epichypernova.scoretracker.ui.theme.Cinzel
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

/** Mosaic variant of the main menu (design 2b): search + resume banner + 2-column tiles. */
@Composable
fun MenuGridContent(
    state: AppState,
    padding: PaddingValues,
    onToggleView: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCurrent: () -> Unit,
    onStartGeneric: (GameType) -> Unit,
    onOpenSpecific: (GameType) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val q = query.trim().lowercase()

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 20.dp, end = 20.dp,
                top = padding.calculateTopPadding() + 14.dp,
                bottom = padding.calculateBottomPadding() + 20.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header
        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "Epic Hypernova",
                modifier = Modifier.width(196.dp).aspectRatio(1993f / 789f),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CircleIconButton("▤", onToggleView)
                CircleIconButton("⚙", onOpenSettings)
            }
        }

        // Search
        Row(
            Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(999.dp))
                .background(Color(0x0DFFFFFF)).border(1.dp, Palette.CardBorder, RoundedCornerShape(999.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("⌕", color = Color(0xFF7387AF), style = TextStyle(fontSize = 15.sp))
            Box(Modifier.weight(1f).padding(start = 10.dp)) {
                if (query.isEmpty()) {
                    Text(stringResource(R.string.search_hint), color = Color(0xFF7387AF), style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 14.sp))
                }
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = SpaceGrotesk, fontSize = 14.sp, color = Palette.TextPrimary),
                    cursorBrush = SolidColor(Palette.Cyan),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // Resume banner
        state.currentGame?.let { game ->
            val names = game.playerIds.mapNotNull { id -> state.users.firstOrNull { it.id == id }?.name }.joinToString(", ")
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0x142FD3F0))
                    .border(1.dp, Color(0x732FD3F0), RoundedCornerShape(16.dp)).clickable { onOpenCurrent() }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(Palette.Mint))
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(
                        stringResource(R.string.resume_prefix, game.name ?: "", Derivations.currentRoundNumber(game)),
                        color = Palette.TextPrimary,
                        style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                    )
                    Text(names, color = Color(0xFFA9BCE0), style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.sp), maxLines = 1)
                }
                Text(stringResource(R.string.open), color = Palette.Cyan, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 12.sp))
            }
        }

        // Grid of tiles (generics + specifics), filtered by query on the localized title
        val all = GameCatalog.generics.map { Triple(it, true, stringResource(it.titleRes)) } +
            GameCatalog.specifics.map { Triple(it, false, stringResource(it.titleRes)) }
        val visible = all.filter { q.isEmpty() || it.third.lowercase().contains(q) }

        visible.chunked(2).forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowItems.forEach { (entry, isGeneric, title) ->
                    Box(Modifier.weight(1f)) {
                        if (isGeneric) {
                            GenericTile(entry, title, primary = entry.gameType == GameType.MANOS_Y_PUNTOS) { onStartGeneric(entry.gameType) }
                        } else {
                            SpecificTile(entry, title) { if (entry.available) onOpenSpecific(entry.gameType) }
                        }
                    }
                }
                if (rowItems.size == 1) Box(Modifier.weight(1f))
            }
        }

        // Request-a-game tile
        Box(
            Modifier.fillMaxWidth().height(64.dp).dashedBorder(Color(0x2EFFFFFF), radius = 16),
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(R.string.request_game), color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 14.sp))
        }
    }
}

@Composable
private fun GenericTile(entry: GameEntry, title: String, primary: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().height(136.dp).clip(RoundedCornerShape(20.dp))
            .background(if (primary) Palette.Cyan else Palette.CardSurface)
            .then(if (primary) Modifier else Modifier.border(1.dp, Palette.CardBorder, RoundedCornerShape(20.dp)))
            .clickable { onClick() }
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(entry.glyph, color = if (primary) Palette.OnAccent else entry.tint, style = TextStyle(fontFamily = Cinzel, fontWeight = FontWeight.Bold, fontSize = 26.sp))
        Text(
            title,
            color = if (primary) Palette.OnAccent else Palette.TextPrimary,
            style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
        )
    }
}

@Composable
private fun SpecificTile(entry: GameEntry, title: String, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().height(126.dp).clip(RoundedCornerShape(20.dp))
            .background(Palette.CardSurface).border(1.dp, Palette.CardBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        GlyphBox(entry.glyph, entry.tint, boxSize = 36, radius = 11, glyphSize = 16)
        Text(
            title,
            color = if (entry.available) Palette.TextPrimary else Palette.TextMuted,
            style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
        )
    }
}

