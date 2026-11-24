# AGENTS.md

## Project

Single-module Android app (`:app`) — a Spanish-language memory/matching game ("Juego de las Parejas"): 8 cards, 4 pairs, 2×4 grid. Kotlin + Jetpack Compose + Material 3. No backend, persistence, or navigation.

- All game logic and UI live in one file: `app/src/main/java/fmdev/kotlin001/parellesapp/MainActivity.kt` (`CardModel`, `GameUiState`, `GameViewModel`, `MainActivity`, `GameScreen`, `CardItem`). Read it before changing behavior.
- Only theme files live elsewhere: `ui/theme/` (`Theme.kt`, `Color.kt`, `Type.kt`). Don't split into modules or add DI/Hilt.
- The version-control root is the **parent** `AndroidAppsGit/` directory, not this folder; sibling apps (`TresEnRatlla`, `Trilero_Compose`) live there.

## Build & test

- `./gradlew :app:assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`
- `./gradlew :app:testDebugUnitTest` (unit; `--tests "fmdev.kotlin001.parellesapp.*"` to filter) — only a template stub exists today.
- `./gradlew :app:connectedDebugAndroidTest` requires a running emulator/device (instrumentation test is also a stub).
- `./gradlew :app:lintDebug` runs lint; no ktlint/detekt/formatter is configured.
- No CI workflows and no root README. Don't invent commands.
- First build downloads Gradle 9.6.0 and dependencies (slow). Builds normally run from Android Studio on Windows.

## Toolchain quirks

- Shared versions live in `gradle/libs.versions.toml` (AGP 9.4.1, Kotlin 2.2.10, Compose BOM 2026.02.01, Gradle 9.6.0, compileSdk/targetSdk 37, minSdk 24, Java 11). Add dependencies there via the catalog, not as inline coordinates — `app/build.gradle.kts` already contains one inline exception (`lifecycle-viewmodel-compose:2.8.7`).
- AGP 9 DSL only: `compileSdk { version = release(37) }` and release shrinking via `optimization { enable = true; packageScope = setOf(...) }`. Do not add AGP 8-style `minifyEnabled` / `proguardFiles`.
- R8 keep rules go in `app/src/main/keepRules/*.keep` (`rules.keep` today); there is no `proguard-rules.pro`.
- `gradle.properties` enables configuration cache and `android.overridePathCheck=true` (repo path contains "Programació"; AGP aborts on the non-ASCII path without it).
- `local.properties` holds a Windows SDK path (`C:\Users\fmdev\AppData\Local\Android\Sdk`) and is gitignored. Under WSL that path doesn't resolve — build through Windows (`gradlew.bat`) or fix `sdk.dir` before running Gradle in WSL.
- Package, namespace, and applicationId are all `fmdev.kotlin001.parellesapp`; new Kotlin files go under that Java path.

## Conventions

- UI copy is hardcoded Spanish inside Compose; only `app_name` is in `res/values/strings.xml`. Keep new strings in Spanish, matching that pattern.
- Card faces and backgrounds are drawables (`chiquito`, `eugenio`, `rubianes`, `faemino`, `back`, `fondo_azul/amarillo/rojo/verde`). Adding a card face means adding the drawable **and** the entry in `GameViewModel.faces`.
- Background color encodes game state: blue idle, yellow match, red mismatch, green win. Preserve this when touching game flow.
