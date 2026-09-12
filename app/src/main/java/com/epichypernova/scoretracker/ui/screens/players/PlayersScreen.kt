package com.epichypernova.scoretracker.ui.screens.players

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
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
import com.epichypernova.scoretracker.ui.components.AppTab
import com.epichypernova.scoretracker.ui.components.Avatar
import com.epichypernova.scoretracker.ui.components.SectionLabel
import com.epichypernova.scoretracker.ui.components.TabScaffold
import com.epichypernova.scoretracker.ui.components.avatarResId
import com.epichypernova.scoretracker.ui.components.cardSurface
import com.epichypernova.scoretracker.ui.components.dashedBorder
import com.epichypernova.scoretracker.ui.components.showRewardedAd
import com.epichypernova.scoretracker.ui.theme.Cinzel
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

@Composable
fun PlayersScreen(
    repo: Repository,
    state: AppState,
    tabLabels: Map<AppTab, String>,
    onSelectTab: (AppTab) -> Unit,
    onAddPlayer: () -> Unit,
    onEditPlayer: (String) -> Unit,
) {
    val context = LocalContext.current
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

            val locked = (1..30).filter { it !in state.unlockedAvatars }
            if (locked.isNotEmpty()) {
                item {
                    SectionLabel(stringResource(R.string.unlock_avatars_title), color = Palette.Cyan, modifier = Modifier.padding(top = 20.dp))
                    Text(
                        stringResource(R.string.unlock_avatars_sub),
                        color = Palette.TextTertiary,
                        style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.sp),
                        modifier = Modifier.padding(top = 2.dp, bottom = 4.dp),
                    )
                }
                items(locked) { n ->
                    LockedAvatarRow(
                        number = n,
                        progress = state.avatarAdProgress[n] ?: 0,
                        required = AppActions.adsRequiredForAvatar(n),
                        onWatch = { showRewardedAd(context, onReward = { repo.update { AppActions.watchAvatarAd(it, n) } }) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LockedAvatarRow(number: Int, progress: Int, required: Int, onWatch: () -> Unit) {
    val resId = avatarResId(number)
    Row(
        Modifier.fillMaxWidth().cardSurface(16).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(48.dp).clip(CircleShape), contentAlignment = Alignment.Center) {
            if (resId != 0) {
                Image(
                    painter = painterResource(resId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape).alpha(0.35f),
                )
            }
            Text("🔒", style = TextStyle(fontSize = 16.sp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                pluralStringResource(R.plurals.unlock_watch_ads, required, required),
                color = Palette.TextPrimary,
                style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 14.sp),
            )
            if (required > 1) {
                Text(
                    stringResource(R.string.unlock_progress, progress, required),
                    color = Palette.Cyan,
                    style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier.height(38.dp).clip(RoundedCornerShape(999.dp)).background(Palette.Cyan).clickable { onWatch() }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                stringResource(R.string.unlock_watch_button),
                color = Palette.OnAccent,
                style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 13.sp),
            )
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
