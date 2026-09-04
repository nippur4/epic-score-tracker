package com.epichypernova.scoretracker.ui.screens.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.data.AppActions
import com.epichypernova.scoretracker.data.Repository
import com.epichypernova.scoretracker.data.model.AppState
import com.epichypernova.scoretracker.ui.components.BackHeader
import com.epichypernova.scoretracker.ui.components.Segmented
import com.epichypernova.scoretracker.ui.components.SectionLabel
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

@Composable
fun SettingsScreen(
    repo: Repository,
    state: AppState,
    onBack: () -> Unit,
) {
    val languages = listOf("es", "en")
    val selected = languages.indexOf(state.settings.language).coerceAtLeast(0)

    Column(Modifier.fillMaxSize().background(Palette.AppBg)) {
        BackHeader(title = stringResource(R.string.settings), onBack = onBack)

        Column(
            Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SectionLabel(stringResource(R.string.language))
            Segmented(
                options = listOf(stringResource(R.string.language_es), stringResource(R.string.language_en)),
                selectedIndex = selected,
                onSelect = { i ->
                    val lang = languages[i]
                    repo.update { AppActions.setLanguage(it, lang) }
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang))
                },
            )

            Text(
                stringResource(R.string.about_version),
                color = Palette.TextTertiary,
                style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.sp),
                modifier = Modifier.padding(top = 20.dp),
            )
            Text(
                stringResource(R.string.about_local),
                color = Palette.TextMuted,
                style = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.sp),
            )
        }
    }
}
