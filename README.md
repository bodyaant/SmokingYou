<div align="center">
  <img src="fastlane/metadata/android/en-US/images/featureGraphic.png" alt="SmokingYou Showcase" width="100%">

  # SmokingYou

  Minimalist, data-oriented smoking tracker for Android based on Material 3 Expressive.

  <p align="center">
    <a href="https://github.com/bodyaant/SmokingYou/releases/latest">
      <img src="https://img.shields.io/github/v/release/bodyaant/SmokingYou?logo=github&labelColor=1a1a1a&color=6750A4&style=flat-square" alt="Latest Release">
    </a>
    <a href="https://github.com/bodyaant/SmokingYou/releases">
      <img src="https://img.shields.io/github/downloads/bodyaant/SmokingYou/total?logo=github&labelColor=1a1a1a&color=625B71&style=flat-square" alt="Total Downloads">
    </a>
    <a href="https://github.com/bodyaant/SmokingYou/stargazers">
      <img src="https://img.shields.io/github/stars/bodyaant/SmokingYou?logo=github&labelColor=1a1a1a&color=E0B6FF&style=flat-square" alt="Stars">
    </a>
    <a href="LICENSE">
      <img src="https://img.shields.io/github/license/bodyaant/SmokingYou?logo=gnu&labelColor=1a1a1a&color=blue&style=flat-square" alt="License">
    </a>
    <img src="https://img.shields.io/badge/Android-8.0%2B_(API_26%2B)-3DDC84?logo=android&logoColor=white&labelColor=1a1a1a&style=flat-square" alt="Android 8.0+">
  </p>

  <p align="center">
    <a href="https://github.com/bodyaant/SmokingYou/releases/latest">
      <img src="https://img.shields.io/github/v/release/bodyaant/SmokingYou?label=Download%20SmokingYou&style=for-the-badge&color=6750A4&logo=android&logoColor=white" alt="Download SmokingYou">
    </a>
  </p>
  <p align="center">
    <a href="https://boosty.to/bodyaant">
      <img src="https://img.shields.io/badge/Boosty-Support_the_author-ff6f61?logo=boosty&logoColor=white" width="200">
    </a>
  </p>
</div>

---

SmokingYou is designed to help you track and manage your smoking habits. By offering clear statistics, dynamic charts, home screen widgets, and motivational achievements, the app provides the insights needed to cut down or quit entirely.

## Screenshots

<p align="center">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="33%"><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="33%"><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width="33%">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" width="33%"><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/5.png" width="33%"><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/6.png" width="33%">
</p>

## Features

- **Habit Tracking:** Log entries with a single tap. A real-time timer displays the duration elapsed since your last cigarette.
- **Home Screen Widgets:**
  - **Quick Add Widget (1x1):** One-tap instant cigarette logging from your home screen.
  - **Timer & Counter Widget (3x1):** Displays live smoke-free timer, daily cigarette counter, and quick add action.
  - Includes widget placement setup with Android 12+ dynamic widget pinning support.
- **Mindful Craving Pause:** Take a mindful pause with breathing guidance to resist cravings and track successfully resisted attempts.
- **Tapering Reduction Plan:** Set gradual reduction goals, track daily limits, and complete check-ins to cut down smoking at your own pace.
- **Historical Baseline Generator:** Analyze past smoking habits, compute baseline statistics, and track projected money and health savings.
- **Detailed Analytics & History:** Interactive daily/weekly charts, craving trigger distributions (including alcohol, stress, coffee, etc.), and editable logs.
- **WHO Health & Financial Stats:** Track total count, averages, daily extremes, longest smoke-free streak, financial savings, and WHO health recovery milestones.
- **Achievements System:** Unlock milestone badges and secret achievements for consistency and smoke-free intervals with local notifications.
- **Data Portability:** Local backup and restore (JSON export/import) to secure your logs.
- **Highly Customizable Themes:**
  - Standard Light, Dark, and System themes.
  - **AMOLED Dark Mode** for extra power saving.
  - **Dynamic Colors (Material You)** matching system wallpaper on Android 12+.
  - Curated color presets (Classic, Sage, Rose, Ocean, Lavender, Purple, Amber, Crimson, Slate).
  - Custom font presets.
  - **Dynamic App Icons** (change the app icon directly from settings).

## Tech Stack

- **Language:** [Kotlin](https://kotlinlang.org/) (Coroutines, Flow)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 Expressive
- **Widgets:** [Jetpack Glance](https://developer.android.com/jetpack/compose/glance) — modern declarative home screen widgets.
- **Data Persistence:** 
  - [Room Database](https://developer.android.com/training/data-storage/room) — local storage with automated schema migrations.
  - [Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore) — settings, tapering preferences, and baseline stats.
  - GSON — data backup serialization.
- **Dependency Injection:** [Koin](https://insert-koin.io/) — lightweight dependency injection framework.

## Localization

Currently supported languages:
- English
- Russian
- German
- Spanish
- French
- Italian
- Portuguese
- Turkish
- Ukrainian

## Installation

1. Download the latest APK from the [Releases](https://github.com/bodyaant/SmokingYou/releases/latest) page.
2. Ensure installation from unknown sources is allowed in your Android settings.
3. Install the APK on your device.

## Special Thanks

Special thanks to the following projects for design ideas and inspiration:
- **[Tomato](https://github.com/nsh07/Tomato)** and **[Zenith](https://github.com/1372Slash/Zenith)** - For beautiful interface concepts and inspiration, Material 3 Expressive guidelines implementation ideas and design inspiration.

## Contributing

Contributions are welcome. If you find a bug or have a feature suggestion, please open an issue or submit a pull request.
