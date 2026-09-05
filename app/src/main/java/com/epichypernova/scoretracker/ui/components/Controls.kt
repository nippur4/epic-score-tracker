package com.epichypernova.scoretracker.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.ui.theme.Cinzel
import com.epichypernova.scoretracker.ui.theme.CtaBrush
import com.epichypernova.scoretracker.ui.theme.Orbitron
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk
import com.epichypernova.scoretracker.ui.theme.chamferShape

/** Custom switch: track 48×28, knob 22×22. */
@Composable
fun AppToggle(checked: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val knobOffset by animateDpAsState(if (checked) 23.dp else 3.dp, label = "knob")
    Box(
        modifier = modifier
            .size(width = 48.dp, height = 28.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (checked) Palette.Cyan else Color(0x1FFFFFFF))
            .clickable { onChange(!checked) },
    ) {
        Box(
            Modifier
                .padding(top = 3.dp)
                .offset(x = knobOffset)
                .size(22.dp)
                .clip(CircleShape)
                .background(Palette.OnAccent),
        )
    }
}

/** Two-option segmented control inside a pill container. */
@Composable
fun Segmented(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0x47000000))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEachIndexed { i, label ->
            val active = i == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (active) Palette.Cyan else Color.Transparent)
                    .clickable { onSelect(i) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    color = if (active) Palette.OnAccent else Palette.TextSecondary,
                    style = TextStyle(
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                    ),
                )
            }
        }
    }
}

/** Numeric stepper: − value + with the value in Orbitron. */
@Composable
fun Stepper(
    value: Int,
    onDec: () -> Unit,
    onInc: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        StepperButton("−", onDec)
        Text(
            text = value.toString(),
            modifier = Modifier.defaultMinSize(minWidth = 52.dp),
            color = Palette.Cyan,
            textAlign = TextAlign.Center,
            style = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 20.sp),
        )
        StepperButton("+", onInc)
    }
}

@Composable
fun StepperButton(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .border(1.dp, Palette.ButtonBorder, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, color = Palette.TextPrimary, style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 18.sp))
    }
}

/** Primary CTA with chamfered corners and the cyan gradient. */
@Composable
fun ChamferCta(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Int = 56,
    cut: Int = 14,
    fontSize: Int = 16,
) {
    val shape = chamferShape(cut.dp)
    Box(
        modifier = modifier
            .height(height.dp)
            .clip(shape)
            .background(CtaBrush)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.uppercase(),
            color = Palette.OnAccentDeep,
            style = TextStyle(
                fontFamily = Cinzel,
                fontWeight = FontWeight.Black,
                fontSize = fontSize.sp,
                letterSpacing = (fontSize * 0.0875f).sp,
            ),
        )
    }
}

/** Secondary bordered button. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Int = 50,
    filled: Boolean = false,
) {
    Box(
        modifier = modifier
            .height(height.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (filled) Palette.ControlFill else Color.Transparent)
            .border(1.dp, Palette.ButtonBorder, RoundedCornerShape(999.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Palette.TextPrimary,
            style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 14.sp),
        )
    }
}

/** 40dp circular icon button (menu header actions). */
@Composable
fun CircleIconButton(glyph: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Palette.ControlFill)
            .border(1.dp, Palette.CardBorder, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(glyph, color = Palette.TextSecondary, style = TextStyle(fontSize = 17.sp))
    }
}

/** Pill filter chip (history). */
@Composable
fun FilterChip(text: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (active) Palette.Cyan else Color.Transparent)
            .then(if (active) Modifier else Modifier.border(1.dp, Color(0x24FFFFFF), RoundedCornerShape(999.dp)))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(
            text = text,
            color = if (active) Palette.OnAccent else Palette.TextSecondary,
            style = TextStyle(
                fontFamily = SpaceGrotesk,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp,
            ),
        )
    }
}
