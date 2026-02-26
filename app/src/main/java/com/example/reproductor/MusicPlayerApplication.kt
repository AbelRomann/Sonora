package com.example.reproductor

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MusicPlayerApplication : Application(), ImageLoaderFactory {
    
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // Usa hasta el 25% de la memoria disponible para las portadas
                    .strongReferencesEnabled(true)
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.04) // Máximo 4% del espacio libre en disco para portadas cacheadas
                    .build()
            }
            .crossfade(true) // Activa animaciones suaves al cargar las imágenes
            .respectCacheHeaders(false) // Ignora cabeceras HTTP buscando en caché local siempre que sea posible
            .build()
    }
}
