package com.epichypernova.scoretracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

enum class AppTab(val glyph: String) {
    JUEGOS("♠"),      // ♠
    HISTORIAL("◷"),   // ◷
    JUGADORES("☺");   // ☺
}

@Composable
fun BottomTabBar(
    selected: AppTab,
    labels: Map<AppTab, String>,
    onSelect: (AppTab) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Palette.AppBgDeep)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        // top hairline border
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0x17FFFFFF)))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 9.dp, bottom = 11.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            AppTab.entries.forEach { tab ->
                val active = tab == selected
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = tab.glyph,
                        color = if (active) Palette.Cyan else Color(0xFF7387AF),
                        style = TextStyle(fontSize = 17.sp),
                    )
                    Text(
                        text = labels[tab] ?: tab.name,
                        color = if (active) Palette.Cyan else Color(0xFF7387AF),
                        style = TextStyle(
                            fontFamily = SpaceGrotesk,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 11.sp,
                        ),
                    )
                }
            }
        }
    }
}
