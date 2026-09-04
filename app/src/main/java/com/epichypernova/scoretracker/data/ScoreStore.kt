package com.epichypernova.scoretracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.epichypernova.scoretracker.data.model.AppState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "epic_hypernova_state")

/** Persists the whole [AppState] as a single JSON blob in Preferences DataStore. */
class ScoreStore(private val context: Context) {

    private val key = stringPreferencesKey("app_state")
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /** Returns the persisted state, or null on first run (nothing saved yet). */
    suspend fun load(): AppState? {
        val raw = context.dataStore.data.map { it[key] }.first() ?: return null
        return runCatching { json.decodeFromString<AppState>(raw) }.getOrNull()
    }

    suspend fun save(state: AppState) {
        val raw = json.encodeToString(state)
        context.dataStore.edit { it[key] = raw }
    }
}
