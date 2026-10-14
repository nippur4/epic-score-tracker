package com.epichypernova.scoretracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.data.model.GameType
import com.epichypernova.scoretracker.ui.GameCatalog
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

/**
 * A short "how to play" per game: what it is about, and how points get entered here.
 * Games whose rulebooks can't be summarised honestly in a paragraph (D&D and the TCGs)
 * have no entry, and the entry points below simply draw nothing for them.
 */
private fun rulesOf(type: GameType): Pair<Int, Int>? = when (type) {
    GameType.MANOS_Y_PUNTOS -> R.string.rules_manos to R.string.rules_manos_score
    GameType.CONTADOR_SIMPLE -> R.string.rules_contador to R.string.rules_contador_score
    GameType.TRUCO -> R.string.rules_truco to R.string.rules_truco_score
    GameType.CHINCHON -> R.string.rules_chinchon to R.string.rules_chinchon_score
    GameType.BURAKO -> R.string.rules_burako to R.string.rules_burako_score
    GameType.DARTS -> R.string.rules_darts to R.string.rules_darts_score
    GameType.GENERALA -> R.string.rules_generala to R.string.rules_generala_score
    GameType.BOWLING -> R.string.rules_bowling to R.string.rules_bowling_score
    GameType.UNO -> R.string.rules_uno to R.string.rules_uno_score
    GameType.WARHAMMER -> R.string.rules_warhammer to R.string.rules_warhammer_score
    GameType.POKER -> R.string.rules_poker to R.string.rules_poker_score
    GameType.ESCOBA -> R.string.rules_escoba to R.string.rules_escoba_score
    GameType.MUS -> R.string.rules_mus to R.string.rules_mus_score
    else -> null
}

/** Whether this game has a rules sheet at all. */
fun hasRules(type: GameType): Boolean = rulesOf(type) != null

/** Full-width button for a game's config sheet. Draws nothing for games without rules. */
@Composable
fun RulesButton(type: GameType) {
    if (!hasRules(type)) return
    var show by remember { mutableStateOf(false) }
    SheetSecondaryButton(stringResource(R.string.rules_view)) { show = true }
    if (show) RulesDialog(type) { show = false }
}

/** Quiet link for the setup screens, where you decide what you're getting into. */
@Composable
fun RulesLink(type: GameType, modifier: Modifier = Modifier) {
    if (!hasRules(type)) return
    var show by remember { mutableStateOf(false) }
    Row(
        modifier.clip(RoundedCornerShape(999.dp)).clickable { show = true }.padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("ⓘ", color = Palette.Cyan, style = TextStyle(fontSize = 13.sp))
        Text(
            stringResource(R.string.rules_view),
            color = Palette.Cyan,
            style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
        )
    }
    if (show) RulesDialog(type) { show = false }
}

/** The sheet itself, for screens that open it from their own menu. */
@Composable
fun RulesDialog(type: GameType, onClose: () -> Unit) {
    val (summary, scoring) = rulesOf(type) ?: return
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            TextButton(onClick = onClose) {
                Text(stringResource(R.string.done), color = Palette.Cyan, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold))
            }
        },
        title = {
            Text(
                stringResource(GameCatalog.labelRes(type)),
                color = Palette.TextPrimary,
                style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
            )
        },
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionLabel(stringResource(R.string.rules_section_game))
                RulesParagraph(stringResource(summary))
                SectionLabel(stringResource(R.string.rules_section_score), modifier = Modifier.padding(top = 8.dp))
                RulesParagraph(stringResource(scoring))
            }
        },
        containerColor = Palette.SheetSurface,
    )
}

@Composable
private fun RulesParagraph(text: String) {
    Text(
        text,
        color = Palette.TextSecondary,
        style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 13.5.sp, lineHeight = 19.sp),
    )
}
