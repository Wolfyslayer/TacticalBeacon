# Tactical Beacon

**Military-inspired offline GPS navigation for Android**

A fully offline-capable GPS navigation app designed for field use. Drop unlimited pins, navigate to them with adaptive proximity alerts, and track your path — all without a mobile data connection.

---

## Features

### Interactive Map
- OSMDroid-powered map with Standard, Satellite, and Topographic tile sources
- Tap anywhere on the map to drop a pin at that location
- Drop a pin at your current GPS position with one tap
- Offline tile caching — download map areas for use without internet
- Optional coordinate grid overlay
- Breadcrumb trail showing your path history

### Pin Management
- Unlimited pins stored locally in Room database
- Each pin supports:
  - **Custom name** and notes
  - **12 icon types**: Camp, Vehicle, Cache, Hunting Stand, Waypoint, Danger, Objective, Extraction, Medical, Water Source, Food Cache, Observation Post
  - **8 colors**: Olive, Red, Amber, Blue, White, Cyan, Purple, Orange
  - Automatic GPS coordinates and altitude capture
- Edit or delete pins at any time
- Search pins by name, notes, or type

### Proximity Alert System (Core Feature)
When navigating to a pin, the app provides **adaptive feedback** that intensifies as you approach:

| Distance | Alert Interval |
|----------|---------------|
| > 200 m  | Silent |
| 200 m    | Every 10 seconds |
| 100 m    | Every 5 seconds |
| 50 m     | Every 2 seconds |
| 20 m     | Every second |
| 10 m     | Twice per second |
| 5 m      | Rapid (5× per second) |
| < 2 m    | Continuous tone / vibration |

- **Sound mode**: Audible beeps with adjustable volume
- **Silent/Vibrate mode**: Vibration pulses with adjustable strength
- Automatically switches between sound and vibration based on ringer mode

### Compass Navigation
- Large compass rose rendered entirely in Canvas (no images required)
- Bearing arrow pointing directly to the selected pin
- Animated smooth rotation using device rotation vector sensor
- Stats displayed: heading, distance, bearing, GPS accuracy
- Proximity level badge (Far → Near → Close → Very Close → Immediate → Critical → Arrived)

### Import / Export
- **JSON export**: Full pin data including metadata
- **GPX export**: Standard GPX 1.1 format compatible with other GPS tools
- **JSON import**: Restore from Tactical Beacon JSON files
- **GPX import**: Import waypoints from any GPX file

### Settings
- Metric / Imperial units
- GPS update rate: 500ms, 1s, 2s, 5s
- Battery saver mode (balanced accuracy)
- Keep screen awake during navigation
- Grid overlay toggle
- Breadcrumb trail toggle
- Alert volume (0–100%)
- Vibration strength (1–5)
- Customizable proximity thresholds (7 configurable distances)

---

## Technical Architecture

```
TacticalBeacon/
├── app/src/main/kotlin/com/tacticalbeacon/
│   ├── TacticalBeaconApp.kt          # Application class (Hilt)
│   ├── MainActivity.kt               # Entry point, nav graph, permissions
│   ├── data/
│   │   ├── model/Models.kt           # Pin, Breadcrumb, LocationState, etc.
│   │   ├── db/
│   │   │   ├── AppDatabase.kt        # Room database
│   │   │   ├── PinDao.kt             # Pin CRUD
│   │   │   ├── BreadcrumbDao.kt      # Trail management
│   │   │   └── Converters.kt         # Type converters
│   │   └── repository/
│   │       ├── PinRepository.kt      # Pin ops + GPX/JSON export
│   │       ├── BreadcrumbRepository.kt
│   │       └── SettingsRepository.kt # DataStore preferences
│   ├── location/
│   │   ├── LocationManager.kt        # Fused Location Provider
│   │   ├── CompassManager.kt         # Rotation vector sensor
│   │   └── LocationForegroundService.kt  # Background GPS service
│   ├── navigation/
│   │   ├── ProximityAlertManager.kt  # Adaptive beep/vibration engine
│   │   └── NavigationViewModel.kt   # Shared ViewModel
│   ├── ui/
│   │   ├── theme/
│   │   │   ├── Theme.kt              # Dark tactical Material3 theme
│   │   │   └── Typography.kt
│   │   ├── map/
│   │   │   ├── MapScreen.kt          # Main map with OSMDroid
│   │   │   ├── PinEditDialog.kt      # Create/edit pin dialog
│   │   │   ├── PinDetailSheet.kt     # Pin detail bottom sheet
│   │   │   ├── GridOverlay.kt        # Coordinate grid
│   │   │   ├── OfflineMapManager.kt  # Tile download manager
│   │   │   └── OfflineMapDialog.kt   # Download UI
│   │   ├── compass/
│   │   │   └── CompassScreen.kt      # Canvas compass rose
│   │   ├── pins/
│   │   │   ├── PinListScreen.kt      # Searchable pin list
│   │   │   └── PinListViewModel.kt
│   │   └── settings/
│   │       └── SettingsScreen.kt     # All settings
│   ├── di/
│   │   └── AppModule.kt              # Hilt DI module
│   └── utils/
│       └── FileUtils.kt              # Content resolver helpers
```

---

## Build Requirements

| Requirement | Version |
|-------------|---------|
| Android Studio | Hedgehog 2023.1.1+ or newer |
| JDK | 17 |
| Gradle | 8.9 |
| Android Gradle Plugin | 8.5.2 |
| Kotlin | 2.0.21 |
| Min SDK | 31 (Android 12) |
| Target SDK | 35 (Android 15) |

---

## How to Build

### 1. Open in Android Studio

```bash
# Clone or extract the project
cd TacticalBeacon

# Open Android Studio and select "Open an existing project"
# Navigate to the TacticalBeacon directory
```

### 2. Sync Gradle

Android Studio will automatically sync Gradle and download all dependencies on first open. Ensure you have an internet connection for the initial dependency download.

### 3. Build

```bash
# Debug build
./gradlew assembleDebug

# Release build (requires signing config)
./gradlew assembleRelease
```

### 4. Install on Device

```bash
# Install debug APK via ADB
./gradlew installDebug

# Or use Android Studio's Run button
```

---

## Permissions Explained

| Permission | Purpose |
|------------|---------|
| `ACCESS_FINE_LOCATION` | Precise GPS for navigation |
| `ACCESS_COARSE_LOCATION` | Fallback location |
| `ACCESS_BACKGROUND_LOCATION` | Proximity alerts when app is in background |
| `FOREGROUND_SERVICE` | Reliable GPS tracking notification |
| `FOREGROUND_SERVICE_LOCATION` | Required for location foreground service |
| `VIBRATE` | Proximity vibration alerts |
| `WAKE_LOCK` | Keep screen on during navigation |
| `POST_NOTIFICATIONS` | Foreground service notification |
| `INTERNET` | Online tile loading (optional) |

---

## Offline Map Usage

1. Open the app and navigate to your area of interest on the map
2. Tap the map type selector → **Download Offline**
3. Select zoom range (8–16 recommended for field use)
4. Tap **Download** — tiles are cached locally
5. The app will use cached tiles automatically when offline

> **Storage estimate**: A 10km × 10km area at zoom 8–16 requires approximately 50–200 MB.

---

## GPX Compatibility

Exported GPX files are compatible with:
- Garmin devices and BaseCamp
- CalTopo
- Gaia GPS
- AllTrails
- Any GPX 1.1-compatible application

---

## Color Palette

| Color | Hex | Usage |
|-------|-----|-------|
| Matte Black | `#0A0A0A` | Background |
| Dark Surface | `#121212` | Cards, app bar |
| Olive Green | `#6B7C3A` | Primary accent, buttons |
| High Contrast White | `#F5F5F5` | Primary text |
| Alert Red | `#D32F2F` | Danger, north indicator |
| Alert Amber | `#F57F17` | Warnings, GPS accuracy |

---

## License

MIT License — free for personal and commercial use.
