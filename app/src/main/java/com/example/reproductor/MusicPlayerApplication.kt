package com.example.reproductor

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// ---> PASO 1: AÑADE ESTA LÍNEA <---
@HiltAndroidApp
class MusicPlayerApplication : Application() {
    // El cuerpo de la clase puede estar vacío.
}
