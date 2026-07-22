<div align="center">
  <img src="images/preview.png" alt="SmokingYou Showcase" width="100%">

  # SmokingYou

  Minimalist, data-oriented smoking tracker for Android based on Material 3 Expressive.

  <p align="center">
    <a href="https://github.com/mem2sp/SmokingYou/releases/latest">
      <img src="https://img.shields.io/github/v/release/mem2sp/SmokingYou?logo=github&labelColor=1a1a1a&color=6750A4&style=flat-square" alt="Latest Release">
    </a>
    <a href="https://github.com/mem2sp/SmokingYou/releases">
      <img src="https://img.shields.io/github/downloads/mem2sp/SmokingYou/total?logo=github&labelColor=1a1a1a&color=625B71&style=flat-square" alt="Total Downloads">
    </a>
    <a href="https://github.com/mem2sp/SmokingYou/stargazers">
      <img src="https://img.shields.io/github/stars/mem2sp/SmokingYou?logo=github&labelColor=1a1a1a&color=E0B6FF&style=flat-square" alt="Stars">
    </a>
    <a href="https://github.com/mem2sp/SmokingYou/network/members">
      <img src="https://img.shields.io/github/forks/mem2sp/SmokingYou?logo=github&labelColor=1a1a1a&color=4F378B&style=flat-square" alt="Forks">
    </a>
    <a href="https://github.com/mem2sp/SmokingYou/issues">
      <img src="https://img.shields.io/github/issues/mem2sp/SmokingYou?logo=github&labelColor=1a1a1a&color=B3261E&style=flat-square" alt="Issues">
    </a>
    <a href="LICENSE">
      <img src="https://img.shields.io/github/license/mem2sp/SmokingYou?logo=gnu&labelColor=1a1a1a&color=blue&style=flat-square" alt="License">
    </a>
  </p>

  <p align="center">
    <img src="https://img.shields.io/badge/Kotlin-100%25-7F52FF?logo=kotlin&logoColor=white&labelColor=1a1a1a&style=flat-square" alt="Kotlin">
    <img src="https://img.shields.io/badge/Jetpack_Compose-Modern_UI-4285F4?logo=jetpackcompose&logoColor=white&labelColor=1a1a1a&style=flat-square" alt="Jetpack Compose">
    <img src="https://img.shields.io/badge/Material_Design-3_Expressive-7B1FA2?logo=materialdesign&logoColor=white&labelColor=1a1a1a&style=flat-square" alt="Material 3">
    <img src="https://img.shields.io/badge/Android-8.0%2B_(API_26%2B)-3DDC84?logo=android&logoColor=white&labelColor=1a1a1a&style=flat-square" alt="Android">
    <img src="https://img.shields.io/badge/Android_Studio-Ladybug%2B-3DDC84?logo=androidstudio&logoColor=white&labelColor=1a1a1a&style=flat-square" alt="Android Studio">
  </p>

  <p align="center">
    <a href="https://github.com/mem2sp/SmokingYou/releases/latest">
      <img src="https://img.shields.io/github/v/release/mem2sp/SmokingYou?label=Download%20SmokingYou&style=for-the-badge&color=6750A4&logo=android&logoColor=white" alt="Download SmokingYou">
    </a>
  </p>
</div>

---

SmokingYou is designed to help you track and manage your smoking habits. By offering clear statistics, dynamic charts, and motivational achievements, the app provides the insights needed to cut down or quit entirely.

## Screenshots

<p align="center">
  <img src="images/1.png" width="33%"><img src="images/2.png" width="33%"><img src="images/3.png" width="33%">
  <img src="images/4.png" width="33%"><img src="images/5.png" width="33%"><img src="images/6.png" width="33%">
</p>

## Features

- **Habit Tracking:** Log entries with a single tap. A real-time timer displays the duration elapsed since your last cigarette.
- **Detailed History:** View, edit, or delete past logs.
- **Analytics:** Visualize daily and weekly patterns using interactive charts.
- **Statistics:** Track total count, averages, daily extremes, and your longest smoke-free streak.
- **Achievements & Notifications:** Stay motivated with milestone badges for consistency and smoke-free intervals, with instant local notifications on unlock.
- **Daily Limit:** Define a daily cigarette limit to monitor consumption.
- **Data Portability:** Local backup and restore (JSON export/import) to secure your logs.
- **Highly Customizable Themes:**
  - Standard Light, Dark, and System themes.
  - **AMOLED Dark Mode** for extra power saving.
  - **Dynamic Colors (Material You)** matching system wallpaper on Android 12+.
  - Curated color presets (Classic, Sage, Rose, Ocean, Lavender).
  - Custom font presets.
  - **Dynamic App Icons** (change the app icon directly from settings).

## Tech Stack

- **Language:** [Kotlin](https://kotlinlang.org/) (Coroutines, Flow)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 Expressive
- **Data Persistence:** 
  - [Room Database](https://developer.android.com/training/data-storage/room) — for secure local storage of logs and history.
  - [Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore) — for settings and configuration parameters.
  - GSON — for data backup serialization.
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

1. Download the latest APK from the [Releases](https://github.com/mem2sp/SmokingYou/releases/latest) page.
2. Ensure installation from unknown sources is allowed in your Android settings.
3. Install the APK on your device.

## Special Thanks

Special thanks to the following projects for design ideas and inspiration:
- **[Tomato](https://github.com/nsh07/Tomato)** and **[Zenith](https://github.com/1372Slash/Zenith)** - For beautiful interface concepts and inspiration, Material 3 Expressive guidelines implementation ideas and design inspiration.

## Contributing

Contributions are welcome. If you find a bug or have a feature suggestion, please open an issue or submit a pull request.
