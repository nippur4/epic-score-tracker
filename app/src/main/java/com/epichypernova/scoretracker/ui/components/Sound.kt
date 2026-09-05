package com.epichypernova.scoretracker.ui.components

import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * Loads a short sound into a [SoundPool] and returns a low-latency play function.
 * The pool is released when the composable leaves the tree.
 */
@Composable
fun rememberSoundEffect(resId: Int): () -> Unit {
    val context = LocalContext.current
    val pool = remember {
        SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    }
    var soundId by remember { mutableIntStateOf(0) }
    DisposableEffect(resId) {
        val id = pool.load(context, resId, 1)
        soundId = id
        onDispose { pool.release() }
    }
    val vol = LocalSoundVolume.current
    return remember(soundId, vol) { { if (soundId != 0 && vol > 0f) pool.play(soundId, vol, vol, 1, 0, 1f) } }
}
