package com.example.reproductor.util

import androidx.media3.common.ForwardingPlayer
import androidx.media3.exoplayer.ExoPlayer

/**
 * A [ForwardingPlayer] wrapper around [ExoPlayer] that routes every
 * [pause] / [play] call through [AudioFadeManager], regardless of the
 * caller (UI, notification, headphone button, MediaSession command, etc.).
 *
 * Because [MediaSession] holds a reference to this player, all commands
 * that arrive via MediaSession are automatically faded — no need to
 * intercept them separately in [MediaSession.Callback].
 */
class FadingPlayer(
    private val exoPlayer: ExoPlayer,
    private val fadeManager: AudioFadeManager
) : ForwardingPlayer(exoPlayer) {

    override fun play() {
        fadeManager.fadeInAndPlay(exoPlayer)
    }

    override fun pause() {
        fadeManager.fadeOutAndPause(exoPlayer)
    }
}
