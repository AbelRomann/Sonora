package com.example.reproductor.domain.model

import androidx.compose.runtime.Immutable

/**
 * Estado de progreso de la reproducción.
 * Separado de [PlayerState] para evitar recomposición completa de la UI del reproductor
 * cada vez que cambia la posición (cada ~1 segundo).
 */
@Immutable
data class PlaybackProgress(
    val currentPosition: Long = 0L,
    val duration: Long = 0L
)
