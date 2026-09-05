package com.epichypernova.scoretracker.ui.screens.generic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.epichypernova.scoretracker.data.model.GameType
import com.epichypernova.scoretracker.data.model.GenericRules
import com.epichypernova.scoretracker.data.model.User
import com.epichypernova.scoretracker.ui.components.AppToggle
import com.epichypernova.scoretracker.ui.components.Avatar
import com.epichypernova.scoretracker.ui.components.BackHeader
import com.epichypernova.scoretracker.ui.components.ChamferCta
import com.epichypernova.scoretracker.ui.components.SectionLabel
import com.epichypernova.scoretracker.ui.components.Segmented
import com.epichypernova.scoretracker.ui.components.Stepper
import com.epichypernova.scoretracker.ui.components.cardSurface
import com.epichypernova.scoretracker.ui.components.rememberAdGate
import com.epichypernova.scoretracker.ui.components.dashedBorder
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GenericSetupScreen(
    repo: Repository,
    state: AppState,
    gameType: GameType,
    onBack: () -> Unit,
    onStarted: () -> Unit,
) {
    val title = stringResource(if (gameType == GameType.CONTADOR_SIMPLE) R.string.game_contador_title else R.string.game_manos_title)
    val hasHands = gameType == GameType.MANOS_Y_PUNTOS

    val selectedIds = remember {
        state.users.filter { it.favorite }.map { it.id }.toMutableStateList()
    }
    var targetScore by remember { mutableStateOf(100) }
    var lowWins by remember { mutableStateOf(true) }
    var fixedHandsOn by remember { mutableStateOf(false) }
    var fixedHands by remember { mutableStateOf(8) }
    var bidsEnabled by remember { mutableStateOf(false) }
    var pointsPerHit by remember { mutableStateOf(1) }
    var saveConfig by remember { mutableStateOf(false) }
    var configName by remember { mutableStateOf("") }
    var showAdd by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    val selectedUsers = selectedIds.mapNotNull { id -> state.users.firstOrNull { it.id == id } }
    val canStart = selectedIds.size >= 2 && (!saveConfig || configName.isNotBlank())
    val adGate = rememberAdGate(repo, state)

    Column(Modifier.fillMaxSize().background(Palette.AppBg)) {
        BackHeader(title = title, onBack = onBack, overtitle = stringResource(R.string.new_game))

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Box(Modifier.height(4.dp))

            // Players
            Column {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SectionLabel(stringResource(R.string.players_label))
                    Text(
                        stringResource(R.string.players_count, selectedIds.size),
                        color = Palette.TextTertiary,
                        style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.sp),
                    )
                }
                FlowRow(
                    Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    selectedUsers.forEach { u ->
                        PlayerChip(u, onRemove = { selectedIds.remove(u.id) })
                    }
                    AddChip(onClick = { showAdd = true })
                }
            }

            // Rules
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionLabel(stringResource(R.string.rules))
                Row(
                    Modifier.fillMaxWidth().cardSurface(16).padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.target_score), color = Palette.TextPrimary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 15.sp))
                    Stepper(targetScore, onDec = { targetScore = (targetScore - 5).coerceAtLeast(5) }, onInc = { targetScore += 5 })
                }
                Row(
                    Modifier.fillMaxWidth().cardSurface(16).padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.wins), color = Palette.TextPrimary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 15.sp))
                    Segmented(
                        options = listOf(stringResource(R.string.wins_high), stringResource(R.string.wins_low)),
                        selectedIndex = if (lowWins) 1 else 0,
                        onSelect = { lowWins = it == 1 },
                        modifier = Modifier.width(180.dp),
                    )
                }
                if (hasHands) {
                    ToggleRow(
                        title = stringResource(R.string.fixed_hands),
                        subtitle = if (fixedHandsOn) stringResource(R.string.fixed_hands_on, fixedHands) else stringResource(R.string.fixed_hands_off),
                        checked = fixedHandsOn,
                        onChange = { fixedHandsOn = it },
                        trailing = if (fixedHandsOn) ({
                            Stepper(fixedHands, onDec = { fixedHands = (fixedHands - 1).coerceAtLeast(1) }, onInc = { fixedHands += 1 })
                        }) else null,
                    )
                    ToggleRow(
                        title = stringResource(R.string.bids),
                        subtitle = stringResource(R.string.bids_sub),
                        checked = bidsEnabled,
                        onChange = { bidsEnabled = it },
                    )
                    if (bidsEnabled) {
                        Row(
                            Modifier.fillMaxWidth().cardSurface(16).padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(stringResource(R.string.points_per_hit), color = Palette.TextPrimary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 15.sp))
                            Stepper(pointsPerHit, onDec = { pointsPerHit = (pointsPerHit - 1).coerceAtLeast(1) }, onInc = { pointsPerHit += 1 })
                        }
                    }
                }
            }

            // Save config
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionLabel(stringResource(R.string.save))
                ToggleRow(
                    title = stringResource(R.string.save_config),
                    subtitle = stringResource(R.string.save_config_sub),
                    checked = saveConfig,
                    onChange = { saveConfig = it },
                )
                if (saveConfig) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            stringResource(R.string.config_name_label),
                            color = Palette.TextTertiary,
                            style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 12.5.sp),
                        )
                        Row(
                            Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Palette.ControlFill)
                                .border(1.5.dp, if (configName.isBlank()) Palette.PlayerPink else Palette.Cyan, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(Modifier.weight(1f)) {
                                if (configName.isEmpty()) {
                                    Text(stringResource(R.string.config_name_hint), color = Palette.TextMuted, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 15.sp))
                                }
                                BasicTextField(
                                    value = configName,
                                    onValueChange = { if (it.length <= 32) configName = it },
                                    singleLine = true,
                                    textStyle = TextStyle(fontFamily = SpaceGrotesk, fontSize = 15.sp, color = Palette.TextPrimary),
                                    cursorBrush = SolidColor(Palette.Cyan),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                            Text("${configName.length}/32", color = Palette.TextMuted, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.sp))
                        }
                    }
                }
            }

            Box(Modifier.height(8.dp))
        }

        // Sticky CTA
        Box(Modifier.fillMaxWidth().background(Palette.AppBg).navigationBarsPadding().padding(20.dp)) {
            ChamferCta(
                text = stringResource(R.string.start_game),
                onClick = {
                    if (!canStart) return@ChamferCta
                    val rules = GenericRules(
                        targetScore = targetScore,
                        lowWins = lowWins,
                        fixedHands = if (hasHands && fixedHandsOn) fixedHands else null,
                        bidsEnabled = hasHands && bidsEnabled,
                        pointsPerHit = pointsPerHit.coerceAtLeast(1),
                    )
                    adGate {
                        repo.update { s ->
                            var next = AppActions.startGeneric(s, gameType, if (saveConfig) configName.ifBlank { title } else title, selectedIds.toList(), rules)
                            if (saveConfig && configName.isNotBlank()) {
                                next = AppActions.saveConfig(next, configName, gameType, selectedIds.toList(), rules)
                            }
                            next
                        }
                        onStarted()
                    }
                },
                modifier = Modifier.fillMaxWidth().alpha(if (canStart) 1f else 0.4f),
            )
        }
    }

    if (showAdd) {
        val available = state.users.filterNot { it.id in selectedIds }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            confirmButton = { TextButton(onClick = { showAdd = false }) { Text(stringResource(R.string.done), color = Palette.Cyan) } },
            title = { Text(stringResource(R.string.add_player_title), color = Palette.TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    available.forEach { u ->
                        Row(
                            Modifier.fillMaxWidth().clickable { selectedIds.add(u.id); showAdd = false }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Avatar(u.name, Color(u.color), size = 32, fontSize = 15, avatarId = u.avatarId)
                            Text(u.name, color = Palette.TextPrimary, modifier = Modifier.padding(start = 12.dp))
                        }
                    }

                    // Create a new player without leaving this screen
                    Text(
                        stringResource(R.string.or_create_new),
                        color = Palette.TextTertiary,
                        style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.2.sp),
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
                    )
                    Row(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Palette.ControlFill)
                            .border(1.dp, Palette.CardBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.weight(1f)) {
                            if (newName.isEmpty()) {
                                Text(stringResource(R.string.new_player_name_hint), color = Palette.TextMuted, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 14.sp))
                            }
                            BasicTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                singleLine = true,
                                textStyle = TextStyle(fontFamily = SpaceGrotesk, fontSize = 14.sp, color = Palette.TextPrimary),
                                cursorBrush = SolidColor(Palette.Cyan),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    Box(
                        Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(999.dp))
                            .background(Palette.Cyan).alpha(if (newName.isBlank()) 0.4f else 1f)
                            .clickable(enabled = newName.isNotBlank()) {
                                val color = NEW_PLAYER_COLORS[state.users.size % NEW_PLAYER_COLORS.size]
                                val user = User(AppActions.newId("u"), newName.trim(), color)
                                repo.update { AppActions.addExistingUser(it, user) }
                                selectedIds.add(user.id)
                                newName = ""
                                showAdd = false
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(stringResource(R.string.create_and_add), color = Palette.OnAccent, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 14.sp))
                    }
                }
            },
            containerColor = Palette.SheetSurface,
        )
    }
}

private val NEW_PLAYER_COLORS = listOf(
    0xFF2FD3F0, 0xFF55E6A5, 0xFFFF6FA8, 0xFFA18AF5, 0xFF3B7BF7, 0xFFF27BA9,
)

@Composable
private fun PlayerChip(user: User, onRemove: () -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Palette.ControlFill)
            .border(1.dp, Color(0x1CFFFFFF), RoundedCornerShape(999.dp))
            .padding(start = 6.dp, top = 6.dp, bottom = 6.dp, end = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Avatar(user.name, Color(user.color), size = 27, fontSize = 12, avatarId = user.avatarId)
        Text(user.name, color = Palette.TextPrimary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 14.sp))
        Text("✕", color = Palette.TextMuted, modifier = Modifier.clickable { onRemove() }, style = TextStyle(fontSize = 13.sp))
    }
}

@Composable
private fun AddChip(onClick: () -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .dashedBorder(Color(0x732FD3F0), radius = 999)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.add_player_chip), color = Palette.Cyan, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp))
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    Column(Modifier.fillMaxWidth().cardSurface(16).padding(horizontal = 14.dp, vertical = 12.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, color = Palette.TextPrimary, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 15.sp))
                Text(subtitle, color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.5.sp))
            }
            AppToggle(checked, onChange)
        }
        if (trailing != null) {
            Box(Modifier.padding(top = 10.dp).align(Alignment.End)) { trailing() }
        }
    }
}
