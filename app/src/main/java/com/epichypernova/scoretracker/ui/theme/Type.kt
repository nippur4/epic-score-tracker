@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.epichypernova.scoretracker.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.epichypernova.scoretracker.R

private fun wght(weight: Int) = FontVariation.Settings(FontVariation.weight(weight))

/** Cinzel — titles, app name, monograms (700/900). */
val Cinzel = FontFamily(
    Font(R.font.cinzel_variable, FontWeight.Normal, variationSettings = wght(400)),
    Font(R.font.cinzel_variable, FontWeight.SemiBold, variationSettings = wght(600)),
    Font(R.font.cinzel_variable, FontWeight.Bold, variationSettings = wght(700)),
    Font(R.font.cinzel_variable, FontWeight.Black, variationSettings = wght(900)),
)

/** Orbitron — scores and numbers (700/800), tabular. */
val Orbitron = FontFamily(
    Font(R.font.orbitron_variable, FontWeight.Normal, variationSettings = wght(400)),
    Font(R.font.orbitron_variable, FontWeight.Medium, variationSettings = wght(500)),
    Font(R.font.orbitron_variable, FontWeight.Bold, variationSettings = wght(700)),
    Font(R.font.orbitron_variable, FontWeight.ExtraBold, variationSettings = wght(800)),
)

/** Space Grotesk — UI and copy (400–700). */
val SpaceGrotesk = FontFamily(
    Font(R.font.space_grotesk_variable, FontWeight.Normal, variationSettings = wght(400)),
    Font(R.font.space_grotesk_variable, FontWeight.Medium, variationSettings = wght(500)),
    Font(R.font.space_grotesk_variable, FontWeight.SemiBold, variationSettings = wght(600)),
    Font(R.font.space_grotesk_variable, FontWeight.Bold, variationSettings = wght(700)),
)
