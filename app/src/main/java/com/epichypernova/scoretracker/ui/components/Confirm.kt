package com.epichypernova.scoretracker.ui.components

import android.view.WindowManager
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

private val DANGER = Color(0xFFEB5757)

/**
 * Guards an action that can't be undone. Call it in the screen body and use the returned
 * lambda where the action used to go: it opens the dialog, and only a confirm runs [onConfirm].
 */
@Composable
fun rememberConfirm(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
): () -> Unit {
    var asking by remember { mutableStateOf(false) }
    if (asking) {
        AlertDialog(
            onDismissRequest = { asking = false },
            dismissButton = {
                TextButton(onClick = { asking = false }) {
                    Text(stringResource(R.string.cancel), color = Palette.TextSecondary)
                }
            },
            confirmButton = {
                TextButton(onClick = { asking = false; onConfirm() }) {
                    Text(confirmLabel, color = DANGER, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold))
                }
            },
            title = { Text(title, color = Palette.TextPrimary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)) },
            text = { Text(message, color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 13.5.sp, lineHeight = 19.sp)) },
            containerColor = Palette.SheetSurface,
        )
    }
    return { asking = true }
}

/** "Reiniciar": every score back to zero, players kept. */
@Composable
fun rememberResetConfirm(onConfirm: () -> Unit): () -> Unit = rememberConfirm(
    title = stringResource(R.string.confirm_reset_title),
    message = stringResource(R.string.confirm_reset_text),
    confirmLabel = stringResource(R.string.score_reset_word),
    onConfirm = onConfirm,
)

/** "Nueva partida": the board in play is thrown away. */
@Composable
fun rememberNewGameConfirm(onConfirm: () -> Unit): () -> Unit = rememberConfirm(
    title = stringResource(R.string.confirm_new_title),
    message = stringResource(R.string.confirm_new_text),
    confirmLabel = stringResource(R.string.confirm_new_button),
    onConfirm = onConfirm,
)

/** "Terminar partida": goes to the history and can't be taken back. */
@Composable
fun rememberFinishConfirm(onConfirm: () -> Unit): () -> Unit = rememberConfirm(
    title = stringResource(R.string.confirm_finish_title),
    message = stringResource(R.string.confirm_finish_text),
    confirmLabel = stringResource(R.string.confirm_finish_button),
    onConfirm = onConfirm,
)

/**
 * Keeps the screen awake while this composable is in the tree, so a board left on the
 * table doesn't lock mid-game. Switched off from Ajustes.
 */
@Composable
fun KeepScreenOn(enabled: Boolean = true) {
    val window = LocalContext.current.findActivity()?.window
    DisposableEffect(window, enabled) {
        if (enabled) window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
}
