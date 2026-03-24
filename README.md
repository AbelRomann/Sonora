# 🎵 Reproductor

A modern, dark-themed music player for Android built with Kotlin and Jetpack Compose.

---

## 📱 Features

- **Library scanning** — automatically indexes all audio files on your device
- **Now Playing screen** — full-screen player with album art, progress bar, and playback controls
- **Mini Player** — persistent bottom bar with marquee title, animated progress ring, and palette-adaptive background color
- **Queue management** — drag-and-drop reordering, swipe-to-remove, and clear queue
- **Playlists** — create, rename, delete, and manage playlists; add songs individually or in bulk
- **Artist & Album views** — browse your library grouped by artist or album
- **Most Played section** — highlights the tracks you listen to most (requires ≥ 30 seconds of playback to count)
- **Favorites** — mark songs as favorites and filter by them in the library
- **Search** — instant search across titles, artists, and albums
- **Shuffle & Repeat** — per-queue shuffle and repeat (off / all / one)
- **Audio fade** — smooth fade-in / fade-out on play and pause

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture (domain / data / presentation) |
| Dependency Injection | Hilt |
| Media playback | Media3 (ExoPlayer) + MediaSession |
| Database | Room |
| Image loading | Coil |
| Navigation | Jetpack Navigation Compose |
| Reorderable lists | `sh.calvin.reorderable` |

---

## 🏗 Project Structure

```
app/src/main/java/com/example/reproductor/
├── data/           # Room entities, DAOs, repository implementations
├── di/             # Hilt modules
├── domain/         # Models, repository interfaces, use cases
├── presentation/   # Composable screens, ViewModels, components
│   ├── components/ # MiniPlayer, QueueBottomSheet, SongItem, …
│   ├── screens/    # Home, Library, Player, Playlists, Artists, …
│   └── player/     # MusicPlayerController, PlayerViewModel
├── service/        # MusicPlayerService (foreground)
└── util/           # AudioFadeManager, PaletteUtil, …
```

---

## 🚀 Getting Started

1. Clone the repo
   ```bash
   git clone https://github.com/AbelRomann/Reproductor.git
   ```
2. Open in **Android Studio Hedgehog** or newer
3. Sync Gradle
4. Run on a device or emulator (API 26+)

> The app will ask for audio permission on first launch. Grant it to scan your music library.

---

## 📄 License

This project is for personal / portfolio use.
