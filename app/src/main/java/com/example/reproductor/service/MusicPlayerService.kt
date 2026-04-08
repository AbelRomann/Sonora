package com.example.reproductor.service

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.reproductor.MainActivity
import com.example.reproductor.util.FadingPlayer
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MusicPlayerService : MediaSessionService() {

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var fadingPlayer: FadingPlayer

    private var mediaSession: MediaSession? = null


    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // MediaSession usa FadingPlayer — todas las pausas/plays pasan por AudioFadeManager
        mediaSession = MediaSession.Builder(this, fadingPlayer)
            .setSessionActivity(pendingIntent)
            .setCallback(CustomMediaSessionCallback())
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            exoPlayer.release()
            release()
            mediaSession = null
        }
        isServiceForeground = false
        super.onDestroy()
    }


    // Rastrea si ya estamos en modo foreground
    private var isServiceForeground = false

    /**
     * Al pasar siempre [startInForegroundRequired] = true a super, evitamos que
     * Media3 llame a stopForeground() cuando el player pausa. Esto mantiene la
     * notificación visible (con sus controles originales) sin parpadeo.
     */
    @OptIn(UnstableApi::class)
    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        super.onUpdateNotification(session, true)
        isServiceForeground = true
    }

    // Custom callback to handle double and triple clicks on headset buttons
    private inner class CustomMediaSessionCallback : MediaSession.Callback {
        private var clickCount = 0
        private var clickRunnable: Runnable? = null
        private val handler = android.os.Handler(android.os.Looper.getMainLooper())
        private val CLICK_DELAY = 400L // ms

        override fun onMediaButtonEvent(
            session: MediaSession,
            controllerInfo: MediaSession.ControllerInfo,
            intent: Intent
        ): Boolean {
            val keyEvent = intent.getParcelableExtra<android.view.KeyEvent>(Intent.EXTRA_KEY_EVENT)
            if (keyEvent != null && keyEvent.action == android.view.KeyEvent.ACTION_DOWN) {
                val keyCode = keyEvent.keyCode
                if (keyCode == android.view.KeyEvent.KEYCODE_HEADSETHOOK || keyCode == android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                    clickCount++
                    
                    clickRunnable?.let { handler.removeCallbacks(it) }
                    
                    clickRunnable = Runnable {
                        when (clickCount) {
                            1 -> {
                                // Usar fadingPlayer para que el fade se aplique
                                if (exoPlayer.isPlaying) {
                                    fadingPlayer.pause()
                                } else {
                                    fadingPlayer.play()
                                }
                            }
                            2 -> {
                                exoPlayer.seekToNext()
                            }
                            3 -> {
                                exoPlayer.seekToPrevious()
                            }
                            else -> {
                                if (exoPlayer.isPlaying) {
                                    fadingPlayer.pause()
                                } else {
                                    fadingPlayer.play()
                                }
                            }
                        }
                        clickCount = 0
                    }
                    
                    handler.postDelayed(clickRunnable!!, CLICK_DELAY)
                    return true
                }
            }
            return super.onMediaButtonEvent(session, controllerInfo, intent)
        }
    }
}
