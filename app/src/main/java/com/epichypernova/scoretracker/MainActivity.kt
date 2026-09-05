package com.epichypernova.scoretracker

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.epichypernova.scoretracker.nav.AppNavGraph
import com.epichypernova.scoretracker.ui.components.LocalSoundVolume
import com.epichypernova.scoretracker.ui.theme.EpicHypernovaTheme
import com.epichypernova.scoretracker.ui.theme.Palette
import androidx.compose.foundation.layout.Box

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { AppRoot() }
    }
}

@Composable
private fun AppRoot() {
    val state by ServiceLocator.repository.state.collectAsState()
    EpicHypernovaTheme {
        CompositionLocalProvider(LocalSoundVolume provides state.settings.soundVolume) {
            Box(Modifier.fillMaxSize().background(Palette.AppBg)) {
                AppNavGraph(repo = ServiceLocator.repository)
            }
        }
    }
}
