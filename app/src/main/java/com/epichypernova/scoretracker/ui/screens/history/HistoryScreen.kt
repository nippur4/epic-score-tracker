package com.epichypernova.scoretracker.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.annotation.StringRes
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.data.model.AppState
import com.epichypernova.scoretracker.data.model.GameType
import com.epichypernova.scoretracker.data.model.HistoryEntry
import com.epichypernova.scoretracker.ui.GameCatalog
import com.epichypernova.scoretracker.ui.components.AppTab
import com.epichypernova.scoretracker.ui.components.EllipsisText
import com.epichypernova.scoretracker.ui.components.FilterChip
import com.epichypernova.scoretracker.ui.components.GlyphBox
import com.epichypernova.scoretracker.ui.components.SectionLabel
import com.epichypernova.scoretracker.ui.components.TabScaffold
import com.epichypernova.scoretracker.ui.components.cardSurface
import com.epichypernova.scoretracker.ui.theme.Cinzel
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

private enum class HFilter(@StringRes val labelRes: Int) {
    TODAS(R.string.filter_all), GENERICOS(R.string.filter_generics), TRUCO(R.string.filter_truco), MAGIC(R.string.filter_magic)
}

@Composable
fun HistoryScreen(
    state: AppState,
    tabLabels: Map<AppTab, String>,
    onSelectTab: (AppTab) -> Unit,
) {
    var filter by remember { mutableStateOf(HFilter.TODAS) }

    val filtered = state.history.filter { e ->
        when (filter) {
            HFilter.TODAS -> true
            HFilter.GENERICOS -> e.gameType.isGeneric
            HFilter.TRUCO -> e.gameType == GameType.TRUCO
            HFilter.MAGIC -> e.gameType == GameType.MAGIC
        }
    }.sortedByDescending { it.finishedAt }

    val now = System.currentTimeMillis()
    val week = 7 * 24 * 60 * 60 * 1000L
    val thisWeek = filtered.filter { now - it.finishedAt <= week }
    val older = filtered.filter { now - it.finishedAt > week }

    TabScaffold(AppTab.HISTORIAL, tabLabels, onSelectTab) { padding ->
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp, end = 20.dp,
                top = padding.calculateTopPadding() + 22.dp,
                bottom = padding.calculateBottomPadding() + 20.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                SectionLabel(stringResource(R.string.history_count, state.history.size), color = Palette.Cyan)
                Text(
                    stringResource(R.string.history_title),
                    color = Palette.TextPrimary,
                    style = TextStyle(fontFamily = Cinzel, fontWeight = FontWeight.Bold, fontSize = 29.sp, letterSpacing = 1.16.sp),
                    modifier = Modifier.padding(top = 4.dp, bottom = 10.dp),
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HFilter.entries.forEach { f ->
                        FilterChip(stringResource(f.labelRes), active = filter == f, onClick = { filter = f })
                    }
                }
            }
            if (thisWeek.isNotEmpty()) {
                item { SectionLabel(stringResource(R.string.group_this_week), modifier = Modifier.padding(top = 12.dp, bottom = 2.dp)) }
                items(thisWeek) { HistoryCard(it, now) }
            }
            if (older.isNotEmpty()) {
                item { SectionLabel(stringResource(R.string.group_before), modifier = Modifier.padding(top = 12.dp, bottom = 2.dp)) }
                items(older) { HistoryCard(it, now) }
            }
        }
    }
}

@Composable
private fun HistoryCard(entry: HistoryEntry, now: Long) {
    Row(
        Modifier.fillMaxWidth().cardSurface(16).padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlyphBox(
            glyph = GameCatalog.glyphFor(entry.gameType),
            tint = GameCatalog.tintFor(entry.gameType),
            boxSize = 40, radius = 12, glyphSize = 19,
        )
        androidx.compose.foundation.layout.Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(GameCatalog.labelRes(entry.gameType)),
                    color = Palette.TextPrimary,
                    style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                )
                Text(
                    "  ·  ${relativeMoment(now - entry.finishedAt)}",
                    color = Palette.TextMuted,
                    style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 11.5.sp),
                )
            }
            EllipsisText(
                entry.summary,
                style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.5.sp),
                color = Palette.TextTertiary,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                stringResource(R.string.won),
                color = Palette.Mint,
                style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 9.5.sp, letterSpacing = 1.3.sp),
                textAlign = TextAlign.End,
            )
            Text(
                entry.winnerNames.joinToString(", "),
                color = Palette.Mint,
                style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun relativeMoment(deltaMs: Long): String {
    val h = deltaMs / (60 * 60 * 1000L)
    return when {
        h < 1 -> stringResource(R.string.moment_now)
        h < 24 -> stringResource(R.string.moment_hours, h.toInt())
        else -> {
            val d = h / 24
            if (d == 1L) stringResource(R.string.moment_yesterday) else stringResource(R.string.moment_days, d.toInt())
        }
    }
}
