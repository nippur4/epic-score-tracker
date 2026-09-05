package com.epichypernova.scoretracker.ui.components

import androidx.compose.runtime.compositionLocalOf

/** Current game sound volume (0f..1f), provided at the app root from persisted settings. */
val LocalSoundVolume = compositionLocalOf { 1f }
