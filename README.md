# Meskot (መስኮት) - Ethiopian & Habesha Community Network

Meskot is a modern, feature-rich social networking application crafted specifically for the Ethiopian and Habesha diaspora and local community. Built with modern Android development standards using Kotlin, Jetpack Compose, Material 3, and Firebase.

---

## Features

- **Bilingual Interface**: Seamless switching between English and Amharic (አማርኛ).
- **Instant Messaging**: Real-time 1-on-1 chat and group messaging with presence indicators and typing indicators.
- **Audio & Video Calling**: Built-in call interface with microphone mute, speaker toggle, and camera controls.
- **Community Feed & Stories**: Rich media sharing with 24-hour stories, photo carousels, likes, comments, and shares.
- **Community Groups**: Public and private interest groups (e.g., tech, music, cultural heritage).
- **Photo Albums & Gallery**: High-resolution image albums and media viewing.
- **Tipping / Appreciation**: In-app tipping to support community creators.
- **Offline & Cloud Sync**: Firebase Authentication, Firestore, and Realtime Database integration with offline persistence.

---

## How to Get the APK

### Option 1: Built APK in this repository
The debug APK is generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Option 2: Automated GitHub Builds
When this repository is pushed to GitHub, GitHub Actions will automatically compile the project and make the APK available under the **Actions** tab as an artifact (`meskot-debug-apk`).

---

## Building from Source

### Prerequisites
- **Android Studio** (Hedgehog / Iguana / Ladybug or newer recommended)
- **JDK 17** or higher
- **Android SDK Platform 34+** (minSdk 24, compileSdk 36)

### Build Steps

1. **Clone the repository:**
   ```bash
   git clone https://github.com/<your-username>/meskot.git
   cd meskot
   ```

2. **Firebase Setup:**
   - Create a project on the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app with package name `com.aistudio.meskot.zqkv` (or your chosen package name).
   - Download `google-services.json` and place it in the `app/` folder.

3. **Build the Debug APK:**
   - On Linux / macOS:
     ```bash
     ./gradlew assembleDebug
     ```
   - On Windows:
     ```cmd
     gradlew.bat assembleDebug
     ```

4. The resulting APK will be located at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

---

## Publishing to GitHub

To push this repository to GitHub:

```bash
# 1. Initialize git (if not already done)
git init

# 2. Stage all files (respecting .gitignore)
git add .

# 3. Commit
git commit -m "Initial commit: Meskot social app"

# 4. Set main branch and connect your remote
git branch -M main
git remote add origin https://github.com/<your-username>/<your-repo-name>.git

# 5. Push to GitHub
git push -u origin main
```

To create a GitHub Release with the APK:
1. Go to your GitHub repository.
2. Click **Releases** > **Draft a new release**.
3. Create a new tag (e.g. `v1.0.0`).
4. Drag and drop `app/build/outputs/apk/debug/app-debug.apk` into the release binaries section.
5. Click **Publish release**.

---

## Architecture & Tech Stack

- **Language:** Kotlin 2.x
- **UI Toolkit:** Jetpack Compose with Material Design 3 (M3)
- **State Management:** Android ViewModel, Kotlin Coroutines, StateFlow
- **Backend & Database:** Firebase Authentication, Cloud Firestore, Realtime Database
- **Media & Camera:** AndroidX CameraX, Coil for Compose
- **Build System:** Gradle Kotlin DSL (`build.gradle.kts`) with Version Catalog (`libs.versions.toml`)
