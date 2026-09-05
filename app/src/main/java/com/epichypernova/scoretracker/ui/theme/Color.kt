package com.epichypernova.scoretracker.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Design tokens — colors. Values taken verbatim from the Epic Hypernova design handoff
 * (README §"Design Tokens"). Kept as plain top-level vals so they read the same as the spec.
 */
object Palette {
    // Base surfaces
    val AppBg = Color(0xFF0C1730)
    val AppBgDeep = Color(0xFF0A1327)      // deep bg / bars
    val SheetSurface = Color(0xFF142547)   // elevated surface (bottom sheet)
    val TrucoSheet = Color(0xFF142547)

    // Card / control fills (semi-transparent over AppBg)
    val CardSurface = Color(0x0BFFFFFF)     // rgba(255,255,255,0.045)
    val RowSurface = Color(0x09FFFFFF)      // rgba(255,255,255,0.035)
    val ControlFill = Color(0x12FFFFFF)     // rgba(255,255,255,0.07)

    // Borders / dividers
    val CardBorder = Color(0x1AFFFFFF)      // rgba(255,255,255,0.10)
    val RowBorder = Color(0x12FFFFFF)       // rgba(255,255,255,0.07)
    val ButtonBorder = Color(0x29FFFFFF)    // rgba(255,255,255,0.16)
    val Divider = Color(0x17FFFFFF)         // rgba(255,255,255,0.09)
    val BevelTop = Color(0x1AFFFFFF)        // inset 0 1px 0 rgba(255,255,255,0.10)

    // Text
    val TextPrimary = Color(0xFFEAF1FF)
    val TextSecondary = Color(0xFFB9C8E8)
    val TextTertiary = Color(0xFF8497BC)
    val TextMuted = Color(0xFF6C7EA3)
    val TextFaint = Color(0xFF556792)
    val OnAccent = Color(0xFF0B1730)
    val OnAccentDeep = Color(0xFF04121C)

    // Accents (from the logo)
    val Cyan = Color(0xFF2FD3F0)
    val CyanBright = Color(0xFF6BE4FA)
    val CyanDeep = Color(0xFF14A8CC)
    val CyanNumber = Color(0xFFCFF6FF)      // light cyan for numbers
    val Magenta = Color(0xFFE24BD6)
    val MagentaNumber = Color(0xFFFFD9F6)
    val Mint = Color(0xFF55E6A5)            // leader / winner / poison

    // Player colors
    val PlayerCyan = Color(0xFF2FD3F0)
    val PlayerViolet = Color(0xFFA18AF5)
    val PlayerMint = Color(0xFF55E6A5)
    val PlayerPink = Color(0xFFFF6FA8)
    val PlayerBlue = Color(0xFF3B7BF7)
    val PlayerRose = Color(0xFFF27BA9)

    val PlayerColors = listOf(
        PlayerCyan, PlayerMint, PlayerPink, PlayerViolet, PlayerBlue, PlayerRose
    )

    // Game colors
    val GameMagic = Color(0xFFA18AF5)
    val GamePokemon = Color(0xFF55E6A5)
    val GameYugioh = Color(0xFFFF6FA8)
    val GameDigimon = Color(0xFF3B7BF7)
    val GameGeneric = Color(0xFF2FD3F0)     // truco / genéricos

    // Truco palettes
    val TrucoUs = Cyan
    val TrucoThem = Magenta
    val PorotoStick = Color(0xFFEAF1FF)
    val PorotoEmpty = Color(0xFF465C86)
}

/** CTA gradient: linear-gradient(180deg, #6BE4FA 0%, #2FD3F0 55%, #14A8CC 100%) */
val CtaBrush: Brush
    get() = Brush.verticalGradient(
        0f to Palette.CyanBright,
        0.55f to Palette.Cyan,
        1f to Palette.CyanDeep,
    )
