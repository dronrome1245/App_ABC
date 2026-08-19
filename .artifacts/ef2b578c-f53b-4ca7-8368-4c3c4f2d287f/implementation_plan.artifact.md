# Implementation Plan - M1.1: Project Initialization and Skeleton

This plan covers the initial setup of the App_ABC Android project, including the directory structure, build configuration, and core architecture layers (Clean Architecture/MVI).

## User Review Required

> [!IMPORTANT]
> I will be using the package name `com.dronrome1245.appabc` as specified in `NEXT_TASK.md`.
> The `minSdk` will be set to 26 (Android 8.0) and `targetSdk` to 34 (Android 14).
> I will use Gradle Kotlin DSL and Version Catalog (`libs.versions.toml`).

## Proposed Changes

### Build Configuration

#### [NEW] [settings.gradle.kts](file:///Y:/Личные%20документы/Кречко/Приложение/App_ABC/settings.gradle.kts)
Define project name and modules.

#### [NEW] [libs.versions.toml](file:///Y:/Личные%20документы/Кречко/Приложение/App_ABC/gradle/libs.versions.toml)
Centralize dependency management.

#### [NEW] [build.gradle.kts](file:///Y:/Личные%20документы/Кречко/Приложение/App_ABC/build.gradle.kts)
Root build script.

#### [NEW] [app/build.gradle.kts](file:///Y:/Личные%20документы/Кречко/Приложение/App_ABC/app/build.gradle.kts)
App module build script with Compose, Room, and DataStore dependencies.

---

### App Structure & Core

#### [NEW] [AndroidManifest.xml](file:///Y:/Личные%20документы/Кречко/Приложение/App_ABC/app/src/main/AndroidManifest.xml)
Basic manifest configuration.

#### [NEW] [core/audio/TextToSpeechWrapper.kt](file:///Y:/Личные%20документы/Кречко/Приложение/App_ABC/app/src/main/java/com/dronrome1245/appabc/core/audio/TextToSpeechWrapper.kt)
Interface for TTS.

#### [NEW] [core/theme/Theme.kt](file:///Y:/Личные%20документы/Кречко/Приложение/App_ABC/app/src/main/java/com/dronrome1245/appabc/core/theme/Theme.kt)
Material 3 theme setup.

---

### Domain Layer (Clean Kotlin)

#### [NEW] [domain/model/Letter.kt](file:///Y:/Личные%20документы/Кречко/Приложение/App_ABC/app/src/main/java/com/dronrome1245/appabc/domain/model/Letter.kt)
#### [NEW] [domain/model/Attempt.kt](file:///Y:/Личные%20документы/Кречко/Приложение/App_ABC/app/src/main/java/com/dronrome1245/appabc/domain/model/Attempt.kt)
#### [NEW] [domain/model/Session.kt](file:///Y:/Личные%20документы/Кречко/Приложение/App_ABC/app/src/main/java/com/dronrome1245/appabc/domain/model/Session.kt)
Domain models.

#### [NEW] [domain/engine/LearningEngine.kt](file:///Y:/Личные%20документы/Кречко/Приложение/App_ABC/app/src/main/java/com/dronrome1245/appabc/domain/engine/LearningEngine.kt)
Core learning logic.

---

### Data Layer

#### [NEW] [data/local/db/AppDatabase.kt](file:///Y:/Личные%20документы/Кречко/Приложение/App_ABC/app/src/main/java/com/dronrome1245/appabc/data/local/db/AppDatabase.kt)
Room database setup.

#### [NEW] [data/local/preferences/AppSettings.kt](file:///Y:/Личные%20документы/Кречко/Приложение/App_ABC/app/src/main/java/com/dronrome1245/appabc/data/local/preferences/AppSettings.kt)
DataStore preferences.

---

### UI Layer

#### [NEW] [ui/MainActivity.kt](file:///Y:/Личные%20документы/Кречко/Приложение/App_ABC/app/src/main/java/com/dronrome1245/appabc/ui/MainActivity.kt)
Entry point with basic navigation and placeholder screens.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to verify build.
- Run `./gradlew testDebugUnitTest` to verify basic unit tests.
- Create a simple Unit Test for `LearningEngine` to ensure pure Kotlin logic works.

### Manual Verification
- Launch the app on an emulator/device.
- Verify that a placeholder screen is displayed.
- Check logcat for any initialization errors.
