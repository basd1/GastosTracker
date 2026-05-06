# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew assembleDebug            # Build debug APK
./gradlew assembleRelease          # Build release APK (unsigned)
./gradlew installDebug             # Install debug APK on connected device/emulator
./gradlew clean                    # Clean build artifacts
```

## Testing

```bash
./gradlew testDebugUnitTest                    # Run unit tests (JVM)
./gradlew connectedAndroidTest                 # Run instrumented tests (requires device/emulator)
./gradlew testDebugUnitTest --tests "bas.orellana.apppair.ExampleUnitTest"  # Run a single test class
```

## Lint

```bash
./gradlew lintDebug                # Lint check for debug variant
```

## Architecture

This is a minimal Android starter project using **Jetpack Compose** and **Material 3**, targeting **Android 13+ (minSdk 33)**.

- **Single activity:** `MainActivity` is the only entry point; all UI is Compose.
- **Theming:** `ui/theme/Theme.kt` wires Material 3 dynamic color (Android 12+) with a static purple/pink fallback palette. `Color.kt` defines the palette; `Type.kt` defines typography.
- **No dependency injection, networking, or database layers** are present — the project is a blank starting point.

## Key Configuration

- **Package:** `bas.orellana.apppair`
- **Compile/Target SDK:** 36 | **Min SDK:** 33
- **Kotlin:** 2.0.21 | **Compose BOM:** 2024.09.00 | **AGP:** 8.13.2
- Dependencies are managed via the version catalog at `gradle/libs.versions.toml`.
