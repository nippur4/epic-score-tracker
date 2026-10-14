package com.epichypernova.scoretracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

/** The menu's search box, shared by the list and the mosaic. */
@Composable
fun SearchField(query: String, onQuery: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(999.dp))
            .background(Color(0x0DFFFFFF)).border(1.dp, Palette.CardBorder, RoundedCornerShape(999.dp))
            .padding(start = 16.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("⌕", color = Color(0xFF7387AF), style = TextStyle(fontSize = 15.sp))
        Box(Modifier.weight(1f).padding(start = 10.dp)) {
            if (query.isEmpty()) {
                Text(stringResource(R.string.search_hint), color = Color(0xFF7387AF), style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 14.sp))
            }
            BasicTextField(
                value = query,
                onValueChange = onQuery,
                singleLine = true,
                textStyle = TextStyle(fontFamily = SpaceGrotesk, fontSize = 14.sp, color = Palette.TextPrimary),
                cursorBrush = SolidColor(Palette.Cyan),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (query.isNotEmpty()) {
            Box(
                Modifier.size(38.dp).clip(CircleShape).clickable(onClickLabel = stringResource(R.string.search_clear)) { onQuery("") },
                contentAlignment = Alignment.Center,
            ) {
                Text("✕", color = Palette.TextMuted, style = TextStyle(fontSize = 13.sp))
            }
        }
    }
}
