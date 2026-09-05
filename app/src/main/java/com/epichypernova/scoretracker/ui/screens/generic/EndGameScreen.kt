package com.epichypernova.scoretracker.ui.screens.generic

import android.media.MediaPlayer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.data.model.AppState
import com.epichypernova.scoretracker.ui.components.ChamferCta
import com.epichypernova.scoretracker.ui.components.ExplosionEffect
import com.epichypernova.scoretracker.ui.components.SectionLabel
import com.epichypernova.scoretracker.ui.components.menuBackdrop
import com.epichypernova.scoretracker.ui.theme.Cinzel
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

@Composable
fun EndGameScreen(
    state: AppState,
    onDone: () -> Unit,
) {
    val result = state.pendingResult
    if (result == null) {
        Box(Modifier.fillMaxSize().menuBackdrop())
        return
    }

    // Epic victory sound — plays on entry, released on exit; mute toggle pauses/resumes.
    val context = LocalContext.current
    var muted by remember { mutableStateOf(false) }
    val player = remember { runCatching { MediaPlayer.create(context, R.raw.victory) }.getOrNull() }
    DisposableEffect(Unit) {
        runCatching { player?.start() }
        onDispose { runCatching { player?.stop(); player?.release() } }
    }

    Box(Modifier.fillMaxSize().menuBackdrop()) {
    ExplosionEffect(Modifier.fillMaxSize())
    Column(
        Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("🏆", style = TextStyle(fontSize = 64.sp))
            Spacer(Modifier.height(10.dp))
            SectionLabel(stringResource(R.string.game_over).uppercase(), color = Palette.Cyan)
            Spacer(Modifier.height(6.dp))
            Text(
                if (result.winners.size <= 1) stringResource(R.string.winner_one) else stringResource(R.string.winner_many),
                color = Palette.TextTertiary,
                style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, letterSpacing = 1.5.sp),
            )
            Text(
                result.winners.joinToString(" · ").ifBlank { "—" },
                color = Palette.Mint,
                textAlign = TextAlign.Center,
                style = TextStyle(fontFamily = Cinzel, fontWeight = FontWeight.Black, fontSize = 34.sp, letterSpacing = 1.2.sp),
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                result.title,
                color = Palette.TextMuted,
                style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.sp),
                modifier = Modifier.padding(top = 2.dp),
            )

            Spacer(Modifier.height(26.dp))
            SectionLabel(stringResource(R.string.final_scores).uppercase())
            Spacer(Modifier.height(8.dp))
            result.lines.forEachIndexed { i, line ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("${i + 1}", color = Palette.TextMuted, modifier = Modifier.width(24.dp), style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 13.sp))
                    Text(line.name, color = Color(line.color), modifier = Modifier.weight(1f), style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 16.sp))
                    Text(
                        line.value,
                        color = if (line.winner) Palette.Mint else Palette.TextPrimary,
                        style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 22.sp, fontFeatureSettings = "tnum"),
                    )
                }
            }
        }

        Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (muted) "🔇" else "🔊",
                modifier = Modifier.width(48.dp).padding(end = 8.dp).clickable {
                    muted = !muted
                    runCatching { if (muted) player?.pause() else player?.start() }
                },
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 22.sp),
            )
            ChamferCta(text = stringResource(R.string.back_to_menu), onClick = onDone, modifier = Modifier.weight(1f))
        }
    }
    }
}
