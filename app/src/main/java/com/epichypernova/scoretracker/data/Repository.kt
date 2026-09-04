package com.epichypernova.scoretracker.data

import com.epichypernova.scoretracker.data.model.AppState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Single source of truth for app state. Holds an in-memory [StateFlow] seeded from
 * [ScoreStore] and writes every mutation back to disk so the current game survives
 * the app being closed.
 */
class Repository(
    private val store: ScoreStore,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    init {
        scope.launch {
            val loaded = store.load()
            if (loaded == null) {
                // First run: seed sample data matching the design.
                val seeded = SeedData.initial()
                _state.value = seeded
                store.save(seeded)
            } else {
                _state.value = loaded
            }
        }
    }

    /** Apply a pure transform to the current state and persist the result. */
    fun update(transform: (AppState) -> AppState) {
        val next = transform(_state.value)
        _state.value = next
        scope.launch { store.save(next) }
    }
}
