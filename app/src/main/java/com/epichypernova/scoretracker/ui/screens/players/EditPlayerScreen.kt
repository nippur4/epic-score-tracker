package com.epichypernova.scoretracker.ui.screens.players

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.data.AppActions
import com.epichypernova.scoretracker.data.Repository
import com.epichypernova.scoretracker.data.model.AppState
import com.epichypernova.scoretracker.data.model.User
import com.epichypernova.scoretracker.ui.components.Avatar
import com.epichypernova.scoretracker.ui.components.SectionLabel
import com.epichypernova.scoretracker.ui.components.AppToggle
import com.epichypernova.scoretracker.ui.theme.Cinzel
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

private val COLOR_OPTIONS = listOf(
    0xFF2FD3F0, 0xFF55E6A5, 0xFFFF6FA8, 0xFFA18AF5, 0xFF3B7BF7, 0xFFF27BA9,
)

@Composable
fun EditPlayerScreen(
    repo: Repository,
    state: AppState,
    userId: String,
    onClose: () -> Unit,
) {
    val existing = state.users.firstOrNull { it.id == userId }
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var color by remember { mutableStateOf(existing?.color ?: COLOR_OPTIONS.first()) }
    var favorite by remember { mutableStateOf(existing?.favorite ?: false) }
    var confirmDelete by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Palette.AppBg)
            .imePadding(),
    ) {
        // Header
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 44.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("✕", color = Palette.TextSecondary, modifier = Modifier.clickable { onClose() }, style = TextStyle(fontSize = 18.sp))
            Text(
                if (existing == null) stringResource(R.string.new_player) else stringResource(R.string.edit_player),
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                color = Palette.TextPrimary,
                style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
            )
            Text(
                stringResource(R.string.save),
                color = if (name.isBlank()) Palette.TextMuted else Palette.Cyan,
                style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                modifier = Modifier.clickable(enabled = name.isNotBlank()) {
                    if (existing == null) {
                        repo.update { AppActions.addUser(it, name, color, favorite) }
                    } else {
                        repo.update { AppActions.updateUser(it, existing.copy(name = name.trim(), color = color, favorite = favorite)) }
                    }
                    onClose()
                },
            )
        }

        Column(
            Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            // Avatar preview
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Avatar(name.ifBlank { "?" }, Color(color), size = 78, fontSize = 34)
            }

            // Name field with cyan underline
            Column {
                SectionLabel(stringResource(R.string.name))
                BasicTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = SpaceGrotesk, fontSize = 19.sp, color = Palette.TextPrimary),
                    cursorBrush = SolidColor(Palette.Cyan),
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                )
                Box(Modifier.fillMaxWidth().height(1.5.dp).padding(top = 6.dp).background(Palette.Cyan))
            }

            // Color selector
            Column {
                SectionLabel(stringResource(R.string.color))
                Row(
                    Modifier.padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    COLOR_OPTIONS.forEach { c ->
                        val active = c == color
                        Box(
                            Modifier.size(46.dp)
                                .then(if (active) Modifier.border(2.dp, Palette.Cyan, CircleShape) else Modifier)
                                .clickable { color = c },
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(Modifier.size(40.dp).clip(CircleShape).background(Color(c)))
                        }
                    }
                }
            }

            // Favorite toggle
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.favorite),
                        color = Palette.TextPrimary,
                        style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 15.sp),
                    )
                    Text(
                        stringResource(R.string.favorite_sub),
                        color = Palette.TextTertiary,
                        style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.5.sp),
                    )
                }
                AppToggle(favorite, { favorite = it })
            }

            if (existing != null) {
                Column {
                    Text(
                        stringResource(R.string.delete_player),
                        color = Palette.PlayerPink,
                        style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp),
                        modifier = Modifier.clickable { confirmDelete = true },
                    )
                    Text(
                        stringResource(R.string.delete_note),
                        color = Palette.TextTertiary,
                        style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.sp),
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
    }

    if (confirmDelete && existing != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            confirmButton = {
                TextButton(onClick = {
                    repo.update { AppActions.deleteUser(it, existing.id) }
                    confirmDelete = false
                    onClose()
                }) { Text(stringResource(R.string.delete), color = Palette.PlayerPink) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel), color = Palette.TextSecondary) }
            },
            title = { Text(stringResource(R.string.delete_confirm_title, existing.name), color = Palette.TextPrimary) },
            text = { Text(stringResource(R.string.delete_confirm_text), color = Palette.TextTertiary) },
            containerColor = Palette.SheetSurface,
        )
    }
}
