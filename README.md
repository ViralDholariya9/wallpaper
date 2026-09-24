# Parallax 3D Wallpaper - Android App (Kotlin & Jetpack Compose)

A modern, high-performance Android Wallpaper application built with **100% Kotlin**, **Jetpack Compose (Material 3)**, and a custom **3D Parallax Live Wallpaper Engine** utilizing real-time device sensors (Gyroscope & Accelerometer).

Fully optimized and compliant with **Google Play Console 2024–2026 policies** (`targetSdk = 35`, Android 15 Ready, App Bundle `.aab` support, ProGuard/R8 optimizations, and clean permissions).

---

## 🌟 Key Features

- **3D Parallax Live Wallpaper Engine**:
  - Native `ParallaxWallpaperService` rendering multi-layer canvas with dynamic tilt sensitivity.
  - Interactive on-screen 3D preview with gyroscope tracking and low-pass filtering.
- **Set as Wallpaper**:
  - Apply to **Home Screen**, **Lock Screen**, or **Both** instantly using `WallpaperManager`.
  - Direct integration with system Live Wallpaper picker.
- **Safe Gallery Download**:
  - Saves images to `Pictures/ParallaxWallpapers` using modern `MediaStore` API (no invasive permissions required on Android 10+).
- **Curated Wallpaper Categories**:
  - 3D Parallax, AMOLED / Dark, Cyberpunk Neon, Space & Cosmos, Nature, Anime, and Minimal.
- **Favorites & Bookmarks**:
  - Instant local bookmarking for easy access.
- **Play Store Ready**:
  - Preconfigured Proguard / R8 rules (`app/proguard-rules.pro`).
  - Privacy policy and terms dialogs built into `SettingsScreen.kt`.
  - Release signing configuration template ready for `.aab` generation.

---

## 🏗️ Architecture & Tech Stack

- **Language**: Kotlin 2.0+
- **UI Toolkit**: Jetpack Compose with Material 3
- **Dependency Management**: Gradle Kotlin DSL & Version Catalog (`gradle/libs.versions.toml`)
- **Image Loading**: Coil Compose 2.7.0
- **Sensors**: Android `SensorManager` (`TYPE_ROTATION_VECTOR` and `TYPE_ACCELEROMETER`)
- **Target SDK**: 35 (Android 15)
- **Min SDK**: 26 (Android 8.0 Oreo - covering 95%+ of active devices worldwide)

---

## 📁 Project Structure

```
├── app/
│   ├── build.gradle.kts          # App-level build config (targetSdk 35, signing, proguard)
│   ├── proguard-rules.pro        # Production R8/ProGuard shrinking rules
│   └── src/main/
│       ├── AndroidManifest.xml   # Permissions, activities, and ParallaxWallpaperService
│       ├── java/com/parallax/wallpaper/
│       │   ├── ParallaxApp.kt                    # Application class
│       │   ├── MainActivity.kt                   # Edge-to-edge Compose entry & Navigation
│       │   ├── model/
│       │   │   ├── WallpaperItem.kt              # Wallpaper data model
│       │   │   ├── WallpaperLayer.kt             # Multi-layer 3D depth model
│       │   │   └── Category.kt                   # Wallpaper categories
│       │   ├── data/
│       │   │   └── WallpaperRepository.kt        # Repository & sample collections
│       │   ├── sensor/
│       │   │   └── ParallaxSensorManager.kt      # Gyroscope/Accelerometer filter
│       │   ├── service/
│       │   │   └── ParallaxWallpaperService.kt   # System Live Wallpaper service engine
│       │   ├── ui/
│       │   │   ├── theme/                        # Colors, Typography, Material3 Theme
│       │   │   ├── components/                   # ParallaxCard, SetWallpaperBottomSheet
│       │   │   └── screens/                      # HomeScreen, DetailScreen, Favorites, Settings
│       │   └── utils/
│       │       ├── WallpaperHelper.kt            # Set wallpaper & MediaStore gallery saver
│       │       └── LiveWallpaperHelper.kt        # Live wallpaper intent launcher
│       └── res/                                  # Drawables, mipmaps, strings, colors, themes
├── gradle/
│   ├── libs.versions.toml        # Version Catalog
│   └── wrapper/                  # Gradle Wrapper 8.13
├── build.gradle.kts              # Root build script
├── settings.gradle.kts           # Module & repository configuration
├── PLAY_CONSOLE_GUIDE.md         # Gujarati & English Google Play Console publishing guide
└── README.md
```

---

## 🚀 How to Build & Run

### 1. Open in Android Studio
1. Launch **Android Studio**.
2. Click **Open** and select this directory (`d:\kotlin\parralex wallpaper`).
3. Android Studio will automatically sync the Gradle files.

### 2. Run Debug APK on Device or Emulator
```bash
.\gradlew.bat assembleDebug
```

### 3. Generate Release App Bundle (.aab) for Google Play Console
```bash
.\gradlew.bat bundleRelease
```
See [PLAY_CONSOLE_GUIDE.md](PLAY_CONSOLE_GUIDE.md) for full instructions on signing and uploading to the Play Console.
