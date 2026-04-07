# Semana 3 — Lyrics local + pequeños ajustes UX (plan de implementación)

**Fecha:** 7 de abril de 2026

Este documento describe cómo implementar letras locales (LRC/embedded) y mejoras UX pequeñas sobre la base actual del proyecto.

---

## Objetivo de semana

1. Mostrar letras sincronizadas (si existen) para cada canción.
2. Soportar fallback a letra estática cuando no haya timestamps.
3. Mejorar UX en Player/Library con cambios de bajo riesgo y alto impacto.

---

## 1) Letras locales — alcance funcional

### Formatos objetivo (v1)

- **`.lrc` externo** junto al archivo de audio, mismo nombre base.
  - Ejemplo: `Cancion.mp3` + `Cancion.lrc`
- **Letras incrustadas** en metadata (si el parser las expone).

### Comportamiento esperado

- Si hay LRC con timestamps, mostrar línea activa según `playbackProgress.currentPosition`.
- Si hay letra sin timestamps, mostrar bloque completo desplazable.
- Si no hay letra, mostrar estado vacío (“No se encontró letra local”).

---

## 2) Diseño técnico sugerido

### 2.1 Dominio / modelos

Crear modelo:

```kotlin
data class LyricsLine(
  val timeMs: Long?,
  val text: String
)

data class SongLyrics(
  val songId: Long,
  val source: LyricsSource,
  val lines: List<LyricsLine>
)

enum class LyricsSource { LRC_FILE, EMBEDDED_TAG, NONE }
```

### 2.2 Data layer

Agregar `LyricsDataSource`:

- `loadLyricsForSong(song: Song): SongLyrics`
- Resolver ruta del `.lrc` por nombre base.
- Parsear timestamps tipo `[mm:ss.xx]` y variantes.

Casos a cubrir en parser:

- Multiples timestamps por línea: `[00:10.00][00:20.00]Letra`
- Líneas metadata LRC (`[ar:]`, `[ti:]`) ignoradas
- Líneas vacías o malformadas

### 2.3 Repository / UseCase

En `MusicRepository`:

- `suspend fun getLyrics(song: Song): SongLyrics`

UseCase:

- `GetLyricsForSongUseCase(song)`

### 2.4 Presentación

En `PlayerViewModel`:

- `lyricsState: StateFlow<LyricsUiState>`
- `fun loadLyrics(song: Song?)`

`LyricsUiState`:

- `Loading`
- `Synced(lines)`
- `PlainText(lines)`
- `Empty`
- `Error(message)`

En `PlayerScreen`:

- Pestaña o sección “Letras” debajo del arte.
- Scroll automático suave a la línea activa (solo modo synced).
- Toggle “auto-scroll” en UI para evitar saltos si usuario está leyendo manualmente.

---

## 3) Algoritmo de sincronización

Dado `currentPositionMs`:

1. Buscar índice de la última línea con `timeMs <= currentPositionMs`.
2. Marcarla activa.
3. Si cambia de índice, animar highlight + scroll.

Complejidad recomendada:

- Búsqueda binaria por timestamp (`O(log n)`) para listas largas.

---

## 4) Ajustes UX pequeños (alto impacto)

### Player

- Botón “Letras” visible en `PlayerScreen`.
- Microfeedback háptico al cambiar preset EQ / activar Sleep Timer.
- Mensaje breve cuando no hay letra local.

### Library

- Empty-state con CTA (“Desliza para recargar” / “Reintentar”).
- Evitar truncar errores largos: mostrar texto resumido y opción “Ver detalle”.

### Permisos

- En pantalla de permiso, explicar beneficios en 1 línea + estado detectado (denegado/permanente).

---

## 5) Plan por días (Semana 3)

- **Día 1:** Parser LRC + tests unitarios parser.
- **Día 2:** DataSource + UseCase + wiring con ViewModel.
- **Día 3:** UI básica de letras (synced/plain/empty).
- **Día 4:** Auto-scroll + highlight + mejoras UX pequeñas.
- **Día 5:** QA manual (distintos archivos, formatos, duraciones) + fixes.

---

## 6) Definición de listo (DoD)

- Reproduce y muestra letras sincronizadas en al menos 3 canciones con LRC válido.
- Fallback correcto para letra plana y ausencia de letra.
- Sin crasheos con archivos malformados.
- UX consistente en tema oscuro y estados de error.

---

## 7) Riesgos y mitigaciones

- **Riesgo:** diversidad de formatos LRC.
  - **Mitigación:** parser tolerante + tests con fixtures reales.
- **Riesgo:** scroll automático molesto.
  - **Mitigación:** toggle de auto-scroll y pausa cuando el usuario toca la lista.
- **Riesgo:** impacto en recomposición.
  - **Mitigación:** calcular línea activa en ViewModel y emitir solo cambios de índice.

