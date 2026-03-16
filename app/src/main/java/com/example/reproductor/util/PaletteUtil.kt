package com.example.reproductor.util

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Extracts a dominant/vibrant color from an artwork URI using Android Palette API.
 *
 * Results are cached in an LRU cache (up to 50 entries) keyed by artwork URI string,
 * so repeated lookups for the same song cost nothing. Extraction runs on Dispatchers.IO.
 *
 * Color is darkened towards black (lerp 45%) to ensure legibility of text rendered on top.
 */
object PaletteUtil {

    private val cache = LruCache<String, Color>(50)

    suspend fun extractDominantColor(
        context: Context,
        artworkUri: String?,
        fallback: Color = Color(0xFF1B2238) // SurfaceDark
    ): Color {
        if (artworkUri == null) return fallback

        // Cache hit
        cache.get(artworkUri)?.let { return it }

        return withContext(Dispatchers.IO) {
            try {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(artworkUri)
                    .allowHardware(false) // palette needs software bitmap
                    .size(128, 128)       // small enough to be fast
                    .build()

                val result = loader.execute(request)
                val bitmap = (result as? SuccessResult)?.drawable
                    ?.let { (it as? android.graphics.drawable.BitmapDrawable)?.bitmap }
                    ?: return@withContext fallback

                val palette = Palette.from(bitmap).generate()

                val extracted = palette.let {
                    it.getDominantColor(0) // prefer dominant
                        .takeIf { c -> c != 0 }
                        ?: it.getVibrantColor(0)
                            .takeIf { c -> c != 0 }
                        ?: it.getMutedColor(0)
                            .takeIf { c -> c != 0 }
                        ?: return@withContext fallback
                }

                // Darken toward black by 45% for readability
                val raw = Color(extracted)
                val darkened = Color(
                    red   = (raw.red   * 0.55f).coerceIn(0f, 1f),
                    green = (raw.green * 0.55f).coerceIn(0f, 1f),
                    blue  = (raw.blue  * 0.55f).coerceIn(0f, 1f),
                    alpha = 1f
                )

                cache.put(artworkUri, darkened)
                darkened
            } catch (e: Exception) {
                fallback
            }
        }
    }
}
