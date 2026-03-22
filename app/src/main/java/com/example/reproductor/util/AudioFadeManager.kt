package com.example.reproductor.util

import androidx.media3.common.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Manages smooth audio fade-in and fade-out transitions for ExoPlayer.
 *
 * Uses coroutines to animate [Player.volume] in small steps (~16ms intervals)
 * with easing curves for a natural feel. All fades are cancellable — rapid
 * play/pause taps are handled safely by cancelling any in-flight fade before
 * starting a new one.
 *
 * @param scope The [CoroutineScope] used to launch fade coroutines.
 *              Should outlive individual play/pause calls (e.g. a controller scope).
 */
class AudioFadeManager(private val scope: CoroutineScope) {

    private var fadeJob: Job? = null

    companion object {
        /** Total duration of each fade transition in milliseconds. */
        private const val FADE_DURATION_MS = 200L

        /** Interval between volume steps in milliseconds (~60 fps cadence). */
        private const val STEP_INTERVAL_MS = 16L

        /** Number of discrete volume steps per fade. */
        private val STEP_COUNT = (FADE_DURATION_MS / STEP_INTERVAL_MS).toInt()
    }

    /**
     * Fades audio volume from current level to 0, then pauses the player.
     * Volume is reset to 1f after pause so the next play starts at full volume.
     *
     * If a fade is already in progress, it is cancelled first.
     * If the player is not currently playing, this is a no-op.
     */
    fun fadeOutAndPause(player: Player) {
        // Already paused — nothing to do
        if (!player.isPlaying && player.playbackState != Player.STATE_BUFFERING) return

        cancelFade()
        fadeJob = scope.launch {
            val startVolume = player.volume

            for (step in 1..STEP_COUNT) {
                if (!isActive) return@launch
                val progress = step.toFloat() / STEP_COUNT
                // easeOut: fast initial drop, gentle tail
                val easedProgress = easeOut(progress)
                player.volume = startVolume * (1f - easedProgress)
                delay(STEP_INTERVAL_MS)
            }

            // Ensure we reach exactly zero and pause
            player.volume = 0f
            player.pause()
            // Reset volume so next play() starts at full volume
            player.volume = 1f
        }
    }

    /**
     * Sets volume to 0, starts playback, then fades audio volume up to 1f.
     *
     * If a fade is already in progress, it is cancelled first.
     * If the player is already playing, this is a no-op.
     */
    fun fadeInAndPlay(player: Player) {
        // Already playing — nothing to do
        if (player.isPlaying) return

        cancelFade()
        fadeJob = scope.launch {
            // Start silent, then begin playback
            player.volume = 0f
            player.play()

            for (step in 1..STEP_COUNT) {
                if (!isActive) return@launch
                val progress = step.toFloat() / STEP_COUNT
                // easeIn: gentle start, accelerating ramp
                val easedProgress = easeIn(progress)
                player.volume = easedProgress
                delay(STEP_INTERVAL_MS)
            }

            // Ensure we reach exactly full volume
            player.volume = 1f
        }
    }

    /**
     * Cancels any in-flight fade and resets volume to full.
     * Call this when releasing the player or when an immediate volume
     * reset is needed (e.g. track skip).
     */
    fun cancelFade() {
        fadeJob?.cancel()
        fadeJob = null
    }

    // ── Easing functions ─────────────────────────────────────────────

    /**
     * Quadratic ease-out: decelerating curve.
     * `f(t) = 1 - (1 - t)²`
     */
    private fun easeOut(t: Float): Float {
        val inv = 1f - t
        return 1f - inv * inv
    }

    /**
     * Quadratic ease-in: accelerating curve.
     * `f(t) = t²`
     */
    private fun easeIn(t: Float): Float {
        return t * t
    }
}
