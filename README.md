# Meskot (መስኮት) - Ethiopian & Habesha Social Network

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat&logo=android)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.x-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Backend-Firebase%20Firestore-FFCA28.svg?style=flat&logo=firebase)](https://firebase.google.com/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

**Meskot (መስኮት)** is a modern, high-performance social networking and creator monetization platform engineered specifically for the Ethiopian and Habesha community, both domestically and across the global diaspora. Built with modern Android development standards using **Kotlin**, **Jetpack Compose (Material 3)**, and **Firebase Firestore / Authentication**.

---

## Key Features

- **Meskot Cinema & Clips Lounge**: Short-form video clips with a dedicated Ethiopian gold ambient cinema aesthetic, dual browsing layouts (**Immersive Cinema Stream** vs. **2-Column Mosaic Gallery**), horizontal action dock, and cultural channel categories (*Trending, Buna & Heritage, Ethio Beats, Gursha & Skits, Diaspora*).
- **Meskot Bazaar**: Modern community marketplace for listing, buying, and selling authentic goods, electronics, fashion, and services with localized category filters and direct buyer-to-seller chat inquiries.
- **Personal Details Privacy Lock**: Toggle individual personal details (birthday, profession, location, work, education) between **"Only me" (🔒 Private)** and **"Public" (🌐 Open)** with instant cloud persistence.
- **Interactive Social Text Parser**: Auto-links all 5 major interactive entities (*URLs, Emails, Phone numbers, Hashtags with Ge'ez support, and @Mentions*) with automated trailing punctuation stripping.
- **Facebook-style Horizontal Carousel Ads**: Smooth `HorizontalPager` ad cards with item snapping, adjacent card peek (`contentPadding = 48.dp`), individual card CTA buttons (*Shop Now, Learn More*), and animated dot indicators.
- **Bilingual Interface**: One-tap toggle between **English** and **Amharic (አማርኛ)** across navigation, settings, cards, and modal dialogs.
- **Real-Time Feed & Stories**: Full-featured community feed with 24-hour stories, photo carousels, nested comments, replies, and reactions (*Like, Love, Haha, Wow, Sad, Angry*).
- **Creator Studio & Monetization**: Star tipping, VIP memberships, monetization dashboards with revenue settlements, and Chapa payment integration.
- **Real-Time Messaging & Calling**: 1-on-1 private messaging, group chat rooms, and voice/video call interfaces.

---

## Getting the APK

### Option 1: Automated GitHub Actions Build (Recommended)
Every time this repository is pushed to GitHub, the `.github/workflows/android-build.yml` workflow automatically compiles the project using JDK 21 and publishes both Debug and Release APKs as downloadable build artifacts under the **Actions** tab:
- `meskot-debug-apk`
- `meskot-release-apk`

### Option 2: Build Locally with Gradle
To compile the APKs locally on your machine:

```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease
```

The compiled APKs will be located at:
- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release.apk`

---

## Publishing to GitHub

Follow these steps to initialize and push this codebase to your own GitHub repository:

```bash
# 1. Initialize git repository
git init

# 2. Stage all project files (respecting .gitignore)
git add .

# 3. Create initial commit
git commit -m "Initial commit: Meskot social network v1.0.0"

# 4. Set default branch to main
git branch -M main

# 5. Link to your GitHub remote repository
git remote add origin https://github.com/<your-username>/<your-repo-name>.git

# 6. Push to GitHub
git push -u origin main
```

### Creating a GitHub Release with the APK

1. On GitHub, navigate to your repository's main page.
2. In the right sidebar, click **Releases** > **Draft a new release**.
3. In the **Choose a tag** dropdown, enter a release tag (e.g. `v1.0.0`) and select **Create new tag**.
4. Set the release title (e.g., `Meskot v1.0.0 - Production Release`).
5. Under the description field, drag and drop `app-release.apk` (or `app-debug.apk`) into the **Attach binaries by dropping them here or selecting them** box.
6. Click **Publish release**.

---

## Architecture & Tech Stack

```
com.example/
├── data/
│   ├── Models.kt              # App data entities (User, Post, Comment, AdCampaign, CarouselAdCard)
│   ├── Strings.kt             # English and Amharic localized strings dictionary
│   ├── FirebaseManager.kt     # Firestore, Auth, and Storage SDK operations
│   └── MeskotRepository.kt    # Single source of truth repository with StateFlow
├── recommendation/
│   ├── UserInterestModels.kt  # User interaction tracking
│   └── UserInterestTracker.kt # Personalized feed ranking algorithm
└── ui/
    ├── MainActivity.kt        # Entry-point Activity with Scaffold, navigation tabs, and top bar
    ├── MeskotViewModel.kt     # ViewModel managing UI states, actions, and dialogs
    ├── components/
    │   ├── CarouselAdCard.kt  # Snap-scrolling Horizontal Carousel Ads with peek & CTAs
    │   ├── InteractiveText.kt # Auto-linking parser for URLs, emails, phones, tags, mentions
    │   ├── NavigationBars.kt  # Bottom luxury icon navigation bar & top bar
    │   ├── PostCard.kt        # Feed post renderer with reactions, comments, media grids
    │   ├── ReelComponents.kt  # Short clip creator, viewer, comments, and rail
    │   └── StoryComponents.kt # 24-hour stories rail and viewer
    ├── screens/
    │   ├── FeedScreen.kt      # Main social feed with stories, posts, and ad slots
    │   ├── MarketplaceScreen.kt # Meskot Bazaar with search, filters, and seller chat
    │   ├── ReelsScreen.kt     # Meskot Cinema Clips Lounge (Cinema Stream & Mosaic)
    │   ├── ProfileAndMenuScreens.kt # User profiles with Personal Details privacy lock
    │   └── AdsManagerScreen.kt# Advertiser campaign creator and analytics
    └── theme/
        ├── Color.kt           # Meskot Gold, Obsidian, Paper, and Cross Red palette
        └── Theme.kt           # Material 3 dynamic color scheme and typography
```

- **Build System:** Gradle Kotlin DSL with Version Catalog (`gradle/libs.versions.toml`)
- **Minimum SDK:** API 24 (Android 7.0)
- **Target SDK / Compile SDK:** API 36 (Android 15+)
- **Java Compatibility:** Java 21

---

## License

This project is licensed under the Apache License 2.0.
