package com.epichypernova.scoretracker.ui.screens.players

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
    var showAvatars by remember { mutableStateOf(false) }
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

            // One way in, instead of a row of advertising per locked avatar.
            val locked = (1..30).filter { it !in state.unlockedAvatars }
            if (locked.isNotEmpty()) {
                item {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 20.dp).cardSurface(16)
                            .clickable { showAvatars = true }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(42.dp).clip(CircleShape).background(Color(0x1F2FD3F0)), contentAlignment = Alignment.Center) {
                            com.epichypernova.scoretracker.ui.components.LockIcon(Palette.Cyan, 18)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.unlock_avatars_title),
                                color = Palette.TextPrimary,
                                style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 15.5.sp),
                            )
                            Text(
                                stringResource(R.string.unlock_avatars_count, locked.size),
                                color = Palette.TextTertiary,
                                style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.sp),
                            )
                        }
                        Text("›", color = Color(0xFF556792), style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 16.sp))
                    }
                }
            }
        }
    }

    if (showAvatars) {
        AvatarGallery(
            locked = (1..30).filter { it !in state.unlockedAvatars },
            progress = state.avatarAdProgress,
            onWatch = { n -> showRewardedAd(context, onReward = { repo.update { AppActions.watchAvatarAd(it, n) } }) },
            onClose = { showAvatars = false },
        )
    }
}

/** All the locked avatars at once: tap one to watch the ad that unlocks it. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AvatarGallery(
    locked: List<Int>,
    progress: Map<Int, Int>,
    onWatch: (Int) -> Unit,
    onClose: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.done), color = Palette.Cyan) } },
        title = { Text(stringResource(R.string.unlock_avatars_title), color = Palette.TextPrimary) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    stringResource(R.string.unlock_avatars_sub),
                    color = Palette.TextTertiary,
                    style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.5.sp),
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    locked.forEach { n ->
                        val resId = avatarResId(n)
                        val required = AppActions.adsRequiredForAvatar(n)
                        val seen = progress[n] ?: 0
                        Column(
                            Modifier.width(56.dp).clip(RoundedCornerShape(12.dp)).clickable { onWatch(n) }.padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
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
                                com.epichypernova.scoretracker.ui.components.LockIcon(Palette.TextPrimary, 16)
                            }
                            Text(
                                if (required > 1) "$seen/$required" else stringResource(R.string.unlock_one_ad),
                                color = Palette.Cyan,
                                style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 10.sp),
                                modifier = Modifier.padding(top = 3.dp),
                            )
                        }
                    }
                }
            }
        },
        containerColor = Palette.SheetSurface,
    )
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
