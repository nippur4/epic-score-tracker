package com.epichypernova.scoretracker.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.epichypernova.scoretracker.BuildConfig
import com.epichypernova.scoretracker.R
import com.epichypernova.scoretracker.data.AppActions
import com.epichypernova.scoretracker.data.Repository
import com.epichypernova.scoretracker.data.model.AppState
import com.epichypernova.scoretracker.ui.components.AdsConsent
import com.epichypernova.scoretracker.ui.components.AppTab
import com.epichypernova.scoretracker.ui.components.SecondaryButton
import com.epichypernova.scoretracker.ui.components.Segmented
import com.epichypernova.scoretracker.ui.components.SectionLabel
import com.epichypernova.scoretracker.ui.components.TabScaffold
import com.epichypernova.scoretracker.ui.components.findActivity
import com.epichypernova.scoretracker.ui.theme.Cinzel
import com.epichypernova.scoretracker.ui.theme.Palette
import com.epichypernova.scoretracker.ui.theme.SpaceGrotesk

@Composable
fun SettingsScreen(
    repo: Repository,
    state: AppState,
    tabLabels: Map<AppTab, String>,
    onSelectTab: (AppTab) -> Unit,
) {
    val languages = listOf("es", "en")
    val selected = languages.indexOf(state.settings.language).coerceAtLeast(0)
    val context = LocalContext.current
    val privacyOptionsRequired = remember { AdsConsent.isPrivacyOptionsRequired(context) }
    val privacyPolicyUrl = BuildConfig.PRIVACY_POLICY_URL

    TabScaffold(AppTab.AJUSTES, tabLabels, onSelectTab) { padding ->
        Column(
            Modifier.fillMaxSize().background(Palette.AppBg).padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                stringResource(R.string.settings),
                color = Palette.TextPrimary,
                style = TextStyle(fontFamily = Cinzel, fontWeight = FontWeight.Bold, fontSize = 29.sp, letterSpacing = 1.16.sp),
                modifier = Modifier.padding(top = 22.dp, bottom = 8.dp),
            )

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

            SectionLabel(stringResource(R.string.sound), modifier = Modifier.padding(top = 18.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                com.epichypernova.scoretracker.ui.components.SpeakerIcon(Palette.TextPrimary, 18, waves = 1)
                Slider(
                    value = state.settings.soundVolume,
                    onValueChange = { v -> repo.update { AppActions.setSoundVolume(it, v) } },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = Palette.Cyan,
                        activeTrackColor = Palette.Cyan,
                        inactiveTrackColor = Palette.ButtonBorder,
                    ),
                    modifier = Modifier.weight(1f),
                )
                com.epichypernova.scoretracker.ui.components.SpeakerIcon(Palette.TextPrimary, 18, waves = 2)
            }

            if (privacyOptionsRequired || privacyPolicyUrl.isNotBlank()) {
                SectionLabel(stringResource(R.string.privacy), modifier = Modifier.padding(top = 18.dp))
                if (privacyOptionsRequired) {
                    SecondaryButton(
                        text = stringResource(R.string.privacy_options),
                        onClick = { context.findActivity()?.let { AdsConsent.showPrivacyOptions(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        height = 44,
                    )
                }
                if (privacyPolicyUrl.isNotBlank()) {
                    SecondaryButton(
                        text = stringResource(R.string.privacy_policy),
                        onClick = {
                            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        height = 44,
                    )
                }
            }

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
