# Project Development Guidelines (bahamut-BBS-2)

This document captures project-specific knowledge useful for contributors working on the Android app and its related tooling. It assumes you are an experienced Android/Kotlin developer.

## Build and Configuration

- Toolchain summary
  - Android Gradle Plugin (AGP): declared via Gradle plugins — `com.android.application` 8.12.0, Kotlin 2.1.0.
  - Gradle Wrapper: 8.13 (repo ships `gradlew`).
  - Java: target/source 17; Kotlin JVM target 17.
  - compileSdk: 36; targetSdk: 36; minSdk: 26.
  - Build tools: `buildToolsVersion '35.0.0'`.
  - NDK: `ndkVersion '28.0.12674087 rc2'` (no native code in repo currently, but version is pinned to satisfy Gradle sync when NDK side tools are involved).

- Repositories
  - `google()`, `mavenCentral()`, plus JitPack for `com.github.skydoves:colorpickerview`.

- SDK/NDK configuration
  - Ensure local Android SDK is discoverable. Either set `ANDROID_HOME`/`ANDROID_SDK_ROOT` or create `local.properties` at project root:
    ```
    sdk.dir=/Users/<you>/Library/Android/sdk
    ```
  - If you need NDK-dependent tasks, install the NDK revision specified (28.0.12674087 rc2) via Android Studio SDK Manager. The app itself doesn’t include C/C++ sources, so this is typically only required for full environment parity.

- IDE
  - Open with Android Studio (Giraffe+ recommended given AGP 8.13). First sync may prompt to install missing SDK platforms/build-tools; accept.

- Build targets
  - Assemble debug: `./gradlew :app:assembleDebug`
  - Assemble release (no minify): `./gradlew :app:assembleRelease`

- Versioning
  - Controlled in `app/build.gradle` — `versionCode` and `versionName`.

## Testing

The module uses standard JVM unit tests under `app/src/test`. Android instrumented tests are not configured in this repo (no `androidTest` content). Unit tests run on the host JVM with Robolectric absent; keep tests pure JVM unless you add Robolectric explicitly.

- Where to place tests
  - Path: `app/src/test/java/<package>/...` (Kotlin or Java). Example package: `com.kota.Bahamut`.

- Dependencies
  - JUnit 4 is compatible with AGP 8.x. If your test needs it, add to `app/build.gradle`:
    ```groovy
    dependencies {
        testImplementation 'junit:junit:4.13.2'
        testImplementation 'org.jetbrains.kotlin:kotlin-test-junit:2.1.0'
    }
    ```
  - Keep these in place if you plan to maintain unit tests long-term; they were used for verification during this session and removed afterward to keep the dependency surface minimal. It’s fine to add them back as needed.

- Running tests
  - All unit tests: `./gradlew test`
  - Debug unit tests only: `./gradlew :app:testDebugUnitTest`
  - HTML reports: `app/build/reports/tests/testDebugUnitTest/index.html`

- Verified example (performed during this session)
  - A sample test under `app/src/test/java/com/kota/Bahamut/ExampleGuidelinesTest.kt` asserting `2+3=5` was created and run successfully via Gradle after configuring `local.properties` with the Android SDK path. The test and temporary dependencies were removed afterward to keep the repo clean.

- Adding a new test (example)
  - Create `app/src/test/java/com/kota/Bahamut/FooTest.kt` (omit `package`/`import` lines here for brevity):
    ```kotlin
    class FooTest {
      @Test fun example() {
        assertTrue(1 + 1 == 2)
      }
    }
    ```
  - Ensure the `testImplementation` lines listed above exist, then run `./gradlew :app:testDebugUnitTest`.
  - Note: In your actual file, declare the proper `package` and add imports:
    - `package com.kota.Bahamut`
    - `import org.junit.Assert.assertTrue`
    - `import org.junit.Test`

- Notes
  - If Gradle fails with “SDK location not found,” add `local.properties` as shown above or set `ANDROID_SDK_ROOT`.
  - Unit tests must not depend on Android framework classes unless you bring in Robolectric. Keep logic in plain Kotlin/Java where feasible.

## Project-specific Development Notes

- Modules and key areas
  - App module only (`:app`). Domain packages:
    - `com.kota.Bahamut` — app features (Pages, Dialogs, Service, etc.).
    - `com.kota.Telnet` and `com.kota.TelnetUI` — TELNET protocol logic and rendering; see `TELNET_CONNECTION_IMPROVEMENTS.md` for terminal handling details.
    - `com.kota.ASFramework` — custom lightweight framework utilities (threading, dialogs, page controllers).
  - Cloudflare workers and scripts are present under `cloud/` for backend-adjacent utilities (e.g., imgur token refresh, URL fetch). These are not tied to the Android build but may support features like image upload.

- Kotlin/JVM settings
  - `kotlinOptions.jvmTarget = '17'`. Prefer language features compatible with Kotlin 2.1.0 and JDK 17.

- Network and media stack
  - HTTP: OkHttp 5.1.0.
  - HTML: jsoup 1.21.2.
  - Images: Glide 5.x and PhotoView.

- UI/navigation
  - Uses AndroidX Navigation (fragment/ui-ktx 2.9.x) and Material 1.13.x. ViewBinding is enabled.

- Billing and integrity
  - Billing: `com.android.billingclient:billing:8.0.0`.
  - Play Integrity: `com.google.android.play:integrity:1.5.0`.

- Code style and conventions
  - Follow the existing style in the codebase:
    - Java 17 and Kotlin files coexist. Keep Kotlin top-level functions minimal; prefer classes/objects matching surrounding patterns.
    - Package structure reflects features (e.g., `Pages/*`, `Dialogs/*`). Align new code with these feature packages.
    - Avoid introducing Android framework dependencies into core logic; place logic in plain Kotlin where possible to improve testability.
  - Lint/IDE
    - Enable Kotlin inspections and Android Lint in Android Studio. Address deprecations (e.g., `MediaStore.Images.Media.getBitmap` usages flagged during build).

- Debugging tips
  - TELNET rendering and input handling lives under `com.kota.Telnet` and `com.kota.TelnetUI`; when modifying, add small, self-contained helpers and unit tests for parsing/formatting logic (pure JVM).
  - Network flows touching Cloudflare/imgur should be isolated behind simple interfaces to ease mocking in unit tests.

- Building APKs
  - Release build is non-minified by default (no R8 rules beyond default). If enabling minify, update `proguard-rules.pro` accordingly.

## Gotchas / Checklist

- Ensure `sdk.dir` or `ANDROID_SDK_ROOT` is set; otherwise Gradle tasks (including unit tests) will fail early.
- Keep `compileSdk` and dependencies in sync; AGP 8.13 expects recent SDKs (API 36 in this project).
- When adding instrumented tests under `androidTest`, also configure a test runner and consider Robolectric for JVM tests if you need framework access without a device.
- JitPack is required for the color picker dependency; if JitPack is flaky, consider vendoring or pinning a specific commit.

