# MediaDownloader

Android media downloader app powered by yt-dlp. Supports YouTube, Instagram, TikTok, Twitter/X, Facebook, and 1000+ sites.

## Stack

- Kotlin + Jetpack Compose Material 3
- MVVM + Clean Architecture
- Hilt (DI), Room (DB), WorkManager, Coroutines + Flow
- SAF + FileProvider for storage
- yt-dlp real binary execution via ProcessBuilder

## Project Structure

```
app/src/main/java/com/mediadownloader/
├── core/           – Dispatchers, Result types, Error types, executors, BinaryInstaller
├── data/           – Room DB, DAOs, entities, yt-dlp source, repository impls
├── domain/         – Models, repository interfaces, use cases
├── presentation/   – ViewModels, UI screens (Compose), navigation, state
├── service/        – DownloadWorker (WorkManager), NotificationHelper
├── di/             – Hilt modules
├── receiver/       – BroadcastReceiver for notification actions
└── util/           – Constants, FileHelper, ProgressParser, Extensions
```

## yt-dlp Binary Setup (REQUIRED)

The app ships a placeholder `app/src/main/assets/yt-dlp`.  
**Replace it with the real ARM64 Android binary before building.**

1. Download the latest Android ARM64 build from:  
   https://github.com/yt-dlp/yt-dlp/releases/latest  
   → `yt-dlp_android` (no FFmpeg) **or** `yt-dlp_android_aarch64`

2. Copy it to:
   ```
   app/src/main/assets/yt-dlp
   ```

3. The `BinaryInstaller` copies it to `filesDir/bin/yt-dlp`, applies `chmod 755`, and verifies it is executable before each use.

## Build

```bash
# Debug
./gradlew assembleDebug

# Release (uses debug signing)
./gradlew assembleRelease
```

Requires JDK 17 and Android SDK with API 34.

## CI/CD

GitHub Actions workflow at `.github/workflows/build.yml` runs on push/PR:
- Builds `app-debug.apk` and `app-release.apk`
- Uploads both as workflow artifacts

## Permissions

| Permission | Reason |
|---|---|
| `INTERNET` | Download media |
| `FOREGROUND_SERVICE` | WorkManager foreground service |
| `POST_NOTIFICATIONS` | Download progress notifications |
| `READ/WRITE_EXTERNAL_STORAGE` | Legacy storage access (≤ API 32) |

## Architecture

```
UI (Compose) → ViewModel → UseCase → Repository Interface
                                          ↓
                                    RepositoryImpl
                                          ↓
                               Room DAO / YtDlpManager
                                          ↓
                                    ProcessBuilder
                                          ↓
                                   yt-dlp binary (assets)
```

WorkManager → DownloadWorker → YtDlpManager → real-time stdout parsing
