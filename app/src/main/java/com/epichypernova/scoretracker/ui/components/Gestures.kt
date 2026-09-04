package com.epichypernova.scoretracker.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.rememberCoroutineScope
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
    pointerInput(enabled) {
        if (!enabled) return@pointerInput
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            onClick()
            val job = scope.launch {
                delay(450)
                var period = 130L
                while (true) {
                    onClick()
                    delay(period)
                    period = (period - 12L).coerceAtLeast(45L)
                }
            }
            waitForUpOrCancellation()
            job.cancel()
        }
    }
}
