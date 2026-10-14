package com.epichypernova.scoretracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk
import kotlin.math.abs

/**
 * Standings shared by the boards that take more than a handful of players: who is in which
 * position and with what score, without scrolling the board itself.
 */

/** One line of a standings list: who, their color, and the number that ranks them. */
data class Standing(val name: String, val color: Color, val score: Int)

/**
 * Competition ranking over [entries], best first: tied players share a position and the next
 * one skips (1, 2, 2, 4). [lowWins] puts the smallest score on top, as darts does.
 */
fun rankStandings(entries: List<Standing>, lowWins: Boolean): List<Pair<Int, Standing>> {
    val sorted = if (lowWins) entries.sortedBy { it.score } else entries.sortedByDescending { it.score }
    var position = 1
    var previous: Int? = null
    return sorted.mapIndexed { i, entry ->
        if (previous != entry.score) {
            position = i + 1
            previous = entry.score
        }
        position to entry
    }
}

/** Top-bar pill that opens [StandingsSheet]. */
@Composable
fun StandingsPill(onClick: () -> Unit) {
    GamePill("🏆", label = stringResource(R.string.a11y_standings), onClick = onClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandingsSheet(
    entries: List<Standing>,
    lowWins: Boolean,
    accent: Color = Palette.Cyan,
    onClose: () -> Unit,
) {
    val ranked = remember(entries, lowWins) { rankStandings(entries, lowWins) }
    val best = ranked.firstOrNull()?.second?.score
    val sharedLead = ranked.count { it.first == 1 } > 1
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, containerColor = Palette.SheetSurface) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    stringResource(R.string.standings_title),
                    color = Palette.TextPrimary,
                    style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
                )
                Text(
                    stringResource(
                        R.string.standings_rule,
                        stringResource(if (lowWins) R.string.wins_menor_word else R.string.wins_mayor_word),
                    ),
                    color = Palette.TextTertiary,
                    style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.sp),
                )
            }
            // Twenty players still fit: the list scrolls inside the sheet.
            Column(
                Modifier.heightIn(max = (LocalConfiguration.current.screenHeightDp * 0.6f).dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ranked.forEach { (position, entry) ->
                    StandingRow(
                        position = position,
                        entry = entry,
                        leading = best != null && entry.score == best,
                        sharedLead = sharedLead,
                        gap = abs(entry.score - (best ?: entry.score)),
                        accent = accent,
                    )
                }
            }
        }
    }
}

@Composable
private fun StandingRow(
    position: Int,
    entry: Standing,
    leading: Boolean,
    sharedLead: Boolean,
    gap: Int,
    accent: Color,
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier.fillMaxWidth().rowSurface(14)
            .then(if (leading) Modifier.border(1.dp, accent.copy(alpha = 0.55f), shape) else Modifier)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.size(30.dp).clip(CircleShape)
                .background(if (leading) accent.copy(alpha = 0.22f) else Color(0x14FFFFFF)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "$position",
                color = if (leading) accent else Palette.TextSecondary,
                style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFeatureSettings = "tnum"),
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(entry.color).border(1.dp, Color(0x33FFFFFF), CircleShape))
                EllipsisText(
                    entry.name,
                    TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                    Palette.TextPrimary,
                )
            }
            Text(
                when {
                    leading && sharedLead -> stringResource(R.string.standings_tied)
                    leading -> stringResource(R.string.standings_leader)
                    else -> stringResource(R.string.standings_behind, gap)
                },
                color = if (leading) Palette.Mint else Palette.TextMuted,
                style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 11.5.sp),
            )
        }
        Text(
            "${entry.score}",
            color = Palette.TextPrimary,
            style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, fontFeatureSettings = "tnum"),
        )
    }
}
