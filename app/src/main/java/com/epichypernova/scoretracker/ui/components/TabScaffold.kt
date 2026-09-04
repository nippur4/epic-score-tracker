package com.epichypernova.scoretracker.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.epichypernova.scoretracker.ui.theme.Palette

/** Scaffold with the fixed bottom tab bar, used by the three main tab screens. */
@Composable
fun TabScaffold(
    selected: AppTab,
    tabLabels: Map<AppTab, String>,
    onSelectTab: (AppTab) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        bottomBar = {
            BottomTabBar(selected = selected, labels = tabLabels, onSelect = onSelectTab)
        },
    ) { padding ->
        content(padding)
    }
}
