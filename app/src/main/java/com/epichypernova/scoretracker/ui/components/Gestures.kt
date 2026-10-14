package com.epichypernova.scoretracker.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Fires [onClick] once on press, then repeats with acceleration while held down —
 * used by the Magic life ± buttons.
 */
fun Modifier.repeatingClickable(
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    val scope = rememberCoroutineScope()
    // pointerInput only re-keys on [enabled], so the handler would otherwise keep calling the
    // lambda from the first composition — stale for callers that capture state in it.
    val click by rememberUpdatedState(onClick)
    pointerInput(enabled) {
        if (!enabled) return@pointerInput
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            click()
            val job = scope.launch {
                delay(450)
                var period = 130L
                while (true) {
                    click()
                    delay(period)
                    period = (period - 12L).coerceAtLeast(45L)
                }
            }
            waitForUpOrCancellation()
            job.cancel()
        }
    }
}
