package com.epichypernova.scoretracker.ui.screens.players

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.data.model.AppState
import com.epichypernova.scoretracker.data.model.User
import com.epichypernova.scoretracker.ui.components.AppTab
import com.epichypernova.scoretracker.ui.components.Avatar
import com.epichypernova.scoretracker.ui.components.SectionLabel
import com.epichypernova.scoretracker.ui.components.TabScaffold
import com.epichypernova.scoretracker.ui.components.cardSurface
import com.epichypernova.scoretracker.ui.components.dashedBorder
import com.epichypernova.scoretracker.ui.theme.Cinzel
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

@Composable
fun PlayersScreen(
    state: AppState,
    tabLabels: Map<AppTab, String>,
    onSelectTab: (AppTab) -> Unit,
    onAddPlayer: () -> Unit,
    onEditPlayer: (String) -> Unit,
) {
    val ordered = state.users.sortedWith(
        compareByDescending<User> { it.favorite }.thenBy { it.name.lowercase() }
    )
    TabScaffold(AppTab.JUGADORES, tabLabels, onSelectTab) { padding ->
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
                SectionLabel(stringResource(R.string.players_saved_here), color = Palette.Cyan)
                Text(
                    stringResource(R.string.players_title),
                    color = Palette.TextPrimary,
                    style = TextStyle(fontFamily = Cinzel, fontWeight = FontWeight.Bold, fontSize = 29.sp, letterSpacing = 1.16.sp),
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                )
            }
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .dashedBorder(Color(0x732FD3F0), radius = 16)
                        .clickable { onAddPlayer() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        stringResource(R.string.add_player_button),
                        color = Palette.Cyan,
                        style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                    )
                }
            }
            items(ordered) { user ->
                PlayerCard(user, onClick = { onEditPlayer(user.id) })
            }
        }
    }
}

@Composable
private fun PlayerCard(user: User, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .cardSurface(16)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(user.name, Color(user.color), size = 42, fontSize = 20, avatarId = user.avatarId)
        androidx.compose.foundation.layout.Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    user.name,
                    color = Palette.TextPrimary,
                    style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 15.5.sp),
                )
                if (user.favorite) {
                    Text(
                        "  ★",
                        color = Palette.Cyan,
                        style = TextStyle(fontSize = 12.sp),
                    )
                }
            }
            Text(
                stringResource(R.string.games_played, user.gamesPlayed),
                color = Palette.TextTertiary,
                style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.sp),
            )
        }
        Text("✎", color = Palette.TextMuted, style = TextStyle(fontSize = 15.sp))
    }
}
