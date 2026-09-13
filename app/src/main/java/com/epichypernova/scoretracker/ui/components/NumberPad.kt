@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.epichypernova.scoretracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

/**
 * Reusable numeric-entry bottom sheet: a big amount display, a numpad, optional quick-add
 * chips, and a confirm button. Returns the typed non-negative integer via [onConfirm].
 */
@Composable
fun NumberPadSheet(
    title: String,
    accent: Color,
    confirmLabel: String,
    onConfirm: (Int) -> Unit,
    onClose: () -> Unit,
    subtitle: String? = null,
    quickAdds: List<Int> = emptyList(),
    max: Int = 9_999,
) {
    var amount by remember { mutableIntStateOf(0) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheetState, containerColor = Palette.SheetSurface) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, color = accent, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, letterSpacing = 1.4.sp))
            if (subtitle != null) Text(subtitle, color = Palette.TextTertiary, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.5.sp))
            Text("$amount", color = Palette.CyanNumber, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 40.sp, fontFeatureSettings = "tnum"))

            if (quickAdds.isNotEmpty()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    quickAdds.forEach { q ->
                        Box(
                            Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(999.dp))
                                .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                                .clickable { amount = (amount + q).coerceAtMost(max) },
                            contentAlignment = Alignment.Center,
                        ) { Text("+$q", color = accent, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 13.sp)) }
                    }
                }
            }

            val rows = listOf(listOf("7", "8", "9"), listOf("4", "5", "6"), listOf("1", "2", "3"), listOf("00", "0", "⌫"))
            rows.forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { key ->
                        Box(
                            Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(14.dp)).background(Color(0x14FFFFFF)).clickable {
                                amount = when (key) {
                                    "⌫" -> amount / 10
                                    "00" -> (amount * 100).coerceAtMost(max)
                                    else -> (amount * 10 + key.toInt()).coerceAtMost(max)
                                }
                            },
                            contentAlignment = Alignment.Center,
                        ) { Text(key, color = Palette.TextPrimary, style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 20.sp)) }
                    }
                }
            }

            Box(
                Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(999.dp)).background(accent).clickable { onConfirm(amount); onClose() },
                contentAlignment = Alignment.Center,
            ) { Text(confirmLabel, color = Palette.OnAccent, style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 15.sp)) }
        }
    }
}
