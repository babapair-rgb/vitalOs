# VitalOS Phase 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a running native Android app (VitalOS) implementing the 8 screens actually produced by the Stitch design, on an offline-first Room + mock-BLE foundation.

**Architecture:** Kotlin + Jetpack Compose (Material3 overridden with Stitch design tokens), MVVM with Hilt DI, Room for all persistence, a `BleRepository` interface backed by a simulated TAGG Verve device, WorkManager for periodic background sync, Compose Navigation with a 5-tab bottom nav + Quick-Add FAB.

**Tech Stack:** Kotlin 2.0.21, AGP 8.6.1, Jetpack Compose (Compose BOM 2024.09.03) with the Kotlin Compose compiler plugin, Hilt 2.52 + KSP, Room 2.6.1, WorkManager 2.9.1 + Hilt-Work 1.2.0, Navigation Compose 2.8.3, kotlinx-coroutines 1.9.0, JUnit4, Robolectric 4.13, Turbine 1.1.0.

## Global Constraints

- Package: `com.vitalos.app`. `minSdk` 26, `targetSdk`/`compileSdk` 35.
- Dark theme only in Phase 1 — do not build a light theme.
- Offline-first: every screen reads from Room via `Flow`; no network calls anywhere in Phase 1.
- BLE is fully mocked (`MockTaggVerveBleRepository`) behind a `BleRepository` interface — never attempt a real GATT connection in Phase 1.
- All demo/seeded data must be clearly labeled as demonstration data in the UI (small footer/caption), per the approved spec.
- **Shape-token resolution:** `DESIGN.md`'s prose claims 20–24px card radius, but the actual exported `code.html` tailwind config (the ground truth for what `screen.png` shows) effectively renders cards at `rounded-xl` = 12px and controls at `rounded-lg` = 8px, with pills fully rounded. Use the HTML's effective values (12px card radius, 8px control radius, full pill) as source of truth, not the prose numbers.
- Colors/typography/spacing: use the exact hex values and type scale from `stitch_vitalos_health_intelligence_system/stitch_vitalos_health_intelligence_system/vitalos_kinetic_intelligence/DESIGN.md`.
- No placeholder code, no TODOs, no stub screens beyond the two explicitly scoped placeholders (Insights, Profile).
- Every ViewModel/Repository/BLE class gets a real JUnit unit test (Robolectric where Room/Context is needed). Compose screens themselves are verified by running the app, not by instrumented tests (out of scope per spec).
- Commit after every task.

---

## Task 1: Project Scaffolding, Hilt & Compose Setup

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle/libs.versions.toml`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/vitalos/app/VitalOsApplication.kt`
- Create: `app/src/main/java/com/vitalos/app/MainActivity.kt`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values/themes.xml`
- Create: `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` (and matching `res/drawable/ic_launcher_foreground.xml`, `res/drawable/ic_launcher_background.xml`)
- Create: `.gitignore`

**Interfaces:**
- Produces: Gradle project that builds an empty Compose activity (`MainActivity`) themed by Material3, with Hilt wired (`VitalOsApplication` annotated `@HiltAndroidApp`), ready for later tasks to add screens/DI modules.

- [ ] **Step 1: Create `.gitignore`**

```gitignore
*.iml
.gradle
/local.properties
/.idea
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties
*.hprof
```

- [ ] **Step 2: Create the Gradle version catalog**

`gradle/libs.versions.toml`:

```toml
[versions]
agp = "8.6.1"
kotlin = "2.0.21"
ksp = "2.0.21-1.0.28"
coreKtx = "1.13.1"
lifecycle = "2.8.6"
activityCompose = "1.9.3"
composeBom = "2024.09.03"
navigationCompose = "2.8.3"
hilt = "2.52"
hiltNavigationCompose = "1.2.0"
hiltWork = "1.2.0"
room = "2.6.1"
workManager = "2.9.1"
coroutines = "1.9.0"
junit = "4.13.2"
robolectric = "4.13"
androidxTestCore = "1.6.1"
turbine = "1.1.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigationCompose" }
hilt-work = { group = "androidx.hilt", name = "hilt-work", version.ref = "hiltWork" }
hilt-work-compiler = { group = "androidx.hilt", name = "hilt-compiler", version.ref = "hiltWork" }
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "workManager" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
robolectric = { group = "org.robolectric", name = "robolectric", version.ref = "robolectric" }
androidx-test-core = { group = "androidx.test", name = "core", version.ref = "androidxTestCore" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
androidx-work-testing = { group = "androidx.work", name = "work-testing", version.ref = "workManager" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

- [ ] **Step 3: Create root `settings.gradle.kts`**

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "VitalOS"
include(":app")
```

- [ ] **Step 4: Create root `build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
```

- [ ] **Step 5: Create `app/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.vitalos.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vitalos.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.androidx.room.runtime)
}
```

- [ ] **Step 6: Create the manifest**

`app/src/main/AndroidManifest.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.BLUETOOTH" android:maxSdkVersion="30" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" android:maxSdkVersion="30" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" android:maxSdkVersion="30" />
    <uses-permission android:name="android.permission.BLUETOOTH_SCAN" android:usesPermissionFlags="neverForLocation" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

    <application
        android:name=".VitalOsApplication"
        android:allowBackup="false"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.VitalOS"
        android:supportsRtl="true">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.VitalOS">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 7: Create minimal resources**

`app/src/main/res/values/strings.xml`:

```xml
<resources>
    <string name="app_name">VitalOS</string>
</resources>
```

`app/src/main/res/values/themes.xml`:

```xml
<resources>
    <style name="Theme.VitalOS" parent="android:Theme.Material.NoActionBar">
        <item name="android:statusBarColor">#090B0F</item>
        <item name="android:navigationBarColor">#090B0F</item>
        <item name="android:windowBackground">#090B0F</item>
    </style>
</resources>
```

`app/src/main/res/drawable/ic_launcher_background.xml`:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#090B0F" android:pathData="M0,0h108v108h-108z" />
</vector>
```

`app/src/main/res/drawable/ic_launcher_foreground.xml`:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#B8F36B" android:pathData="M54,24a30,30 0,1 0,0.1 0zM54,36a18,18 0,1 1,-0.1 0z" />
</vector>
```

`app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
```

- [ ] **Step 8: Create `VitalOsApplication`**

`app/src/main/java/com/vitalos/app/VitalOsApplication.kt`:

```kotlin
package com.vitalos.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class VitalOsApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
```

- [ ] **Step 9: Create a placeholder `MainActivity`**

`app/src/main/java/com/vitalos/app/MainActivity.kt`:

```kotlin
package com.vitalos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VitalOsScaffoldPlaceholder()
        }
    }
}

@Composable
private fun VitalOsScaffoldPlaceholder() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text("VitalOS")
            }
        }
    }
}
```

Note: this `MainActivity` is a temporary placeholder — Task 9 replaces its body with the real themed NavHost. The `Modifier` import is `androidx.compose.ui.Modifier`; add it.

- [ ] **Step 10: Verify the project builds**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 11: Commit**

```bash
git add settings.gradle.kts build.gradle.kts gradle/libs.versions.toml app/build.gradle.kts app/src/main/AndroidManifest.xml app/src/main/java/com/vitalos/app/VitalOsApplication.kt app/src/main/java/com/vitalos/app/MainActivity.kt app/src/main/res .gitignore
git commit -m "Scaffold VitalOS Android project with Hilt and Compose"
```

---

## Task 2: Design Tokens & Theme

**Files:**
- Create: `app/src/main/res/font/space_grotesk_medium.ttf`, `space_grotesk_semibold.ttf`, `space_grotesk_bold.ttf`, `inter_regular.ttf`, `inter_medium.ttf`, `inter_semibold.ttf`, `jetbrains_mono_regular.ttf`, `jetbrains_mono_medium.ttf`
- Create: `app/src/main/java/com/vitalos/app/designsystem/Palette.kt`
- Create: `app/src/main/java/com/vitalos/app/designsystem/Typography.kt`
- Create: `app/src/main/java/com/vitalos/app/designsystem/Spacing.kt`
- Create: `app/src/main/java/com/vitalos/app/designsystem/Theme.kt`

**Interfaces:**
- Consumes: nothing (pure design tokens).
- Produces: `VitalOsPalette` (object with all `DESIGN.md` color constants as `androidx.compose.ui.graphics.Color`), `VitalOsTypography` (data class of `TextStyle`s: `displayHero`, `displayHeroMobile`, `headlineLg`, `headlineLgMobile`, `headlineMd`, `headlineSm`, `bodyLg`, `bodyMd`, `bodySm`, `labelCaps`, `dataTabularLg`, `dataTabularMd`, `dataTabularSm`), `VitalOsSpacing` (object: `xs=4.dp, sm=8.dp, md=16.dp, lg=24.dp, xl=36.dp, margin=16.dp`), `VitalOsShapes` (object: `cardRadius=12.dp, controlRadius=8.dp, pill=RoundedCornerShape(50)`), `VitalOsTheme` (object exposing `.colors`, `.typography`, `.spacing`, `.shapes` as `@Composable` accessors) and `VitalOsAppTheme { content }` composable wrapping `MaterialTheme`. Every later screen task imports `VitalOsTheme.colors`/`.typography`/`.spacing`/`.shapes`.

- [ ] **Step 1: Download the required static font files**

Run:

```bash
mkdir -p app/src/main/res/font
BASE="https://raw.githubusercontent.com/google/fonts/main/ofl"
curl -sL "$BASE/spacegrotesk/static/SpaceGrotesk-Medium.ttf" -o app/src/main/res/font/space_grotesk_medium.ttf
curl -sL "$BASE/spacegrotesk/static/SpaceGrotesk-SemiBold.ttf" -o app/src/main/res/font/space_grotesk_semibold.ttf
curl -sL "$BASE/spacegrotesk/static/SpaceGrotesk-Bold.ttf" -o app/src/main/res/font/space_grotesk_bold.ttf
curl -sL "$BASE/inter/static/Inter-Regular.ttf" -o app/src/main/res/font/inter_regular.ttf
curl -sL "$BASE/inter/static/Inter-Medium.ttf" -o app/src/main/res/font/inter_medium.ttf
curl -sL "$BASE/inter/static/Inter-SemiBold.ttf" -o app/src/main/res/font/inter_semibold.ttf
curl -sL "$BASE/jetbrainsmono/static/JetBrainsMono-Regular.ttf" -o app/src/main/res/font/jetbrains_mono_regular.ttf
curl -sL "$BASE/jetbrainsmono/static/JetBrainsMono-Medium.ttf" -o app/src/main/res/font/jetbrains_mono_medium.ttf
ls -la app/src/main/res/font
```

Expected: 8 `.ttf` files, each with non-zero size. If any URL has moved, fetch the same static weight from `https://fonts.google.com/specimen/<Family>` and save it under the same filename/path — the Kotlin code in Step 3 depends on these exact filenames as Android resource names.

- [ ] **Step 2: Create the color palette**

`app/src/main/java/com/vitalos/app/designsystem/Palette.kt`:

```kotlin
package com.vitalos.app.designsystem

import androidx.compose.ui.graphics.Color

object VitalOsPalette {
    val surface = Color(0xFF0F141B)
    val surfaceDim = Color(0xFF0F141B)
    val surfaceBright = Color(0xFF343942)
    val surfaceContainerLowest = Color(0xFF090E16)
    val surfaceContainerLow = Color(0xFF171C24)
    val surfaceContainer = Color(0xFF1B2028)
    val surfaceContainerHigh = Color(0xFF252A33)
    val surfaceContainerHighest = Color(0xFF30353E)
    val onSurface = Color(0xFFDEE2EE)
    val onSurfaceVariant = Color(0xFFC2C9B3)
    val inverseSurface = Color(0xFFDEE2EE)
    val inverseOnSurface = Color(0xFF2C3139)
    val outline = Color(0xFF8D937F)
    val outlineVariant = Color(0xFF434938)
    val surfaceTint = Color(0xFF9ED753)
    val primary = Color(0xFFFDFFF2)
    val onPrimary = Color(0xFF203700)
    val primaryContainer = Color(0xFFB8F36B)
    val onPrimaryContainer = Color(0xFF456F00)
    val inversePrimary = Color(0xFF416900)
    val secondary = Color(0xFF6ADAB4)
    val onSecondary = Color(0xFF003829)
    val secondaryContainer = Color(0xFF28A380)
    val onSecondaryContainer = Color(0xFF003023)
    val tertiary = Color(0xFFFFFDFF)
    val onTertiary = Color(0xFF002F64)
    val tertiaryContainer = Color(0xFFD4E1FF)
    val onTertiaryContainer = Color(0xFF2663B4)
    val error = Color(0xFFFFB4AB)
    val onError = Color(0xFF690005)
    val errorContainer = Color(0xFF93000A)
    val onErrorContainer = Color(0xFFFFDAD6)
    val background = Color(0xFF0F141B)
    val onBackground = Color(0xFFDEE2EE)
    val surfaceVariant = Color(0xFF30353E)

    // Direct void/semantic accents from DESIGN.md prose (used for raw canvas backgrounds and warning/critical states).
    val voidBackground = Color(0xFF090B0F)
    val cardSurface = Color(0xFF151A22)
    val elevatedSurface = Color(0xFF1B222C)
    val microBorder = Color(0xFF232B38)
    val readinessLime = Color(0xFFB8F36B)
    val recoveryMint = Color(0xFF65D6B0)
    val diagnosticBlue = Color(0xFF75A9FF)
    val warningAmber = Color(0xFFF5B95F)
    val criticalRed = Color(0xFFFF7272)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF8E9CAE)
    val textDisabled = Color(0xFF475569)
}
```

- [ ] **Step 3: Create the typography**

`app/src/main/java/com/vitalos/app/designsystem/Typography.kt`:

```kotlin
package com.vitalos.app.designsystem

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.vitalos.app.R

val SpaceGroteskFamily = FontFamily(
    Font(R.font.space_grotesk_medium, FontWeight.Medium),
    Font(R.font.space_grotesk_semibold, FontWeight.SemiBold),
    Font(R.font.space_grotesk_bold, FontWeight.Bold),
)

val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
)

val JetBrainsMonoFamily = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
)

data class VitalOsTypography(
    val displayHero: TextStyle = TextStyle(
        fontFamily = SpaceGroteskFamily, fontSize = 56.sp, fontWeight = FontWeight.Bold,
        lineHeight = 60.sp, letterSpacing = (-0.04).em(56),
    ),
    val displayHeroMobile: TextStyle = TextStyle(
        fontFamily = SpaceGroteskFamily, fontSize = 40.sp, fontWeight = FontWeight.Bold,
        lineHeight = 44.sp, letterSpacing = (-0.03).em(40),
    ),
    val headlineLg: TextStyle = TextStyle(
        fontFamily = SpaceGroteskFamily, fontSize = 32.sp, fontWeight = FontWeight.SemiBold,
        lineHeight = 38.sp, letterSpacing = (-0.02).em(32),
    ),
    val headlineLgMobile: TextStyle = TextStyle(
        fontFamily = SpaceGroteskFamily, fontSize = 26.sp, fontWeight = FontWeight.SemiBold,
        lineHeight = 32.sp, letterSpacing = (-0.02).em(26),
    ),
    val headlineMd: TextStyle = TextStyle(
        fontFamily = SpaceGroteskFamily, fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
        lineHeight = 28.sp, letterSpacing = (-0.01).em(22),
    ),
    val headlineSm: TextStyle = TextStyle(
        fontFamily = InterFamily, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp,
    ),
    val bodyLg: TextStyle = TextStyle(
        fontFamily = InterFamily, fontSize = 16.sp, fontWeight = FontWeight.Normal,
        lineHeight = 24.sp, letterSpacing = (-0.01).em(16),
    ),
    val bodyMd: TextStyle = TextStyle(
        fontFamily = InterFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp,
    ),
    val bodySm: TextStyle = TextStyle(
        fontFamily = InterFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal,
        lineHeight = 16.sp, letterSpacing = 0.01.em(12),
    ),
    val labelCaps: TextStyle = TextStyle(
        fontFamily = InterFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
        lineHeight = 14.sp, letterSpacing = 0.08.em(11),
    ),
    val dataTabularLg: TextStyle = TextStyle(
        fontFamily = JetBrainsMonoFamily, fontSize = 28.sp, fontWeight = FontWeight.Medium,
        lineHeight = 32.sp, letterSpacing = (-0.02).em(28),
    ),
    val dataTabularMd: TextStyle = TextStyle(
        fontFamily = JetBrainsMonoFamily, fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp,
    ),
    val dataTabularSm: TextStyle = TextStyle(
        fontFamily = JetBrainsMonoFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal,
        lineHeight = 16.sp, letterSpacing = 0.02.em(12),
    ),
)

/** Converts an em multiplier (relative to [sizeSp]) into an absolute sp letter-spacing value. */
private fun Double.em(sizeSp: Int) = (this * sizeSp).sp
```

- [ ] **Step 4: Create spacing and shape tokens**

`app/src/main/java/com/vitalos/app/designsystem/Spacing.kt`:

```kotlin
package com.vitalos.app.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object VitalOsSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 36.dp
    val margin = 16.dp
}

object VitalOsShapes {
    val cardRadius = 12.dp
    val controlRadius = 8.dp
    val card = RoundedCornerShape(cardRadius)
    val control = RoundedCornerShape(controlRadius)
    val pill = RoundedCornerShape(50)
}
```

- [ ] **Step 5: Create the theme composition**

`app/src/main/java/com/vitalos/app/designsystem/Theme.kt`:

```kotlin
package com.vitalos.app.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalVitalOsTypography = staticCompositionLocalOf { VitalOsTypography() }

private val vitalOsMaterialColorScheme: ColorScheme = darkColorScheme(
    primary = VitalOsPalette.primaryContainer,
    onPrimary = VitalOsPalette.onPrimaryContainer,
    primaryContainer = VitalOsPalette.primaryContainer,
    onPrimaryContainer = VitalOsPalette.onPrimaryContainer,
    secondary = VitalOsPalette.secondary,
    onSecondary = VitalOsPalette.onSecondary,
    secondaryContainer = VitalOsPalette.secondaryContainer,
    onSecondaryContainer = VitalOsPalette.onSecondaryContainer,
    tertiary = VitalOsPalette.diagnosticBlue,
    background = VitalOsPalette.voidBackground,
    onBackground = VitalOsPalette.onBackground,
    surface = VitalOsPalette.voidBackground,
    onSurface = VitalOsPalette.onSurface,
    surfaceVariant = VitalOsPalette.surfaceVariant,
    onSurfaceVariant = VitalOsPalette.onSurfaceVariant,
    outline = VitalOsPalette.outline,
    error = VitalOsPalette.criticalRed,
    onError = VitalOsPalette.onError,
    errorContainer = VitalOsPalette.errorContainer,
    onErrorContainer = VitalOsPalette.onErrorContainer,
)

object VitalOsTheme {
    val colors: VitalOsPalette get() = VitalOsPalette
    val typography: VitalOsTypography
        @Composable get() = LocalVitalOsTypography.current
    val spacing: VitalOsSpacing get() = VitalOsSpacing
    val shapes: VitalOsShapes get() = VitalOsShapes
}

@Composable
fun VitalOsAppTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalVitalOsTypography provides VitalOsTypography()) {
        MaterialTheme(
            colorScheme = vitalOsMaterialColorScheme,
            content = content,
        )
    }
}
```

- [ ] **Step 6: Verify it compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: Commit**

```bash
git add app/src/main/res/font app/src/main/java/com/vitalos/app/designsystem
git commit -m "Add VitalOS design tokens and theme"
```

---

## Task 3: Room Schema (Entities, DAOs, Database) + DatabaseModule

**Files:**
- Create: `app/src/main/java/com/vitalos/app/data/local/entity/ProfileEntity.kt`
- Create: `app/src/main/java/com/vitalos/app/data/local/entity/DailyMetricsSnapshotEntity.kt`
- Create: `app/src/main/java/com/vitalos/app/data/local/entity/HeartRateSampleEntity.kt`
- Create: `app/src/main/java/com/vitalos/app/data/local/entity/SleepEntities.kt`
- Create: `app/src/main/java/com/vitalos/app/data/local/entity/ActivitySampleEntity.kt`
- Create: `app/src/main/java/com/vitalos/app/data/local/entity/WorkoutSessionEntity.kt`
- Create: `app/src/main/java/com/vitalos/app/data/local/entity/DeviceSyncLogEntity.kt`
- Create: `app/src/main/java/com/vitalos/app/data/local/Converters.kt`
- Create: `app/src/main/java/com/vitalos/app/data/local/dao/ProfileDao.kt`
- Create: `app/src/main/java/com/vitalos/app/data/local/dao/DailyMetricsDao.kt`
- Create: `app/src/main/java/com/vitalos/app/data/local/dao/HeartRateDao.kt`
- Create: `app/src/main/java/com/vitalos/app/data/local/dao/SleepDao.kt`
- Create: `app/src/main/java/com/vitalos/app/data/local/dao/ActivityDao.kt`
- Create: `app/src/main/java/com/vitalos/app/data/local/dao/WorkoutDao.kt`
- Create: `app/src/main/java/com/vitalos/app/data/local/dao/DeviceSyncLogDao.kt`
- Create: `app/src/main/java/com/vitalos/app/data/local/VitalOsDatabase.kt`
- Create: `app/src/main/java/com/vitalos/app/di/DatabaseModule.kt`
- Test: `app/src/test/java/com/vitalos/app/data/local/VitalOsDatabaseTest.kt`

**Interfaces:**
- Consumes: nothing.
- Produces: all entity classes and `enum class SleepStageType { AWAKE, LIGHT, REM, DEEP }`, `enum class WorkoutType { YOGA, RUNNING, WALKING, STRENGTH, OTHER }`, `enum class WorkoutStatus { IN_PROGRESS, COMPLETED, DISCARDED }`; each DAO's exact method signatures below; `VitalOsDatabase` (abstract fun `profileDao()`, `dailyMetricsDao()`, `heartRateDao()`, `sleepDao()`, `activityDao()`, `workoutDao()`, `deviceSyncLogDao()`); Hilt-provided singleton `VitalOsDatabase` and each DAO (via `DatabaseModule`). All later tasks depend on these exact names.

- [ ] **Step 1: Write the entity files**

`app/src/main/java/com/vitalos/app/data/local/entity/ProfileEntity.kt`:

```kotlin
package com.vitalos.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val tierBadge: String,
    val dailyStepGoal: Int,
    val dailyHydrationGoalMl: Int,
    val dailyCalorieGoal: Int,
)
```

`app/src/main/java/com/vitalos/app/data/local/entity/DailyMetricsSnapshotEntity.kt`:

```kotlin
package com.vitalos.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_metrics_snapshot")
data class DailyMetricsSnapshotEntity(
    @PrimaryKey val dateEpochDay: Long,
    val readinessPercent: Int,
    val healthScore: Int,
    val strainTarget: Double,
    val steps: Int,
    val distanceMeters: Double,
    val activeCalories: Int,
    val hydrationMl: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val calorieTarget: Int,
    val caloriesConsumed: Int,
)
```

`app/src/main/java/com/vitalos/app/data/local/entity/HeartRateSampleEntity.kt`:

```kotlin
package com.vitalos.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "heart_rate_sample")
data class HeartRateSampleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampEpochMillis: Long,
    val bpm: Int,
    val isResting: Boolean,
)
```

`app/src/main/java/com/vitalos/app/data/local/entity/SleepEntities.kt`:

```kotlin
package com.vitalos.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class SleepStageType { AWAKE, LIGHT, REM, DEEP }

@Entity(tableName = "sleep_session")
data class SleepSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val startEpochMillis: Long,
    val endEpochMillis: Long,
    val efficiencyPercent: Double,
    val awakenings: Int,
    val sleepScore: Int,
)

@Entity(
    tableName = "sleep_stage_segment",
    foreignKeys = [
        ForeignKey(
            entity = SleepSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId")],
)
data class SleepStageSegmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val stage: SleepStageType,
    val durationMinutes: Int,
    val orderIndex: Int,
)
```

`app/src/main/java/com/vitalos/app/data/local/entity/ActivitySampleEntity.kt`:

```kotlin
package com.vitalos.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_sample")
data class ActivitySampleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val hourOfDay: Int,
    val steps: Int,
    val intensity: Int,
)
```

`app/src/main/java/com/vitalos/app/data/local/entity/WorkoutSessionEntity.kt`:

```kotlin
package com.vitalos.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class WorkoutType { YOGA, RUNNING, WALKING, STRENGTH, OTHER }
enum class WorkoutStatus { IN_PROGRESS, COMPLETED, DISCARDED }

@Entity(tableName = "workout_session")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: WorkoutType,
    val startEpochMillis: Long,
    val endEpochMillis: Long?,
    val durationSeconds: Int,
    val estimatedCalories: Int,
    val avgHeartRate: Int?,
    val maxHeartRate: Int?,
    val status: WorkoutStatus,
)
```

`app/src/main/java/com/vitalos/app/data/local/entity/DeviceSyncLogEntity.kt`:

```kotlin
package com.vitalos.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_sync_log")
data class DeviceSyncLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampEpochMillis: Long,
    val recordsMerged: Int,
    val success: Boolean,
    val errorMessage: String?,
)
```

- [ ] **Step 2: Write the type converters**

`app/src/main/java/com/vitalos/app/data/local/Converters.kt`:

```kotlin
package com.vitalos.app.data.local

import androidx.room.TypeConverter
import com.vitalos.app.data.local.entity.SleepStageType
import com.vitalos.app.data.local.entity.WorkoutStatus
import com.vitalos.app.data.local.entity.WorkoutType

class Converters {
    @TypeConverter
    fun fromSleepStageType(value: SleepStageType): String = value.name

    @TypeConverter
    fun toSleepStageType(value: String): SleepStageType = SleepStageType.valueOf(value)

    @TypeConverter
    fun fromWorkoutType(value: WorkoutType): String = value.name

    @TypeConverter
    fun toWorkoutType(value: String): WorkoutType = WorkoutType.valueOf(value)

    @TypeConverter
    fun fromWorkoutStatus(value: WorkoutStatus): String = value.name

    @TypeConverter
    fun toWorkoutStatus(value: String): WorkoutStatus = WorkoutStatus.valueOf(value)
}
```

- [ ] **Step 3: Write the DAOs**

`app/src/main/java/com/vitalos/app/data/local/dao/ProfileDao.kt`:

```kotlin
package com.vitalos.app.data.local.dao

import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.vitalos.app.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile WHERE id = 1")
    fun observe(): Flow<ProfileEntity?>

    @Query("SELECT COUNT(*) FROM profile")
    suspend fun count(): Int

    @Upsert
    suspend fun upsert(profile: ProfileEntity)
}
```

`app/src/main/java/com/vitalos/app/data/local/dao/DailyMetricsDao.kt`:

```kotlin
package com.vitalos.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.vitalos.app.data.local.entity.DailyMetricsSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyMetricsDao {
    @Query("SELECT * FROM daily_metrics_snapshot WHERE dateEpochDay = :dateEpochDay")
    fun observeByDate(dateEpochDay: Long): Flow<DailyMetricsSnapshotEntity?>

    @Query("SELECT * FROM daily_metrics_snapshot WHERE dateEpochDay BETWEEN :fromEpochDay AND :toEpochDay ORDER BY dateEpochDay ASC")
    fun observeRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<DailyMetricsSnapshotEntity>>

    @Upsert
    suspend fun upsert(snapshot: DailyMetricsSnapshotEntity)

    @Upsert
    suspend fun upsertAll(snapshots: List<DailyMetricsSnapshotEntity>)

    @Query("SELECT COUNT(*) FROM daily_metrics_snapshot")
    suspend fun count(): Int
}
```

`app/src/main/java/com/vitalos/app/data/local/dao/HeartRateDao.kt`:

```kotlin
package com.vitalos.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.vitalos.app.data.local.entity.HeartRateSampleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HeartRateDao {
    @Query("SELECT * FROM heart_rate_sample WHERE timestampEpochMillis >= :sinceEpochMillis ORDER BY timestampEpochMillis ASC")
    fun observeSince(sinceEpochMillis: Long): Flow<List<HeartRateSampleEntity>>

    @Query("SELECT * FROM heart_rate_sample ORDER BY timestampEpochMillis DESC LIMIT 1")
    fun observeLatest(): Flow<HeartRateSampleEntity?>

    @Insert
    suspend fun insert(sample: HeartRateSampleEntity): Long

    @Insert
    suspend fun insertAll(samples: List<HeartRateSampleEntity>)

    @Query("SELECT COUNT(*) FROM heart_rate_sample")
    suspend fun count(): Int
}
```

`app/src/main/java/com/vitalos/app/data/local/dao/SleepDao.kt`:

```kotlin
package com.vitalos.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.vitalos.app.data.local.entity.SleepSessionEntity
import com.vitalos.app.data.local.entity.SleepStageSegmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_session WHERE dateEpochDay = :dateEpochDay LIMIT 1")
    fun observeSessionForDate(dateEpochDay: Long): Flow<SleepSessionEntity?>

    @Query("SELECT * FROM sleep_session ORDER BY dateEpochDay DESC LIMIT :limit")
    fun observeRecentSessions(limit: Int): Flow<List<SleepSessionEntity>>

    @Query("SELECT * FROM sleep_stage_segment WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    fun observeSegments(sessionId: Long): Flow<List<SleepStageSegmentEntity>>

    @Insert
    suspend fun insertSession(session: SleepSessionEntity): Long

    @Insert
    suspend fun insertSegments(segments: List<SleepStageSegmentEntity>)

    @Query("SELECT COUNT(*) FROM sleep_session")
    suspend fun countSessions(): Int
}
```

`app/src/main/java/com/vitalos/app/data/local/dao/ActivityDao.kt`:

```kotlin
package com.vitalos.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.vitalos.app.data.local.entity.ActivitySampleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activity_sample WHERE dateEpochDay = :dateEpochDay ORDER BY hourOfDay ASC")
    fun observeForDate(dateEpochDay: Long): Flow<List<ActivitySampleEntity>>

    @Query("SELECT * FROM activity_sample WHERE dateEpochDay BETWEEN :fromEpochDay AND :toEpochDay ORDER BY dateEpochDay ASC, hourOfDay ASC")
    fun observeRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<ActivitySampleEntity>>

    @Upsert
    suspend fun upsertAll(samples: List<ActivitySampleEntity>)

    @Query("SELECT COUNT(*) FROM activity_sample")
    suspend fun count(): Int
}
```

`app/src/main/java/com/vitalos/app/data/local/dao/WorkoutDao.kt`:

```kotlin
package com.vitalos.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.vitalos.app.data.local.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_session WHERE id = :id")
    fun observeById(id: Long): Flow<WorkoutSessionEntity?>

    @Query("SELECT * FROM workout_session ORDER BY startEpochMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<WorkoutSessionEntity>>

    @Insert
    suspend fun insert(session: WorkoutSessionEntity): Long

    @Update
    suspend fun update(session: WorkoutSessionEntity)

    @Query("SELECT COUNT(*) FROM workout_session")
    suspend fun count(): Int
}
```

`app/src/main/java/com/vitalos/app/data/local/dao/DeviceSyncLogDao.kt`:

```kotlin
package com.vitalos.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.vitalos.app.data.local.entity.DeviceSyncLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceSyncLogDao {
    @Query("SELECT * FROM device_sync_log ORDER BY timestampEpochMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<DeviceSyncLogEntity>>

    @Insert
    suspend fun insert(log: DeviceSyncLogEntity): Long

    @Query("SELECT COUNT(*) FROM device_sync_log")
    suspend fun count(): Int
}
```

- [ ] **Step 4: Write the database class**

`app/src/main/java/com/vitalos/app/data/local/VitalOsDatabase.kt`:

```kotlin
package com.vitalos.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vitalos.app.data.local.dao.ActivityDao
import com.vitalos.app.data.local.dao.DailyMetricsDao
import com.vitalos.app.data.local.dao.DeviceSyncLogDao
import com.vitalos.app.data.local.dao.HeartRateDao
import com.vitalos.app.data.local.dao.ProfileDao
import com.vitalos.app.data.local.dao.SleepDao
import com.vitalos.app.data.local.dao.WorkoutDao
import com.vitalos.app.data.local.entity.ActivitySampleEntity
import com.vitalos.app.data.local.entity.DailyMetricsSnapshotEntity
import com.vitalos.app.data.local.entity.DeviceSyncLogEntity
import com.vitalos.app.data.local.entity.HeartRateSampleEntity
import com.vitalos.app.data.local.entity.ProfileEntity
import com.vitalos.app.data.local.entity.SleepSessionEntity
import com.vitalos.app.data.local.entity.SleepStageSegmentEntity
import com.vitalos.app.data.local.entity.WorkoutSessionEntity

@Database(
    entities = [
        ProfileEntity::class,
        DailyMetricsSnapshotEntity::class,
        HeartRateSampleEntity::class,
        SleepSessionEntity::class,
        SleepStageSegmentEntity::class,
        ActivitySampleEntity::class,
        WorkoutSessionEntity::class,
        DeviceSyncLogEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class VitalOsDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun dailyMetricsDao(): DailyMetricsDao
    abstract fun heartRateDao(): HeartRateDao
    abstract fun sleepDao(): SleepDao
    abstract fun activityDao(): ActivityDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun deviceSyncLogDao(): DeviceSyncLogDao

    companion object {
        const val DATABASE_NAME = "vitalos.db"
    }
}
```

- [ ] **Step 5: Write the Hilt database module**

`app/src/main/java/com/vitalos/app/di/DatabaseModule.kt`:

```kotlin
package com.vitalos.app.di

import android.content.Context
import androidx.room.Room
import com.vitalos.app.data.local.VitalOsDatabase
import com.vitalos.app.data.local.dao.ActivityDao
import com.vitalos.app.data.local.dao.DailyMetricsDao
import com.vitalos.app.data.local.dao.DeviceSyncLogDao
import com.vitalos.app.data.local.dao.HeartRateDao
import com.vitalos.app.data.local.dao.ProfileDao
import com.vitalos.app.data.local.dao.SleepDao
import com.vitalos.app.data.local.dao.WorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VitalOsDatabase =
        Room.databaseBuilder(context, VitalOsDatabase::class.java, VitalOsDatabase.DATABASE_NAME).build()

    @Provides
    fun provideProfileDao(db: VitalOsDatabase): ProfileDao = db.profileDao()

    @Provides
    fun provideDailyMetricsDao(db: VitalOsDatabase): DailyMetricsDao = db.dailyMetricsDao()

    @Provides
    fun provideHeartRateDao(db: VitalOsDatabase): HeartRateDao = db.heartRateDao()

    @Provides
    fun provideSleepDao(db: VitalOsDatabase): SleepDao = db.sleepDao()

    @Provides
    fun provideActivityDao(db: VitalOsDatabase): ActivityDao = db.activityDao()

    @Provides
    fun provideWorkoutDao(db: VitalOsDatabase): WorkoutDao = db.workoutDao()

    @Provides
    fun provideDeviceSyncLogDao(db: VitalOsDatabase): DeviceSyncLogDao = db.deviceSyncLogDao()
}
```

- [ ] **Step 6: Write the failing Robolectric test**

`app/src/test/java/com/vitalos/app/data/local/VitalOsDatabaseTest.kt`:

```kotlin
package com.vitalos.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.vitalos.app.data.local.entity.ActivitySampleEntity
import com.vitalos.app.data.local.entity.DailyMetricsSnapshotEntity
import com.vitalos.app.data.local.entity.DeviceSyncLogEntity
import com.vitalos.app.data.local.entity.HeartRateSampleEntity
import com.vitalos.app.data.local.entity.ProfileEntity
import com.vitalos.app.data.local.entity.SleepSessionEntity
import com.vitalos.app.data.local.entity.SleepStageSegmentEntity
import com.vitalos.app.data.local.entity.SleepStageType
import com.vitalos.app.data.local.entity.WorkoutSessionEntity
import com.vitalos.app.data.local.entity.WorkoutStatus
import com.vitalos.app.data.local.entity.WorkoutType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VitalOsDatabaseTest {

    private lateinit var db: VitalOsDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, VitalOsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun profileDao_upsertThenObserve_emitsProfile() = runTest {
        db.profileDao().upsert(ProfileEntity(name = "Alex", tierBadge = "PRO LAB", dailyStepGoal = 10000, dailyHydrationGoalMl = 3000, dailyCalorieGoal = 2400))
        db.profileDao().observe().test {
            assertEquals("Alex", awaitItem()?.name)
        }
    }

    @Test
    fun dailyMetricsDao_upsertThenObserveByDate_emitsSnapshot() = runTest {
        val snapshot = DailyMetricsSnapshotEntity(
            dateEpochDay = 19000, readinessPercent = 94, healthScore = 89, strainTarget = 14.5,
            steps = 8420, distanceMeters = 6200.0, activeCalories = 540, hydrationMl = 2100,
            proteinGrams = 142, carbsGrams = 180, fatGrams = 55, calorieTarget = 2400, caloriesConsumed = 1780,
        )
        db.dailyMetricsDao().upsert(snapshot)
        db.dailyMetricsDao().observeByDate(19000).test {
            assertEquals(94, awaitItem()?.readinessPercent)
        }
    }

    @Test
    fun heartRateDao_insertThenObserveLatest_emitsMostRecentSample() = runTest {
        db.heartRateDao().insertAll(
            listOf(
                HeartRateSampleEntity(timestampEpochMillis = 1000, bpm = 60, isResting = true),
                HeartRateSampleEntity(timestampEpochMillis = 2000, bpm = 65, isResting = false),
            ),
        )
        db.heartRateDao().observeLatest().test {
            assertEquals(65, awaitItem()?.bpm)
        }
    }

    @Test
    fun sleepDao_insertSessionAndSegments_observesBoth() = runTest {
        val sessionId = db.sleepDao().insertSession(
            SleepSessionEntity(dateEpochDay = 19000, startEpochMillis = 0, endEpochMillis = 100, efficiencyPercent = 96.4, awakenings = 0, sleepScore = 91),
        )
        db.sleepDao().insertSegments(
            listOf(SleepStageSegmentEntity(sessionId = sessionId, stage = SleepStageType.DEEP, durationMinutes = 102, orderIndex = 0)),
        )
        db.sleepDao().observeSessionForDate(19000).test {
            assertEquals(91, awaitItem()?.sleepScore)
        }
        db.sleepDao().observeSegments(sessionId).test {
            assertEquals(SleepStageType.DEEP, awaitItem().first().stage)
        }
    }

    @Test
    fun activityDao_upsertAllThenObserveForDate_emitsHourlySamples() = runTest {
        db.activityDao().upsertAll(
            listOf(ActivitySampleEntity(dateEpochDay = 19000, hourOfDay = 8, steps = 1200, intensity = 2)),
        )
        db.activityDao().observeForDate(19000).test {
            assertEquals(1200, awaitItem().first().steps)
        }
    }

    @Test
    fun workoutDao_insertThenUpdate_persistsStatusChange() = runTest {
        val id = db.workoutDao().insert(
            WorkoutSessionEntity(type = WorkoutType.YOGA, startEpochMillis = 0, endEpochMillis = null, durationSeconds = 0, estimatedCalories = 0, avgHeartRate = null, maxHeartRate = null, status = WorkoutStatus.IN_PROGRESS),
        )
        val inProgress = db.workoutDao().observeById(id)
        inProgress.test { assertEquals(WorkoutStatus.IN_PROGRESS, awaitItem()?.status) }
        db.workoutDao().update(
            WorkoutSessionEntity(id = id, type = WorkoutType.YOGA, startEpochMillis = 0, endEpochMillis = 1800000, durationSeconds = 1800, estimatedCalories = 180, avgHeartRate = 98, maxHeartRate = 120, status = WorkoutStatus.COMPLETED),
        )
        db.workoutDao().observeById(id).test {
            assertEquals(WorkoutStatus.COMPLETED, awaitItem()?.status)
        }
    }

    @Test
    fun deviceSyncLogDao_insertThenObserveRecent_emitsLog() = runTest {
        db.deviceSyncLogDao().insert(DeviceSyncLogEntity(timestampEpochMillis = 5000, recordsMerged = 42, success = true, errorMessage = null))
        db.deviceSyncLogDao().observeRecent(10).test {
            assertEquals(42, awaitItem().first().recordsMerged)
        }
    }
}
```

- [ ] **Step 7: Run the test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.data.local.VitalOsDatabaseTest"`
Expected: FAIL to compile (entities/DAOs/database referenced by the test did not exist before Step 1–5 — if executing steps in order, this check instead confirms the test fails before Step 1–5 are written; since Step 1–5 precede this step here, skip straight to Step 8).

- [ ] **Step 8: Run the test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.data.local.VitalOsDatabaseTest"`
Expected: `BUILD SUCCESSFUL`, 7 tests passed.

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/vitalos/app/data/local app/src/main/java/com/vitalos/app/di/DatabaseModule.kt app/src/test/java/com/vitalos/app/data/local
git commit -m "Add Room schema, DAOs, and DatabaseModule"
```

---

## Task 4: Demo Data Seeder

**Files:**
- Create: `app/src/main/java/com/vitalos/app/data/local/DemoDataSeeder.kt`
- Modify: `app/src/main/java/com/vitalos/app/VitalOsApplication.kt`
- Test: `app/src/test/java/com/vitalos/app/data/local/DemoDataSeederTest.kt`

**Interfaces:**
- Consumes: `VitalOsDatabase` and all DAOs from Task 3 (`profileDao()`, `dailyMetricsDao()`, `heartRateDao()`, `sleepDao()`, `activityDao()`, `workoutDao()`, `deviceSyncLogDao()`).
- Produces: `class DemoDataSeeder(private val database: VitalOsDatabase) { suspend fun seedIfEmpty() }`. Seeds exactly one `ProfileEntity`, 14 days of `DailyMetricsSnapshotEntity` (today back to -13 days), hourly `ActivitySampleEntity` for today, ~60 minutes of `HeartRateSampleEntity` leading up to now, one `SleepSessionEntity` + 4 `SleepStageSegmentEntity` for last night, one completed `WorkoutSessionEntity`, and one `DeviceSyncLogEntity`. `VitalOsApplication.onCreate()` launches `seedIfEmpty()` on an application-scoped coroutine.

- [ ] **Step 1: Write the failing test**

`app/src/test/java/com/vitalos/app/data/local/DemoDataSeederTest.kt`:

```kotlin
package com.vitalos.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DemoDataSeederTest {

    private lateinit var db: VitalOsDatabase
    private lateinit var seeder: DemoDataSeeder

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, VitalOsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        seeder = DemoDataSeeder(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun seedIfEmpty_populatesAllTables() = runTest {
        seeder.seedIfEmpty()

        assertEquals(1, db.profileDao().count())
        assertEquals(14, db.dailyMetricsDao().count())
        assertTrue(db.activityDao().count() > 0)
        assertTrue(db.heartRateDao().count() > 0)
        assertEquals(1, db.sleepDao().countSessions())
        assertEquals(1, db.workoutDao().count())
    }

    @Test
    fun seedIfEmpty_calledTwice_doesNotDuplicate() = runTest {
        seeder.seedIfEmpty()
        seeder.seedIfEmpty()

        assertEquals(1, db.profileDao().count())
        assertEquals(14, db.dailyMetricsDao().count())
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.data.local.DemoDataSeederTest"`
Expected: FAIL — `DemoDataSeeder` is unresolved.

- [ ] **Step 3: Write the seeder**

`app/src/main/java/com/vitalos/app/data/local/DemoDataSeeder.kt`:

```kotlin
package com.vitalos.app.data.local

import com.vitalos.app.data.local.entity.ActivitySampleEntity
import com.vitalos.app.data.local.entity.DailyMetricsSnapshotEntity
import com.vitalos.app.data.local.entity.DeviceSyncLogEntity
import com.vitalos.app.data.local.entity.HeartRateSampleEntity
import com.vitalos.app.data.local.entity.ProfileEntity
import com.vitalos.app.data.local.entity.SleepSessionEntity
import com.vitalos.app.data.local.entity.SleepStageSegmentEntity
import com.vitalos.app.data.local.entity.SleepStageType
import com.vitalos.app.data.local.entity.WorkoutSessionEntity
import com.vitalos.app.data.local.entity.WorkoutStatus
import com.vitalos.app.data.local.entity.WorkoutType
import java.time.LocalDate
import kotlin.random.Random

class DemoDataSeeder(private val database: VitalOsDatabase) {

    suspend fun seedIfEmpty() {
        if (database.profileDao().count() > 0) return

        val today = LocalDate.now().toEpochDay()
        val now = System.currentTimeMillis()
        val random = Random(seed = 42)

        database.profileDao().upsert(
            ProfileEntity(name = "Alex", tierBadge = "PRO LAB", dailyStepGoal = 10000, dailyHydrationGoalMl = 3000, dailyCalorieGoal = 2400),
        )

        val snapshots = (0..13).map { offset ->
            val day = today - offset
            DailyMetricsSnapshotEntity(
                dateEpochDay = day,
                readinessPercent = 70 + random.nextInt(0, 25),
                healthScore = 70 + random.nextInt(0, 20),
                strainTarget = 10.0 + random.nextInt(0, 8),
                steps = 5000 + random.nextInt(0, 6000),
                distanceMeters = 3000.0 + random.nextInt(0, 5000),
                activeCalories = 300 + random.nextInt(0, 400),
                hydrationMl = 1200 + random.nextInt(0, 1800),
                proteinGrams = 100 + random.nextInt(0, 80),
                carbsGrams = 120 + random.nextInt(0, 100),
                fatGrams = 40 + random.nextInt(0, 30),
                calorieTarget = 2400,
                caloriesConsumed = 1500 + random.nextInt(0, 900),
            )
        }
        database.dailyMetricsDao().upsertAll(snapshots)

        val hourlyActivity = (6..21).map { hour ->
            ActivitySampleEntity(
                dateEpochDay = today,
                hourOfDay = hour,
                steps = random.nextInt(0, 1200),
                intensity = random.nextInt(0, 4),
            )
        }
        database.activityDao().upsertAll(hourlyActivity)

        val heartRateSamples = (0 until 60).map { minutesAgo ->
            HeartRateSampleEntity(
                timestampEpochMillis = now - (59 - minutesAgo) * 60_000L,
                bpm = 58 + random.nextInt(0, 60),
                isResting = minutesAgo % 5 == 0,
            )
        }
        database.heartRateDao().insertAll(heartRateSamples)

        val sessionId = database.sleepDao().insertSession(
            SleepSessionEntity(
                dateEpochDay = today,
                startEpochMillis = now - 8 * 60 * 60_000L,
                endEpochMillis = now - 1 * 60 * 60_000L,
                efficiencyPercent = 96.4,
                awakenings = 0,
                sleepScore = 91,
            ),
        )
        database.sleepDao().insertSegments(
            listOf(
                SleepStageSegmentEntity(sessionId = sessionId, stage = SleepStageType.AWAKE, durationMinutes = 22, orderIndex = 0),
                SleepStageSegmentEntity(sessionId = sessionId, stage = SleepStageType.LIGHT, durationMinutes = 236, orderIndex = 1),
                SleepStageSegmentEntity(sessionId = sessionId, stage = SleepStageType.REM, durationMinutes = 130, orderIndex = 2),
                SleepStageSegmentEntity(sessionId = sessionId, stage = SleepStageType.DEEP, durationMinutes = 102, orderIndex = 3),
            ),
        )

        database.workoutDao().insert(
            WorkoutSessionEntity(
                type = WorkoutType.YOGA,
                startEpochMillis = now - 26 * 60 * 60_000L,
                endEpochMillis = now - 25 * 60 * 60_000L - 25 * 60_000L,
                durationSeconds = 35 * 60,
                estimatedCalories = 180,
                avgHeartRate = 98,
                maxHeartRate = 124,
                status = WorkoutStatus.COMPLETED,
            ),
        )

        database.deviceSyncLogDao().insert(
            DeviceSyncLogEntity(timestampEpochMillis = now - 4 * 60_000L, recordsMerged = 42, success = true, errorMessage = null),
        )
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.data.local.DemoDataSeederTest"`
Expected: `BUILD SUCCESSFUL`, 2 tests passed.

- [ ] **Step 5: Wire seeding into application startup**

Modify `app/src/main/java/com/vitalos/app/VitalOsApplication.kt` — replace its body with:

```kotlin
package com.vitalos.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.vitalos.app.data.local.DemoDataSeeder
import com.vitalos.app.data.local.VitalOsDatabase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class VitalOsApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var database: VitalOsDatabase

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            DemoDataSeeder(database).seedIfEmpty()
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
```

- [ ] **Step 6: Verify the app still builds**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/vitalos/app/data/local/DemoDataSeeder.kt app/src/main/java/com/vitalos/app/VitalOsApplication.kt app/src/test/java/com/vitalos/app/data/local/DemoDataSeederTest.kt
git commit -m "Seed demo data on first launch"
```

---

## Task 5: BLE Abstraction (Models, Interface, Mock Implementation) + BleModule

**Files:**
- Create: `app/src/main/java/com/vitalos/app/data/ble/BleModels.kt`
- Create: `app/src/main/java/com/vitalos/app/data/ble/BleRepository.kt`
- Create: `app/src/main/java/com/vitalos/app/data/ble/MockTaggVerveBleRepository.kt`
- Create: `app/src/main/java/com/vitalos/app/di/BleModule.kt`
- Create: `app/src/test/java/com/vitalos/app/testing/FakeBleRepository.kt`
- Test: `app/src/test/java/com/vitalos/app/data/ble/MockTaggVerveBleRepositoryTest.kt`

**Interfaces:**
- Consumes: `HeartRateDao`, `DeviceSyncLogDao` (Task 3).
- Produces:
  - `enum class BleConnectionPhase { DISCONNECTED, SCANNING, CONNECTING, CONNECTED, SYNCING }`
  - `data class BleDeviceInfo(val id: String, val name: String, val rssi: Int)`
  - `data class BleConnectionState(val phase: BleConnectionPhase, val connectedDevice: BleDeviceInfo? = null, val batteryPercent: Int? = null, val lastSyncEpochMillis: Long? = null)`
  - `data class SyncResult(val recordsMerged: Int, val success: Boolean, val errorMessage: String? = null)`
  - `interface BleRepository { val connectionState: StateFlow<BleConnectionState>; val discoveredDevices: StateFlow<List<BleDeviceInfo>>; val liveHeartRate: StateFlow<Int?>; suspend fun startScan(); suspend fun stopScan(); suspend fun connect(deviceId: String); suspend fun disconnect(); suspend fun sync(): SyncResult }`
  - `MockTaggVerveBleRepository` implementing it, Hilt-bound as the singleton `BleRepository` via `BleModule`.
  - `FakeBleRepository` test double (same interface, `MutableStateFlow`s exposed directly for tests to manipulate) for later ViewModel tests (Tasks 11, 16) to consume.

- [ ] **Step 1: Write the BLE models**

`app/src/main/java/com/vitalos/app/data/ble/BleModels.kt`:

```kotlin
package com.vitalos.app.data.ble

enum class BleConnectionPhase { DISCONNECTED, SCANNING, CONNECTING, CONNECTED, SYNCING }

data class BleDeviceInfo(
    val id: String,
    val name: String,
    val rssi: Int,
)

data class BleConnectionState(
    val phase: BleConnectionPhase = BleConnectionPhase.DISCONNECTED,
    val connectedDevice: BleDeviceInfo? = null,
    val batteryPercent: Int? = null,
    val lastSyncEpochMillis: Long? = null,
)

data class SyncResult(
    val recordsMerged: Int,
    val success: Boolean,
    val errorMessage: String? = null,
)
```

- [ ] **Step 2: Write the BleRepository interface**

`app/src/main/java/com/vitalos/app/data/ble/BleRepository.kt`:

```kotlin
package com.vitalos.app.data.ble

import kotlinx.coroutines.flow.StateFlow

interface BleRepository {
    val connectionState: StateFlow<BleConnectionState>
    val discoveredDevices: StateFlow<List<BleDeviceInfo>>
    val liveHeartRate: StateFlow<Int?>

    suspend fun startScan()
    suspend fun stopScan()
    suspend fun connect(deviceId: String)
    suspend fun disconnect()
    suspend fun sync(): SyncResult
}
```

- [ ] **Step 3: Write the fake test double**

`app/src/test/java/com/vitalos/app/testing/FakeBleRepository.kt`:

```kotlin
package com.vitalos.app.testing

import com.vitalos.app.data.ble.BleConnectionState
import com.vitalos.app.data.ble.BleDeviceInfo
import com.vitalos.app.data.ble.BleRepository
import com.vitalos.app.data.ble.SyncResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeBleRepository : BleRepository {
    val connectionStateFlow = MutableStateFlow(BleConnectionState())
    val discoveredDevicesFlow = MutableStateFlow<List<BleDeviceInfo>>(emptyList())
    val liveHeartRateFlow = MutableStateFlow<Int?>(null)

    var syncResult: SyncResult = SyncResult(recordsMerged = 0, success = true)
    var scanCalled = false
    var connectCalledWith: String? = null
    var disconnectCalled = false
    var syncCalled = false

    override val connectionState: StateFlow<BleConnectionState> = connectionStateFlow
    override val discoveredDevices: StateFlow<List<BleDeviceInfo>> = discoveredDevicesFlow
    override val liveHeartRate: StateFlow<Int?> = liveHeartRateFlow

    override suspend fun startScan() {
        scanCalled = true
    }

    override suspend fun stopScan() {}

    override suspend fun connect(deviceId: String) {
        connectCalledWith = deviceId
    }

    override suspend fun disconnect() {
        disconnectCalled = true
    }

    override suspend fun sync(): SyncResult {
        syncCalled = true
        return syncResult
    }
}
```

- [ ] **Step 4: Write the failing test for the mock implementation**

`app/src/test/java/com/vitalos/app/data/ble/MockTaggVerveBleRepositoryTest.kt`:

```kotlin
package com.vitalos.app.data.ble

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.vitalos.app.data.local.VitalOsDatabase
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MockTaggVerveBleRepositoryTest {

    private lateinit var db: VitalOsDatabase
    private val dispatcher = StandardTestDispatcher()
    private val scope = TestScope(dispatcher)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, VitalOsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun newRepository() = MockTaggVerveBleRepository(
        heartRateDao = db.heartRateDao(),
        deviceSyncLogDao = db.deviceSyncLogDao(),
        externalScope = scope,
        dispatcher = dispatcher,
    )

    @Test
    fun startScan_thenConnect_transitionsThroughExpectedPhases() = scope.runTest {
        val repo = newRepository()
        repo.connectionState.test {
            assertEquals(BleConnectionPhase.DISCONNECTED, awaitItem().phase)
            repo.startScan()
            advanceTimeBy(1_600)
            assertEquals(BleConnectionPhase.SCANNING, awaitItem().phase)
            val device = repo.discoveredDevices.value.first()
            repo.connect(device.id)
            assertEquals(BleConnectionPhase.CONNECTING, awaitItem().phase)
            advanceTimeBy(900)
            val connected = awaitItem()
            assertEquals(BleConnectionPhase.CONNECTED, connected.phase)
            assertNotNull(connected.batteryPercent)
        }
    }

    @Test
    fun sync_whenConnected_writesHeartRateAndSyncLogRows() = scope.runTest {
        val repo = newRepository()
        repo.startScan()
        advanceTimeBy(1_600)
        val device = repo.discoveredDevices.value.first()
        repo.connect(device.id)
        advanceTimeBy(900)

        val result = repo.sync()

        assertTrue(result.success)
        assertTrue(result.recordsMerged > 0)
        assertTrue(db.heartRateDao().count() > 0)
        val logs = kotlinx.coroutines.flow.first(db.deviceSyncLogDao().observeRecent(10))
        assertEquals(1, logs.size)
    }

    @Test
    fun liveHeartRate_whileConnected_emitsVaryingValuesAroundBaseline() = scope.runTest {
        val repo = newRepository()
        repo.startScan()
        advanceTimeBy(1_600)
        repo.connect(repo.discoveredDevices.value.first().id)
        advanceTimeBy(900)

        assertEquals(64, repo.liveHeartRate.value)
        advanceTimeBy(3_600)
        val bpm = repo.liveHeartRate.value
        assertNotNull(bpm)
        assertTrue(bpm!! in 60..68)
    }
}
```

- [ ] **Step 5: Run the test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.data.ble.MockTaggVerveBleRepositoryTest"`
Expected: FAIL — `MockTaggVerveBleRepository` is unresolved.

- [ ] **Step 6: Write the mock implementation**

`app/src/main/java/com/vitalos/app/data/ble/MockTaggVerveBleRepository.kt`:

```kotlin
package com.vitalos.app.data.ble

import com.vitalos.app.data.local.dao.DeviceSyncLogDao
import com.vitalos.app.data.local.dao.HeartRateDao
import com.vitalos.app.data.local.entity.DeviceSyncLogEntity
import com.vitalos.app.data.local.entity.HeartRateSampleEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class MockTaggVerveBleRepository @Inject constructor(
    private val heartRateDao: HeartRateDao,
    private val deviceSyncLogDao: DeviceSyncLogDao,
    private val externalScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
) : BleRepository {

    private val _connectionState = MutableStateFlow(BleConnectionState())
    override val connectionState: StateFlow<BleConnectionState> = _connectionState

    private val _discoveredDevices = MutableStateFlow<List<BleDeviceInfo>>(emptyList())
    override val discoveredDevices: StateFlow<List<BleDeviceInfo>> = _discoveredDevices

    private val _liveHeartRate = MutableStateFlow<Int?>(null)
    override val liveHeartRate: StateFlow<Int?> = _liveHeartRate

    private var liveHrJob: Job? = null
    private val random = Random(seed = 7)

    override suspend fun startScan() {
        _connectionState.value = _connectionState.value.copy(phase = BleConnectionPhase.SCANNING)
        delay(1_500)
        val suffix = (1000..9999).random(random)
        _discoveredDevices.value = listOf(BleDeviceInfo(id = "TAGG-$suffix", name = "TAGG Verve · $suffix", rssi = -45))
    }

    override suspend fun stopScan() {
        if (_connectionState.value.phase == BleConnectionPhase.SCANNING) {
            _connectionState.value = _connectionState.value.copy(phase = BleConnectionPhase.DISCONNECTED)
        }
    }

    override suspend fun connect(deviceId: String) {
        val device = _discoveredDevices.value.firstOrNull { it.id == deviceId } ?: return
        _connectionState.value = _connectionState.value.copy(phase = BleConnectionPhase.CONNECTING)
        delay(800)
        _connectionState.value = BleConnectionState(
            phase = BleConnectionPhase.CONNECTED,
            connectedDevice = device,
            batteryPercent = 60 + random.nextInt(0, 35),
            lastSyncEpochMillis = _connectionState.value.lastSyncEpochMillis,
        )
        startLiveHeartRate()
    }

    override suspend fun disconnect() {
        liveHrJob?.cancel()
        liveHrJob = null
        _liveHeartRate.value = null
        _connectionState.value = BleConnectionState(phase = BleConnectionPhase.DISCONNECTED)
    }

    override suspend fun sync(): SyncResult {
        val current = _connectionState.value
        if (current.phase != BleConnectionPhase.CONNECTED) {
            return SyncResult(recordsMerged = 0, success = false, errorMessage = "No connected device")
        }
        _connectionState.value = current.copy(phase = BleConnectionPhase.SYNCING)
        delay(400)

        val now = System.currentTimeMillis()
        val newSamples = (0 until 5).map { i ->
            HeartRateSampleEntity(
                timestampEpochMillis = now - (4 - i) * 60_000L,
                bpm = 58 + random.nextInt(0, 60),
                isResting = i == 0,
            )
        }
        heartRateDao.insertAll(newSamples)
        deviceSyncLogDao.insert(
            DeviceSyncLogEntity(timestampEpochMillis = now, recordsMerged = newSamples.size, success = true, errorMessage = null),
        )

        _connectionState.value = current.copy(phase = BleConnectionPhase.CONNECTED, lastSyncEpochMillis = now)
        return SyncResult(recordsMerged = newSamples.size, success = true)
    }

    private fun startLiveHeartRate() {
        liveHrJob?.cancel()
        liveHrJob = externalScope.launch(dispatcher) {
            _liveHeartRate.value = 64
            while (true) {
                delay(3_500)
                val delta = random.nextInt(-1, 2)
                _liveHeartRate.value = 64 + delta
            }
        }
    }
}
```

- [ ] **Step 7: Write the Hilt BLE module**

`app/src/main/java/com/vitalos/app/di/BleModule.kt`:

```kotlin
package com.vitalos.app.di

import com.vitalos.app.data.ble.BleRepository
import com.vitalos.app.data.ble.MockTaggVerveBleRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BleModule {
    @Binds
    @Singleton
    abstract fun bindBleRepository(impl: MockTaggVerveBleRepository): BleRepository

    companion object {
        @Provides
        @Singleton
        fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        @Provides
        fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
    }
}
```

- [ ] **Step 8: Run the test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.data.ble.MockTaggVerveBleRepositoryTest"`
Expected: `BUILD SUCCESSFUL`, 3 tests passed.

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/vitalos/app/data/ble app/src/main/java/com/vitalos/app/di/BleModule.kt app/src/test/java/com/vitalos/app/testing/FakeBleRepository.kt app/src/test/java/com/vitalos/app/data/ble
git commit -m "Add mock TAGG Verve BLE repository"
```

---

## Task 6: HealthDataRepository + RepositoryModule

**Files:**
- Create: `app/src/main/java/com/vitalos/app/data/repository/HealthDataRepository.kt`
- Create: `app/src/main/java/com/vitalos/app/data/repository/HealthDataRepositoryImpl.kt`
- Create: `app/src/main/java/com/vitalos/app/di/RepositoryModule.kt`
- Create: `app/src/test/java/com/vitalos/app/testing/FakeHealthDataRepository.kt`
- Test: `app/src/test/java/com/vitalos/app/data/repository/HealthDataRepositoryImplTest.kt`

**Interfaces:**
- Consumes: all DAOs from Task 3.
- Produces:

```kotlin
data class SleepSessionWithSegments(
    val session: SleepSessionEntity,
    val segments: List<SleepStageSegmentEntity>,
)

interface HealthDataRepository {
    fun observeProfile(): Flow<ProfileEntity?>
    fun observeTodaySnapshot(): Flow<DailyMetricsSnapshotEntity?>
    fun observeSnapshotRange(days: Int): Flow<List<DailyMetricsSnapshotEntity>>
    fun observeRecentHeartRate(windowMinutes: Int): Flow<List<HeartRateSampleEntity>>
    fun observeLatestHeartRate(): Flow<HeartRateSampleEntity?>
    fun observeTodaySleepSession(): Flow<SleepSessionWithSegments?>
    fun observeRecentSleepSessions(limit: Int): Flow<List<SleepSessionEntity>>
    fun observeTodayActivity(): Flow<List<ActivitySampleEntity>>
    fun observeActivityRange(days: Int): Flow<List<ActivitySampleEntity>>
    fun observeRecentWorkouts(limit: Int): Flow<List<WorkoutSessionEntity>>
    fun observeWorkout(id: Long): Flow<WorkoutSessionEntity?>
    fun observeRecentSyncLogs(limit: Int): Flow<List<DeviceSyncLogEntity>>
    suspend fun startWorkout(type: WorkoutType): Long
    suspend fun finishWorkout(id: Long, durationSeconds: Int, estimatedCalories: Int, avgHeartRate: Int?, maxHeartRate: Int?, status: WorkoutStatus)
    suspend fun addWaterIntake(ml: Int)
}
```

  `HealthDataRepositoryImpl` wraps the DAOs; Hilt-bound in `RepositoryModule`. `FakeHealthDataRepository` (test double with `MutableStateFlow`s per method) is consumed by every screen ViewModel test in Tasks 11–16.

- [ ] **Step 1: Write the failing test**

`app/src/test/java/com/vitalos/app/data/repository/HealthDataRepositoryImplTest.kt`:

```kotlin
package com.vitalos.app.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.vitalos.app.data.local.VitalOsDatabase
import com.vitalos.app.data.local.entity.DailyMetricsSnapshotEntity
import com.vitalos.app.data.local.entity.SleepSessionEntity
import com.vitalos.app.data.local.entity.SleepStageSegmentEntity
import com.vitalos.app.data.local.entity.SleepStageType
import com.vitalos.app.data.local.entity.WorkoutStatus
import com.vitalos.app.data.local.entity.WorkoutType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class HealthDataRepositoryImplTest {

    private lateinit var db: VitalOsDatabase
    private lateinit var repository: HealthDataRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, VitalOsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = HealthDataRepositoryImpl(
            profileDao = db.profileDao(),
            dailyMetricsDao = db.dailyMetricsDao(),
            heartRateDao = db.heartRateDao(),
            sleepDao = db.sleepDao(),
            activityDao = db.activityDao(),
            workoutDao = db.workoutDao(),
            deviceSyncLogDao = db.deviceSyncLogDao(),
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun observeTodaySleepSession_combinesSessionAndSegments() = runTest {
        val today = LocalDate.now().toEpochDay()
        val sessionId = db.sleepDao().insertSession(
            SleepSessionEntity(dateEpochDay = today, startEpochMillis = 0, endEpochMillis = 100, efficiencyPercent = 96.0, awakenings = 0, sleepScore = 91),
        )
        db.sleepDao().insertSegments(
            listOf(SleepStageSegmentEntity(sessionId = sessionId, stage = SleepStageType.DEEP, durationMinutes = 100, orderIndex = 0)),
        )

        repository.observeTodaySleepSession().test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals(91, result?.session?.sleepScore)
            assertEquals(1, result?.segments?.size)
        }
    }

    @Test
    fun startWorkout_thenFinishWorkout_updatesStatusAndMetrics() = runTest {
        val id = repository.startWorkout(WorkoutType.YOGA)
        repository.finishWorkout(id, durationSeconds = 1800, estimatedCalories = 180, avgHeartRate = 98, maxHeartRate = 120, status = WorkoutStatus.COMPLETED)

        repository.observeWorkout(id).test {
            val workout = awaitItem()
            assertEquals(WorkoutStatus.COMPLETED, workout?.status)
            assertEquals(180, workout?.estimatedCalories)
        }
    }

    @Test
    fun addWaterIntake_incrementsTodaySnapshotHydration() = runTest {
        val today = LocalDate.now().toEpochDay()
        db.dailyMetricsDao().upsert(
            DailyMetricsSnapshotEntity(
                dateEpochDay = today, readinessPercent = 90, healthScore = 85, strainTarget = 12.0,
                steps = 1000, distanceMeters = 800.0, activeCalories = 100, hydrationMl = 500,
                proteinGrams = 10, carbsGrams = 10, fatGrams = 10, calorieTarget = 2400, caloriesConsumed = 300,
            ),
        )

        repository.addWaterIntake(250)

        repository.observeTodaySnapshot().test {
            assertEquals(750, awaitItem()?.hydrationMl)
        }
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.data.repository.HealthDataRepositoryImplTest"`
Expected: FAIL — `HealthDataRepositoryImpl` is unresolved.

- [ ] **Step 3: Write the repository interface**

`app/src/main/java/com/vitalos/app/data/repository/HealthDataRepository.kt`:

```kotlin
package com.vitalos.app.data.repository

import com.vitalos.app.data.local.entity.ActivitySampleEntity
import com.vitalos.app.data.local.entity.DailyMetricsSnapshotEntity
import com.vitalos.app.data.local.entity.DeviceSyncLogEntity
import com.vitalos.app.data.local.entity.HeartRateSampleEntity
import com.vitalos.app.data.local.entity.ProfileEntity
import com.vitalos.app.data.local.entity.SleepSessionEntity
import com.vitalos.app.data.local.entity.SleepStageSegmentEntity
import com.vitalos.app.data.local.entity.WorkoutSessionEntity
import com.vitalos.app.data.local.entity.WorkoutStatus
import com.vitalos.app.data.local.entity.WorkoutType
import kotlinx.coroutines.flow.Flow

data class SleepSessionWithSegments(
    val session: SleepSessionEntity,
    val segments: List<SleepStageSegmentEntity>,
)

interface HealthDataRepository {
    fun observeProfile(): Flow<ProfileEntity?>
    fun observeTodaySnapshot(): Flow<DailyMetricsSnapshotEntity?>
    fun observeSnapshotRange(days: Int): Flow<List<DailyMetricsSnapshotEntity>>
    fun observeRecentHeartRate(windowMinutes: Int): Flow<List<HeartRateSampleEntity>>
    fun observeLatestHeartRate(): Flow<HeartRateSampleEntity?>
    fun observeTodaySleepSession(): Flow<SleepSessionWithSegments?>
    fun observeRecentSleepSessions(limit: Int): Flow<List<SleepSessionEntity>>
    fun observeTodayActivity(): Flow<List<ActivitySampleEntity>>
    fun observeActivityRange(days: Int): Flow<List<ActivitySampleEntity>>
    fun observeRecentWorkouts(limit: Int): Flow<List<WorkoutSessionEntity>>
    fun observeWorkout(id: Long): Flow<WorkoutSessionEntity?>
    fun observeRecentSyncLogs(limit: Int): Flow<List<DeviceSyncLogEntity>>
    suspend fun startWorkout(type: WorkoutType): Long
    suspend fun finishWorkout(
        id: Long,
        durationSeconds: Int,
        estimatedCalories: Int,
        avgHeartRate: Int?,
        maxHeartRate: Int?,
        status: WorkoutStatus,
    )
    suspend fun addWaterIntake(ml: Int)
}
```

- [ ] **Step 4: Write the implementation**

`app/src/main/java/com/vitalos/app/data/repository/HealthDataRepositoryImpl.kt`:

```kotlin
package com.vitalos.app.data.repository

import com.vitalos.app.data.local.dao.ActivityDao
import com.vitalos.app.data.local.dao.DailyMetricsDao
import com.vitalos.app.data.local.dao.DeviceSyncLogDao
import com.vitalos.app.data.local.dao.HeartRateDao
import com.vitalos.app.data.local.dao.ProfileDao
import com.vitalos.app.data.local.dao.SleepDao
import com.vitalos.app.data.local.dao.WorkoutDao
import com.vitalos.app.data.local.entity.ActivitySampleEntity
import com.vitalos.app.data.local.entity.DailyMetricsSnapshotEntity
import com.vitalos.app.data.local.entity.DeviceSyncLogEntity
import com.vitalos.app.data.local.entity.HeartRateSampleEntity
import com.vitalos.app.data.local.entity.ProfileEntity
import com.vitalos.app.data.local.entity.SleepSessionEntity
import com.vitalos.app.data.local.entity.WorkoutSessionEntity
import com.vitalos.app.data.local.entity.WorkoutStatus
import com.vitalos.app.data.local.entity.WorkoutType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthDataRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao,
    private val dailyMetricsDao: DailyMetricsDao,
    private val heartRateDao: HeartRateDao,
    private val sleepDao: SleepDao,
    private val activityDao: ActivityDao,
    private val workoutDao: WorkoutDao,
    private val deviceSyncLogDao: DeviceSyncLogDao,
) : HealthDataRepository {

    private fun todayEpochDay() = LocalDate.now().toEpochDay()

    override fun observeProfile(): Flow<ProfileEntity?> = profileDao.observe()

    override fun observeTodaySnapshot(): Flow<DailyMetricsSnapshotEntity?> =
        dailyMetricsDao.observeByDate(todayEpochDay())

    override fun observeSnapshotRange(days: Int): Flow<List<DailyMetricsSnapshotEntity>> {
        val today = todayEpochDay()
        return dailyMetricsDao.observeRange(today - (days - 1), today)
    }

    override fun observeRecentHeartRate(windowMinutes: Int): Flow<List<HeartRateSampleEntity>> =
        heartRateDao.observeSince(System.currentTimeMillis() - windowMinutes * 60_000L)

    override fun observeLatestHeartRate(): Flow<HeartRateSampleEntity?> = heartRateDao.observeLatest()

    override fun observeTodaySleepSession(): Flow<SleepSessionWithSegments?> =
        sleepDao.observeSessionForDate(todayEpochDay()).flatMapLatest { session ->
            if (session == null) {
                kotlinx.coroutines.flow.flowOf(null)
            } else {
                sleepDao.observeSegments(session.id).map { segments -> SleepSessionWithSegments(session, segments) }
            }
        }

    override fun observeRecentSleepSessions(limit: Int): Flow<List<SleepSessionEntity>> =
        sleepDao.observeRecentSessions(limit)

    override fun observeTodayActivity(): Flow<List<ActivitySampleEntity>> =
        activityDao.observeForDate(todayEpochDay())

    override fun observeActivityRange(days: Int): Flow<List<ActivitySampleEntity>> {
        val today = todayEpochDay()
        return activityDao.observeRange(today - (days - 1), today)
    }

    override fun observeRecentWorkouts(limit: Int): Flow<List<WorkoutSessionEntity>> =
        workoutDao.observeRecent(limit)

    override fun observeWorkout(id: Long): Flow<WorkoutSessionEntity?> = workoutDao.observeById(id)

    override fun observeRecentSyncLogs(limit: Int): Flow<List<DeviceSyncLogEntity>> =
        deviceSyncLogDao.observeRecent(limit)

    override suspend fun startWorkout(type: WorkoutType): Long = workoutDao.insert(
        WorkoutSessionEntity(
            type = type,
            startEpochMillis = System.currentTimeMillis(),
            endEpochMillis = null,
            durationSeconds = 0,
            estimatedCalories = 0,
            avgHeartRate = null,
            maxHeartRate = null,
            status = WorkoutStatus.IN_PROGRESS,
        ),
    )

    override suspend fun finishWorkout(
        id: Long,
        durationSeconds: Int,
        estimatedCalories: Int,
        avgHeartRate: Int?,
        maxHeartRate: Int?,
        status: WorkoutStatus,
    ) {
        val existing = workoutDao.observeById(id)
        val current = kotlinx.coroutines.flow.first(existing) ?: return
        workoutDao.update(
            current.copy(
                endEpochMillis = System.currentTimeMillis(),
                durationSeconds = durationSeconds,
                estimatedCalories = estimatedCalories,
                avgHeartRate = avgHeartRate,
                maxHeartRate = maxHeartRate,
                status = status,
            ),
        )
    }

    override suspend fun addWaterIntake(ml: Int) {
        val today = todayEpochDay()
        val current = kotlinx.coroutines.flow.first(dailyMetricsDao.observeByDate(today))
        val updated = (current ?: DailyMetricsSnapshotEntity(
            dateEpochDay = today, readinessPercent = 0, healthScore = 0, strainTarget = 0.0,
            steps = 0, distanceMeters = 0.0, activeCalories = 0, hydrationMl = 0,
            proteinGrams = 0, carbsGrams = 0, fatGrams = 0, calorieTarget = 2400, caloriesConsumed = 0,
        )).copy(hydrationMl = (current?.hydrationMl ?: 0) + ml)
        dailyMetricsDao.upsert(updated)
    }
}
```

- [ ] **Step 5: Write the Hilt repository module**

`app/src/main/java/com/vitalos/app/di/RepositoryModule.kt`:

```kotlin
package com.vitalos.app.di

import com.vitalos.app.data.repository.HealthDataRepository
import com.vitalos.app.data.repository.HealthDataRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindHealthDataRepository(impl: HealthDataRepositoryImpl): HealthDataRepository
}
```

- [ ] **Step 6: Run the test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.data.repository.HealthDataRepositoryImplTest"`
Expected: `BUILD SUCCESSFUL`, 3 tests passed.

- [ ] **Step 7: Write the fake test double for ViewModel tests**

`app/src/test/java/com/vitalos/app/testing/FakeHealthDataRepository.kt`:

```kotlin
package com.vitalos.app.testing

import com.vitalos.app.data.local.entity.ActivitySampleEntity
import com.vitalos.app.data.local.entity.DailyMetricsSnapshotEntity
import com.vitalos.app.data.local.entity.DeviceSyncLogEntity
import com.vitalos.app.data.local.entity.HeartRateSampleEntity
import com.vitalos.app.data.local.entity.ProfileEntity
import com.vitalos.app.data.local.entity.SleepSessionEntity
import com.vitalos.app.data.local.entity.WorkoutSessionEntity
import com.vitalos.app.data.local.entity.WorkoutStatus
import com.vitalos.app.data.local.entity.WorkoutType
import com.vitalos.app.data.repository.HealthDataRepository
import com.vitalos.app.data.repository.SleepSessionWithSegments
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeHealthDataRepository : HealthDataRepository {
    val profileFlow = MutableStateFlow<ProfileEntity?>(null)
    val todaySnapshotFlow = MutableStateFlow<DailyMetricsSnapshotEntity?>(null)
    val snapshotRangeFlow = MutableStateFlow<List<DailyMetricsSnapshotEntity>>(emptyList())
    val recentHeartRateFlow = MutableStateFlow<List<HeartRateSampleEntity>>(emptyList())
    val latestHeartRateFlow = MutableStateFlow<HeartRateSampleEntity?>(null)
    val todaySleepSessionFlow = MutableStateFlow<SleepSessionWithSegments?>(null)
    val recentSleepSessionsFlow = MutableStateFlow<List<SleepSessionEntity>>(emptyList())
    val todayActivityFlow = MutableStateFlow<List<ActivitySampleEntity>>(emptyList())
    val activityRangeFlow = MutableStateFlow<List<ActivitySampleEntity>>(emptyList())
    val recentWorkoutsFlow = MutableStateFlow<List<WorkoutSessionEntity>>(emptyList())
    val workoutFlows = mutableMapOf<Long, MutableStateFlow<WorkoutSessionEntity?>>()
    val recentSyncLogsFlow = MutableStateFlow<List<DeviceSyncLogEntity>>(emptyList())

    var nextWorkoutId = 1L
    var waterAddedMl = 0

    override fun observeProfile(): StateFlow<ProfileEntity?> = profileFlow
    override fun observeTodaySnapshot(): StateFlow<DailyMetricsSnapshotEntity?> = todaySnapshotFlow
    override fun observeSnapshotRange(days: Int): StateFlow<List<DailyMetricsSnapshotEntity>> = snapshotRangeFlow
    override fun observeRecentHeartRate(windowMinutes: Int): StateFlow<List<HeartRateSampleEntity>> = recentHeartRateFlow
    override fun observeLatestHeartRate(): StateFlow<HeartRateSampleEntity?> = latestHeartRateFlow
    override fun observeTodaySleepSession(): StateFlow<SleepSessionWithSegments?> = todaySleepSessionFlow
    override fun observeRecentSleepSessions(limit: Int): StateFlow<List<SleepSessionEntity>> = recentSleepSessionsFlow
    override fun observeTodayActivity(): StateFlow<List<ActivitySampleEntity>> = todayActivityFlow
    override fun observeActivityRange(days: Int): StateFlow<List<ActivitySampleEntity>> = activityRangeFlow
    override fun observeRecentWorkouts(limit: Int): StateFlow<List<WorkoutSessionEntity>> = recentWorkoutsFlow
    override fun observeWorkout(id: Long): StateFlow<WorkoutSessionEntity?> =
        workoutFlows.getOrPut(id) { MutableStateFlow(null) }
    override fun observeRecentSyncLogs(limit: Int): StateFlow<List<DeviceSyncLogEntity>> = recentSyncLogsFlow

    override suspend fun startWorkout(type: WorkoutType): Long {
        val id = nextWorkoutId++
        workoutFlows[id] = MutableStateFlow(
            WorkoutSessionEntity(id = id, type = type, startEpochMillis = 0, endEpochMillis = null, durationSeconds = 0, estimatedCalories = 0, avgHeartRate = null, maxHeartRate = null, status = WorkoutStatus.IN_PROGRESS),
        )
        return id
    }

    override suspend fun finishWorkout(id: Long, durationSeconds: Int, estimatedCalories: Int, avgHeartRate: Int?, maxHeartRate: Int?, status: WorkoutStatus) {
        val flow = workoutFlows.getOrPut(id) { MutableStateFlow(null) }
        val current = flow.value ?: return
        flow.value = current.copy(durationSeconds = durationSeconds, estimatedCalories = estimatedCalories, avgHeartRate = avgHeartRate, maxHeartRate = maxHeartRate, status = status)
    }

    override suspend fun addWaterIntake(ml: Int) {
        waterAddedMl += ml
    }
}
```

Note: this fake's override return types (`StateFlow<...>`) are narrower than the interface's `Flow<...>` return types, which Kotlin permits for interface implementation (covariant return types) — callers still see `Flow<...>` through the interface.

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/vitalos/app/data/repository app/src/main/java/com/vitalos/app/di/RepositoryModule.kt app/src/test/java/com/vitalos/app/testing/FakeHealthDataRepository.kt app/src/test/java/com/vitalos/app/data/repository
git commit -m "Add HealthDataRepository with Hilt binding"
```

---

## Task 7: WorkManager BLE Sync Worker

**Files:**
- Create: `app/src/main/java/com/vitalos/app/sync/BleSyncWorker.kt`
- Create: `app/src/main/java/com/vitalos/app/sync/SyncScheduler.kt`
- Modify: `app/src/main/java/com/vitalos/app/VitalOsApplication.kt`
- Test: `app/src/test/java/com/vitalos/app/sync/BleSyncWorkerTest.kt`

**Interfaces:**
- Consumes: `BleRepository` (Task 5).
- Produces: `@HiltWorker class BleSyncWorker(context, params, private val bleRepository: BleRepository) : CoroutineWorker` whose `doWork()` calls `bleRepository.sync()` and returns `Result.success()`/`Result.retry()`. `object SyncScheduler { fun schedulePeriodicSync(context: Context) }` enqueues a 15-minute periodic `WorkRequest` with a unique name `"ble_periodic_sync"`, `ExistingPeriodicWorkPolicy.KEEP`.

- [ ] **Step 1: Write the failing test**

`app/src/test/java/com/vitalos/app/sync/BleSyncWorkerTest.kt`:

```kotlin
package com.vitalos.app.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.vitalos.app.data.ble.SyncResult
import com.vitalos.app.testing.FakeBleRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BleSyncWorkerTest {

    @Test
    fun doWork_whenSyncSucceeds_returnsSuccess() = runTest {
        val fakeBle = FakeBleRepository().apply {
            syncResult = SyncResult(recordsMerged = 5, success = true)
        }
        val context = ApplicationProvider.getApplicationContext<Context>()
        val worker = TestListenableWorkerBuilder<BleSyncWorker>(context)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(appContext: Context, workerClassName: String, workerParameters: androidx.work.WorkerParameters) =
                    BleSyncWorker(appContext, workerParameters, fakeBle)
            })
            .build()

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assert(fakeBle.syncCalled)
    }

    @Test
    fun doWork_whenSyncFails_returnsRetry() = runTest {
        val fakeBle = FakeBleRepository().apply {
            syncResult = SyncResult(recordsMerged = 0, success = false, errorMessage = "No connected device")
        }
        val context = ApplicationProvider.getApplicationContext<Context>()
        val worker = TestListenableWorkerBuilder<BleSyncWorker>(context)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(appContext: Context, workerClassName: String, workerParameters: androidx.work.WorkerParameters) =
                    BleSyncWorker(appContext, workerParameters, fakeBle)
            })
            .build()

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.sync.BleSyncWorkerTest"`
Expected: FAIL — `BleSyncWorker` is unresolved.

- [ ] **Step 3: Write the worker**

`app/src/main/java/com/vitalos/app/sync/BleSyncWorker.kt`:

```kotlin
package com.vitalos.app.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vitalos.app.data.ble.BleRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class BleSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val bleRepository: BleRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val result = bleRepository.sync()
        return if (result.success) Result.success() else Result.retry()
    }
}
```

- [ ] **Step 4: Write the scheduler**

`app/src/main/java/com/vitalos/app/sync/SyncScheduler.kt`:

```kotlin
package com.vitalos.app.sync

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SyncScheduler {
    private const val UNIQUE_WORK_NAME = "ble_periodic_sync"

    fun schedulePeriodicSync(context: Context) {
        val request = PeriodicWorkRequestBuilder<BleSyncWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.sync.BleSyncWorkerTest"`
Expected: `BUILD SUCCESSFUL`, 2 tests passed.

- [ ] **Step 6: Schedule the periodic sync at app startup**

Modify `app/src/main/java/com/vitalos/app/VitalOsApplication.kt` — add to `onCreate()`, after the seeding `launch` block:

```kotlin
        com.vitalos.app.sync.SyncScheduler.schedulePeriodicSync(this)
```

- [ ] **Step 7: Verify the app still builds**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/vitalos/app/sync app/src/main/java/com/vitalos/app/VitalOsApplication.kt app/src/test/java/com/vitalos/app/sync
git commit -m "Add periodic BLE sync worker"
```

---

## Task 8: Reusable Visual Components

**Files:**
- Create: `app/src/main/java/com/vitalos/app/components/VitalCard.kt`
- Create: `app/src/main/java/com/vitalos/app/components/StatusPill.kt`
- Create: `app/src/main/java/com/vitalos/app/components/ConcentricRingGauge.kt`
- Create: `app/src/main/java/com/vitalos/app/components/SleepHypnogramBar.kt`
- Create: `app/src/main/java/com/vitalos/app/components/HrSparkline.kt`
- Create: `app/src/main/java/com/vitalos/app/components/SegmentedProgressBar.kt`

**Interfaces:**
- Consumes: `VitalOsTheme` (Task 2), `SleepStageSegmentEntity`/`SleepStageType` (Task 3).
- Produces:
  - `@Composable fun VitalCard(modifier: Modifier = Modifier, backgroundColor: Color = VitalOsTheme.colors.cardSurface, content: @Composable ColumnScope.() -> Unit)`
  - `@Composable fun StatusPill(text: String, dotColor: Color, modifier: Modifier = Modifier, pulsing: Boolean = false)`
  - `data class RingSpec(val progress: Float, val color: Color, val strokeWidth: Dp)`
  - `@Composable fun ConcentricRingGauge(rings: List<RingSpec>, modifier: Modifier = Modifier, centerContent: @Composable () -> Unit)`
  - `@Composable fun SleepHypnogramBar(segments: List<SleepStageSegmentEntity>, modifier: Modifier = Modifier)`
  - `@Composable fun HrSparkline(samples: List<Int>, modifier: Modifier = Modifier, lineColor: Color = VitalOsTheme.colors.readinessLime)`
  - `@Composable fun SegmentedProgressBar(progress: Float, modifier: Modifier = Modifier, trackColor: Color = VitalOsTheme.colors.surfaceContainerHighest, fillColor: Color = VitalOsTheme.colors.readinessLime)`

  These are consumed by every screen task (9, 11–16).

- [ ] **Step 1: Write `VitalCard`**

`app/src/main/java/com/vitalos/app/components/VitalCard.kt`:

```kotlin
package com.vitalos.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.vitalos.app.designsystem.VitalOsTheme

@Composable
fun VitalCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = VitalOsTheme.colors.cardSurface,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(VitalOsTheme.shapes.card)
            .background(backgroundColor)
            .padding(VitalOsTheme.spacing.md),
        content = content,
    )
}
```

- [ ] **Step 2: Write `StatusPill`**

`app/src/main/java/com/vitalos/app/components/StatusPill.kt`:

```kotlin
package com.vitalos.app.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vitalos.app.designsystem.VitalOsTheme

@Composable
fun StatusPill(
    text: String,
    dotColor: Color,
    modifier: Modifier = Modifier,
    pulsing: Boolean = false,
) {
    val alpha = if (pulsing) {
        val transition = rememberInfiniteTransition(label = "status-pill-pulse")
        transition.animateFloat(
            initialValue = 1f,
            targetValue = 0.4f,
            animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "status-pill-alpha",
        ).value
    } else {
        1f
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(VitalOsTheme.shapes.pill)
            .background(VitalOsTheme.colors.surfaceContainerLow)
            .padding(horizontal = VitalOsTheme.spacing.sm, vertical = VitalOsTheme.spacing.xs),
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(6.dp)
                .alpha(alpha)
                .clip(CircleShape)
                .background(dotColor),
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(VitalOsTheme.spacing.xs))
        Text(text = text, style = VitalOsTheme.typography.dataTabularSm, color = VitalOsTheme.colors.onSurface)
    }
}
```

- [ ] **Step 3: Write `ConcentricRingGauge`**

`app/src/main/java/com/vitalos/app/components/ConcentricRingGauge.kt`:

```kotlin
package com.vitalos.app.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vitalos.app.designsystem.VitalOsTheme
import kotlin.math.min

data class RingSpec(val progress: Float, val color: Color, val strokeWidth: Dp)

@Composable
fun ConcentricRingGauge(
    rings: List<RingSpec>,
    modifier: Modifier = Modifier,
    centerContent: @Composable () -> Unit = {},
) {
    Box(modifier = modifier.size(176.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(176.dp)) {
            val maxStroke = rings.maxOfOrNull { it.strokeWidth.toPx() } ?: 0f
            val baseRadius = (min(size.width, size.height) - maxStroke) / 2f

            rings.forEachIndexed { index, ring ->
                val radius = baseRadius - index * (baseRadius * 0.32f / rings.size.coerceAtLeast(1))
                val strokePx = ring.strokeWidth.toPx()
                val trackStyle = Stroke(width = strokePx, cap = androidx.compose.ui.graphics.StrokeCap.Round)

                drawCircle(
                    color = VitalOsTheme.colors.surfaceContainerHigh,
                    radius = radius,
                    style = trackStyle,
                )
                drawArc(
                    color = ring.color,
                    startAngle = -90f,
                    sweepAngle = 360f * ring.progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = strokePx, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                )
            }
        }
        centerContent()
    }
}
```

- [ ] **Step 4: Write `SleepHypnogramBar`**

`app/src/main/java/com/vitalos/app/components/SleepHypnogramBar.kt`:

```kotlin
package com.vitalos.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.weight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.vitalos.app.data.local.entity.SleepStageSegmentEntity
import com.vitalos.app.data.local.entity.SleepStageType
import com.vitalos.app.designsystem.VitalOsTheme

private fun colorFor(stage: SleepStageType) = when (stage) {
    SleepStageType.AWAKE -> VitalOsTheme.colors.criticalRed
    SleepStageType.LIGHT -> VitalOsTheme.colors.recoveryMint.copy(alpha = 0.6f)
    SleepStageType.REM -> VitalOsTheme.colors.recoveryMint
    SleepStageType.DEEP -> VitalOsTheme.colors.diagnosticBlue
}

@Composable
fun SleepHypnogramBar(
    segments: List<SleepStageSegmentEntity>,
    modifier: Modifier = Modifier,
) {
    val totalMinutes = segments.sumOf { it.durationMinutes }.coerceAtLeast(1)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(VitalOsTheme.shapes.pill)
            .background(VitalOsTheme.colors.surfaceContainerHighest),
    ) {
        segments.forEach { segment ->
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .weight(segment.durationMinutes.toFloat() / totalMinutes)
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(colorFor(segment.stage)),
            )
        }
    }
}
```

- [ ] **Step 5: Write `HrSparkline`**

`app/src/main/java/com/vitalos/app/components/HrSparkline.kt`:

```kotlin
package com.vitalos.app.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.vitalos.app.designsystem.VitalOsTheme

@Composable
fun HrSparkline(
    samples: List<Int>,
    modifier: Modifier = Modifier,
    lineColor: Color = VitalOsTheme.colors.readinessLime,
) {
    if (samples.size < 2) return
    val minValue = samples.min().toFloat()
    val maxValue = samples.max().toFloat().coerceAtLeast(minValue + 1f)

    Canvas(modifier = modifier.fillMaxWidth().height(64.dp)) {
        val stepX = size.width / (samples.size - 1)
        fun yFor(value: Int): Float {
            val normalized = (value - minValue) / (maxValue - minValue)
            return size.height - (normalized * size.height)
        }

        val linePath = Path().apply {
            moveTo(0f, yFor(samples[0]))
            samples.forEachIndexed { index, value ->
                if (index > 0) lineTo(index * stepX, yFor(value))
            }
        }
        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        drawPath(path = fillPath, color = lineColor.copy(alpha = 0.18f), style = Fill)
        drawPath(path = linePath, color = lineColor, style = Stroke(width = 4f))
        drawCircle(color = lineColor, radius = 6f, center = Offset(size.width, yFor(samples.last())))
    }
}
```

- [ ] **Step 6: Write `SegmentedProgressBar`**

`app/src/main/java/com/vitalos/app/components/SegmentedProgressBar.kt`:

```kotlin
package com.vitalos.app.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vitalos.app.designsystem.VitalOsTheme

@Composable
fun SegmentedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = VitalOsTheme.colors.surfaceContainerHighest,
    fillColor: Color = VitalOsTheme.colors.readinessLime,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(700),
        label = "segmented-progress",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(VitalOsTheme.shapes.pill)
            .background(trackColor),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(VitalOsTheme.shapes.pill)
                .background(fillColor),
        )
    }
}
```

- [ ] **Step 7: Verify it compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/vitalos/app/components
git commit -m "Add reusable VitalOS chart and card components"
```

---

## Task 9: Navigation Shell (Routes, NavHost, Bottom Bar, Quick-Add Sheet, Placeholders)

**Files:**
- Create: `app/src/main/java/com/vitalos/app/navigation/VitalOsRoutes.kt`
- Create: `app/src/main/java/com/vitalos/app/navigation/ComingSoonScreen.kt`
- Create: `app/src/main/java/com/vitalos/app/navigation/VitalOsBottomBar.kt`
- Create: `app/src/main/java/com/vitalos/app/navigation/QuickAddSheet.kt`
- Create: `app/src/main/java/com/vitalos/app/navigation/QuickAddViewModel.kt`
- Create: `app/src/main/java/com/vitalos/app/navigation/VitalOsNavHost.kt`
- Modify: `app/src/main/java/com/vitalos/app/MainActivity.kt`

**Interfaces:**
- Consumes: `HealthDataRepository.addWaterIntake`/`startWorkout` (Task 6), `VitalOsAppTheme` (Task 2).
- Produces:
  - `object VitalOsRoutes` with `HOME`, `ACTIVITY`, `HEALTH`, `INSIGHTS`, `PROFILE`, `WORKOUT_HUB`, `WORKOUT_ACTIVE` (`"workout_active/{workoutId}"`), `WORKOUT_SUMMARY` (`"workout_summary/{workoutId}"`), `DEVICE_MANAGEMENT`, plus `fun workoutActive(workoutId: Long): String` and `fun workoutSummary(workoutId: Long): String`.
  - `sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector)` with `Home`, `Activity`, `Health`, `Insights`, `Profile` and `companion object { val items: List<BottomNavItem> }`.
  - `@Composable fun VitalOsNavHost(navController: NavHostController, modifier: Modifier = Modifier)` — every route resolves to `ComingSoonScreen` in this task; Tasks 11–16 each modify exactly one `composable(...)` block to swap in the real screen.
  - `MainActivity` now sets content to `VitalOsAppTheme { VitalOsAppScaffold() }`, with a `Scaffold` hosting `VitalOsBottomBar`, a `SnackbarHost`, and `VitalOsNavHost`.

- [ ] **Step 1: Write the route definitions**

`app/src/main/java/com/vitalos/app/navigation/VitalOsRoutes.kt`:

```kotlin
package com.vitalos.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Cottage
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.ui.graphics.vector.ImageVector

object VitalOsRoutes {
    const val HOME = "home"
    const val ACTIVITY = "activity"
    const val HEALTH = "health"
    const val INSIGHTS = "insights"
    const val PROFILE = "profile"
    const val WORKOUT_HUB = "workout_hub"
    const val WORKOUT_ACTIVE = "workout_active/{workoutId}"
    const val WORKOUT_SUMMARY = "workout_summary/{workoutId}"
    const val DEVICE_MANAGEMENT = "device_management"

    fun workoutActive(workoutId: Long) = "workout_active/$workoutId"
    fun workoutSummary(workoutId: Long) = "workout_summary/$workoutId"
}

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavItem(VitalOsRoutes.HOME, "Home", Icons.Outlined.Cottage)
    object Activity : BottomNavItem(VitalOsRoutes.ACTIVITY, "Activity", Icons.Outlined.LocalFireDepartment)
    object Health : BottomNavItem(VitalOsRoutes.HEALTH, "Health", Icons.Outlined.MonitorHeart)
    object Insights : BottomNavItem(VitalOsRoutes.INSIGHTS, "Insights", Icons.Outlined.AutoAwesome)
    object Profile : BottomNavItem(VitalOsRoutes.PROFILE, "Profile", Icons.Outlined.Tune)

    companion object {
        val items: List<BottomNavItem> = listOf(Home, Activity, Health, Insights, Profile)
    }
}
```

- [ ] **Step 2: Write the placeholder screen**

`app/src/main/java/com/vitalos/app/navigation/ComingSoonScreen.kt`:

```kotlin
package com.vitalos.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.vitalos.app.designsystem.VitalOsTheme

@Composable
fun ComingSoonScreen(title: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VitalOsTheme.colors.voidBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(title, style = VitalOsTheme.typography.headlineMd, color = VitalOsTheme.colors.onSurface)
        Text("Coming in a later phase", style = VitalOsTheme.typography.bodyMd, color = VitalOsTheme.colors.textSecondary)
    }
}
```

- [ ] **Step 3: Write the bottom bar**

`app/src/main/java/com/vitalos/app/navigation/VitalOsBottomBar.kt`:

```kotlin
package com.vitalos.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.vitalos.app.designsystem.VitalOsTheme

@Composable
fun VitalOsBottomBar(
    navController: NavHostController,
    onQuickAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(VitalOsTheme.colors.surfaceContainerLowest),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            val leftItems = BottomNavItem.items.take(2)
            val rightItems = BottomNavItem.items.takeLast(3)

            leftItems.forEach { item ->
                BottomNavTab(item, currentRoute?.hierarchy?.any { it.route == item.route } == true) {
                    navController.navigateToTab(item.route)
                }
            }
            Box(modifier = Modifier.size(56.dp))
            rightItems.forEach { item ->
                BottomNavTab(item, currentRoute?.hierarchy?.any { it.route == item.route } == true) {
                    navController.navigateToTab(item.route)
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(VitalOsTheme.colors.readinessLime)
                .clickable(onClick = onQuickAddClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "Quick add", tint = VitalOsTheme.colors.onPrimaryContainer)
        }
    }
}

private fun NavController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun BottomNavTab(item: BottomNavItem, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(top = 8.dp)
            .clickable(onClick = onClick),
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = if (selected) VitalOsTheme.colors.readinessLime else VitalOsTheme.colors.textSecondary,
        )
        Text(
            item.label.uppercase(),
            style = VitalOsTheme.typography.labelCaps,
            color = if (selected) VitalOsTheme.colors.readinessLime else VitalOsTheme.colors.textSecondary,
        )
    }
}
```

- [ ] **Step 4: Write the Quick-Add sheet and its ViewModel**

`app/src/main/java/com/vitalos/app/navigation/QuickAddViewModel.kt`:

```kotlin
package com.vitalos.app.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalos.app.data.local.entity.WorkoutType
import com.vitalos.app.data.repository.HealthDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuickAddViewModel @Inject constructor(
    private val healthDataRepository: HealthDataRepository,
) : ViewModel() {

    fun logWater(onDone: () -> Unit) {
        viewModelScope.launch {
            healthDataRepository.addWaterIntake(250)
            onDone()
        }
    }

    fun startYogaSession(onStarted: (Long) -> Unit) {
        viewModelScope.launch {
            val id = healthDataRepository.startWorkout(WorkoutType.YOGA)
            onStarted(id)
        }
    }
}
```

`app/src/main/java/com/vitalos/app/navigation/QuickAddSheet.kt`:

```kotlin
package com.vitalos.app.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vitalos.app.designsystem.VitalOsTheme

@Composable
fun QuickAddSheet(
    onDismiss: () -> Unit,
    onLogWater: () -> Unit,
    onStartYoga: () -> Unit,
    onRecordMeal: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = VitalOsTheme.colors.elevatedSurface) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = VitalOsTheme.spacing.lg)) {
            Text(
                "QUICK ADD",
                style = VitalOsTheme.typography.labelCaps,
                color = VitalOsTheme.colors.textSecondary,
                modifier = Modifier.padding(horizontal = VitalOsTheme.spacing.md),
            )
            ListItem(
                headlineContent = { Text("Log Water", color = VitalOsTheme.colors.onSurface) },
                leadingContent = { Icon(Icons.Outlined.LocalDrink, contentDescription = null, tint = VitalOsTheme.colors.diagnosticBlue) },
                modifier = Modifier.clickableRow(onLogWater),
            )
            ListItem(
                headlineContent = { Text("Start Yoga", color = VitalOsTheme.colors.onSurface) },
                leadingContent = { Icon(Icons.Outlined.SelfImprovement, contentDescription = null, tint = VitalOsTheme.colors.recoveryMint) },
                modifier = Modifier.clickableRow(onStartYoga),
            )
            ListItem(
                headlineContent = { Text("Record Meal", color = VitalOsTheme.colors.onSurface) },
                supportingContent = { Text("Coming in Phase 2", color = VitalOsTheme.colors.textSecondary) },
                leadingContent = { Icon(Icons.Outlined.Restaurant, contentDescription = null, tint = VitalOsTheme.colors.readinessLime) },
                modifier = Modifier.clickableRow(onRecordMeal),
            )
        }
    }
}

private fun Modifier.clickableRow(onClick: () -> Unit): Modifier =
    this.then(androidx.compose.foundation.clickable(onClick = onClick))
```

- [ ] **Step 5: Write the NavHost**

`app/src/main/java/com/vitalos/app/navigation/VitalOsNavHost.kt`:

```kotlin
package com.vitalos.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun VitalOsNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = VitalOsRoutes.HOME, modifier = modifier) {
        composable(VitalOsRoutes.HOME) { ComingSoonScreen("Home") }
        composable(VitalOsRoutes.ACTIVITY) { ComingSoonScreen("Activity") }
        composable(VitalOsRoutes.HEALTH) { ComingSoonScreen("Health") }
        composable(VitalOsRoutes.INSIGHTS) { ComingSoonScreen("Insights") }
        composable(VitalOsRoutes.PROFILE) { ComingSoonScreen("Profile") }
        composable(VitalOsRoutes.WORKOUT_HUB) { ComingSoonScreen("Workout Hub") }
        composable(
            VitalOsRoutes.WORKOUT_ACTIVE,
            arguments = listOf(navArgument("workoutId") { type = NavType.LongType }),
        ) { ComingSoonScreen("Active Session") }
        composable(
            VitalOsRoutes.WORKOUT_SUMMARY,
            arguments = listOf(navArgument("workoutId") { type = NavType.LongType }),
        ) { ComingSoonScreen("Workout Summary") }
        composable(VitalOsRoutes.DEVICE_MANAGEMENT) { ComingSoonScreen("TAGG Verve") }
    }
}
```

- [ ] **Step 6: Wire `MainActivity` to the real shell**

Replace the full contents of `app/src/main/java/com/vitalos/app/MainActivity.kt`:

```kotlin
package com.vitalos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.vitalos.app.designsystem.VitalOsAppTheme
import com.vitalos.app.navigation.QuickAddSheet
import com.vitalos.app.navigation.QuickAddViewModel
import com.vitalos.app.navigation.VitalOsBottomBar
import com.vitalos.app.navigation.VitalOsNavHost
import com.vitalos.app.navigation.VitalOsRoutes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VitalOsAppTheme {
                VitalOsAppScaffold()
            }
        }
    }
}

@Composable
private fun VitalOsAppScaffold() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showQuickAdd by remember { mutableStateOf(false) }
    val quickAddViewModel: QuickAddViewModel = hiltViewModel()

    Scaffold(
        bottomBar = {
            VitalOsBottomBar(navController = navController, onQuickAddClick = { showQuickAdd = true })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        VitalOsNavHost(navController = navController, modifier = Modifier.padding(innerPadding))
    }

    if (showQuickAdd) {
        QuickAddSheet(
            onDismiss = { showQuickAdd = false },
            onLogWater = {
                quickAddViewModel.logWater {
                    showQuickAdd = false
                    scope.launch { snackbarHostState.showSnackbar("+250 mL added") }
                }
            },
            onStartYoga = {
                quickAddViewModel.startYogaSession { workoutId ->
                    showQuickAdd = false
                    navController.navigate(VitalOsRoutes.workoutActive(workoutId))
                }
            },
            onRecordMeal = {
                showQuickAdd = false
                scope.launch { snackbarHostState.showSnackbar("Meal logging coming in Phase 2") }
            },
        )
    }
}
```

- [ ] **Step 7: Verify the app builds and launches**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`

Manually verify (emulator/device): app launches to a "Home" placeholder, all 5 bottom tabs navigate, and the center FAB opens the Quick-Add sheet.

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/vitalos/app/navigation app/src/main/java/com/vitalos/app/MainActivity.kt
git commit -m "Add navigation shell with bottom bar and Quick-Add sheet"
```

---

## Task 10: BLE Runtime Permissions

**Files:**
- Create: `app/src/main/java/com/vitalos/app/permissions/BlePermissions.kt`
- Test: `app/src/test/java/com/vitalos/app/permissions/BlePermissionsTest.kt`

**Interfaces:**
- Consumes: nothing.
- Produces: `object BlePermissions { fun required(sdkInt: Int = Build.VERSION.SDK_INT): Array<String> }` and `@Composable fun rememberBlePermissionState(): BlePermissionState` returning `class BlePermissionState(val allGranted: Boolean, val request: () -> Unit)`. Task 16 (Device Management screen) consumes `rememberBlePermissionState()`.

- [ ] **Step 1: Write the failing test**

`app/src/test/java/com/vitalos/app/permissions/BlePermissionsTest.kt`:

```kotlin
package com.vitalos.app.permissions

import android.Manifest
import android.os.Build
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class BlePermissionsTest {

    @Test
    fun required_onApi31OrAbove_returnsScanAndConnect() {
        val result = BlePermissions.required(sdkInt = Build.VERSION_CODES.S)
        assertArrayEquals(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT), result)
    }

    @Test
    fun required_belowApi31_returnsLegacyPermissions() {
        val result = BlePermissions.required(sdkInt = Build.VERSION_CODES.R)
        assertArrayEquals(
            arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION),
            result,
        )
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.permissions.BlePermissionsTest"`
Expected: FAIL — `BlePermissions` is unresolved.

- [ ] **Step 3: Write the permissions helper**

`app/src/main/java/com/vitalos/app/permissions/BlePermissions.kt`:

```kotlin
package com.vitalos.app.permissions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

object BlePermissions {
    fun required(sdkInt: Int = Build.VERSION.SDK_INT): Array<String> =
        if (sdkInt >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION)
        }
}

class BlePermissionState(val allGranted: Boolean, val request: () -> Unit)

@Composable
fun rememberBlePermissionState(): BlePermissionState {
    val context = LocalContext.current
    val permissions = remember { BlePermissions.required() }

    fun hasAll() = permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    var allGranted by remember { mutableStateOf(hasAll()) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        allGranted = result.values.all { it }
    }

    return BlePermissionState(allGranted = allGranted, request = { launcher.launch(permissions) })
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.permissions.BlePermissionsTest"`
Expected: `BUILD SUCCESSFUL`, 2 tests passed.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/vitalos/app/permissions app/src/test/java/com/vitalos/app/permissions
git commit -m "Add BLE runtime permission handling"
```

---

## Task 11: Home Dashboard

**Files:**
- Create: `app/src/main/java/com/vitalos/app/feature/home/HomeUiState.kt`
- Create: `app/src/main/java/com/vitalos/app/feature/home/HomeViewModel.kt`
- Create: `app/src/main/java/com/vitalos/app/feature/home/HomeScreen.kt`
- Modify: `app/src/main/java/com/vitalos/app/navigation/VitalOsNavHost.kt:composable(VitalOsRoutes.HOME)`
- Test: `app/src/test/java/com/vitalos/app/feature/home/HomeViewModelTest.kt`

**Interfaces:**
- Consumes: `HealthDataRepository` (Task 6), `BleRepository`/`BleConnectionPhase` (Task 5), `FakeHealthDataRepository`/`FakeBleRepository` (Tasks 5–6), `VitalCard`/`ConcentricRingGauge`/`RingSpec`/`SleepHypnogramBar`/`HrSparkline`/`SegmentedProgressBar`/`StatusPill` (Task 8).
- Produces: `data class HomeUiState(...)` (fields below), `@HiltViewModel class HomeViewModel` exposing `val uiState: StateFlow<HomeUiState>`, `fun quickAddWater()`, `fun manualSync()`; `@Composable fun HomeScreen(viewModel: HomeViewModel = hiltViewModel())`.

- [ ] **Step 1: Write the UI state**

`app/src/main/java/com/vitalos/app/feature/home/HomeUiState.kt`:

```kotlin
package com.vitalos.app.feature.home

import com.vitalos.app.data.local.entity.SleepStageSegmentEntity

data class HomeUiState(
    val isLoading: Boolean = true,
    val greetingName: String = "",
    val tierBadge: String = "",
    val readinessPercent: Int = 0,
    val healthScore: Int = 0,
    val strainTarget: Double = 0.0,
    val restingHr: Int = 0,
    val liveHr: Int? = null,
    val hrSparkline: List<Int> = emptyList(),
    val steps: Int = 0,
    val stepGoal: Int = 10000,
    val distanceMeters: Double = 0.0,
    val activeCalories: Int = 0,
    val sleepDurationMinutes: Int = 0,
    val sleepScore: Int = 0,
    val sleepSegments: List<SleepStageSegmentEntity> = emptyList(),
    val hydrationMl: Int = 0,
    val hydrationGoalMl: Int = 3000,
    val proteinGrams: Int = 0,
    val carbsGrams: Int = 0,
    val fatGrams: Int = 0,
    val calorieTarget: Int = 0,
    val caloriesConsumed: Int = 0,
    val bleConnected: Boolean = false,
    val bleStatusLabel: String = "DISCONNECTED",
)
```

- [ ] **Step 2: Write the failing ViewModel test**

`app/src/test/java/com/vitalos/app/feature/home/HomeViewModelTest.kt`:

```kotlin
package com.vitalos.app.feature.home

import app.cash.turbine.test
import com.vitalos.app.data.ble.BleConnectionPhase
import com.vitalos.app.data.ble.BleConnectionState
import com.vitalos.app.data.local.entity.DailyMetricsSnapshotEntity
import com.vitalos.app.data.local.entity.HeartRateSampleEntity
import com.vitalos.app.data.local.entity.ProfileEntity
import com.vitalos.app.testing.FakeBleRepository
import com.vitalos.app.testing.FakeHealthDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var healthRepo: FakeHealthDataRepository
    private lateinit var bleRepo: FakeBleRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        healthRepo = FakeHealthDataRepository()
        bleRepo = FakeBleRepository()
        viewModel = HomeViewModel(healthRepo, bleRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_mapsRepositoryDataIntoDashboardFields() = runTest {
        healthRepo.profileFlow.value = ProfileEntity(name = "Alex", tierBadge = "PRO LAB", dailyStepGoal = 10000, dailyHydrationGoalMl = 3000, dailyCalorieGoal = 2400)
        healthRepo.todaySnapshotFlow.value = DailyMetricsSnapshotEntity(
            dateEpochDay = 1, readinessPercent = 94, healthScore = 89, strainTarget = 14.5,
            steps = 8420, distanceMeters = 6200.0, activeCalories = 540, hydrationMl = 2100,
            proteinGrams = 142, carbsGrams = 180, fatGrams = 55, calorieTarget = 2400, caloriesConsumed = 1780,
        )
        healthRepo.recentHeartRateFlow.value = listOf(
            HeartRateSampleEntity(timestampEpochMillis = 1, bpm = 60, isResting = true),
            HeartRateSampleEntity(timestampEpochMillis = 2, bpm = 65, isResting = false),
        )
        bleRepo.connectionStateFlow.value = BleConnectionState(phase = BleConnectionPhase.CONNECTED)
        bleRepo.liveHeartRateFlow.value = 64

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()
            assertEquals("Alex", state.greetingName)
            assertEquals(94, state.readinessPercent)
            assertEquals(8420, state.steps)
            assertEquals(60, state.restingHr)
            assertEquals(64, state.liveHr)
            assertTrue(state.bleConnected)
        }
    }

    @Test
    fun quickAddWater_callsRepositoryAddWaterIntake() = runTest {
        viewModel.quickAddWater()
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(250, healthRepo.waterAddedMl)
    }

    @Test
    fun manualSync_callsBleRepositorySync() = runTest {
        viewModel.manualSync()
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(bleRepo.syncCalled)
    }
}
```

- [ ] **Step 3: Run the test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.feature.home.HomeViewModelTest"`
Expected: FAIL — `HomeViewModel` is unresolved.

- [ ] **Step 4: Write the ViewModel**

`app/src/main/java/com/vitalos/app/feature/home/HomeViewModel.kt`:

```kotlin
package com.vitalos.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalos.app.data.ble.BleConnectionPhase
import com.vitalos.app.data.ble.BleRepository
import com.vitalos.app.data.local.entity.DailyMetricsSnapshotEntity
import com.vitalos.app.data.local.entity.HeartRateSampleEntity
import com.vitalos.app.data.local.entity.ProfileEntity
import com.vitalos.app.data.repository.HealthDataRepository
import com.vitalos.app.data.repository.SleepSessionWithSegments
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val healthDataRepository: HealthDataRepository,
    private val bleRepository: BleRepository,
) : ViewModel() {

    private data class BaseData(
        val profile: ProfileEntity?,
        val snapshot: DailyMetricsSnapshotEntity?,
        val sleep: SleepSessionWithSegments?,
        val heartRateSamples: List<HeartRateSampleEntity>,
    )

    private val baseData = combine(
        healthDataRepository.observeProfile(),
        healthDataRepository.observeTodaySnapshot(),
        healthDataRepository.observeTodaySleepSession(),
        healthDataRepository.observeRecentHeartRate(60),
    ) { profile, snapshot, sleep, hr -> BaseData(profile, snapshot, sleep, hr) }

    val uiState: StateFlow<HomeUiState> = combine(
        baseData,
        bleRepository.connectionState,
        bleRepository.liveHeartRate,
    ) { base, bleState, liveHr ->
        HomeUiState(
            isLoading = false,
            greetingName = base.profile?.name.orEmpty(),
            tierBadge = base.profile?.tierBadge.orEmpty(),
            readinessPercent = base.snapshot?.readinessPercent ?: 0,
            healthScore = base.snapshot?.healthScore ?: 0,
            strainTarget = base.snapshot?.strainTarget ?: 0.0,
            restingHr = base.heartRateSamples.lastOrNull { it.isResting }?.bpm
                ?: base.heartRateSamples.lastOrNull()?.bpm ?: 0,
            liveHr = liveHr,
            hrSparkline = base.heartRateSamples.map { it.bpm },
            steps = base.snapshot?.steps ?: 0,
            stepGoal = base.profile?.dailyStepGoal ?: 10000,
            distanceMeters = base.snapshot?.distanceMeters ?: 0.0,
            activeCalories = base.snapshot?.activeCalories ?: 0,
            sleepDurationMinutes = base.sleep?.segments?.sumOf { it.durationMinutes } ?: 0,
            sleepScore = base.sleep?.session?.sleepScore ?: 0,
            sleepSegments = base.sleep?.segments ?: emptyList(),
            hydrationMl = base.snapshot?.hydrationMl ?: 0,
            hydrationGoalMl = base.profile?.dailyHydrationGoalMl ?: 3000,
            proteinGrams = base.snapshot?.proteinGrams ?: 0,
            carbsGrams = base.snapshot?.carbsGrams ?: 0,
            fatGrams = base.snapshot?.fatGrams ?: 0,
            calorieTarget = base.snapshot?.calorieTarget ?: 0,
            caloriesConsumed = base.snapshot?.caloriesConsumed ?: 0,
            bleConnected = bleState.phase == BleConnectionPhase.CONNECTED,
            bleStatusLabel = bleState.phase.name,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun quickAddWater() {
        viewModelScope.launch { healthDataRepository.addWaterIntake(250) }
    }

    fun manualSync() {
        viewModelScope.launch { bleRepository.sync() }
    }
}
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.feature.home.HomeViewModelTest"`
Expected: `BUILD SUCCESSFUL`, 3 tests passed.

- [ ] **Step 6: Write the screen**

`app/src/main/java/com/vitalos/app/feature/home/HomeScreen.kt`:

```kotlin
package com.vitalos.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.vitalos.app.components.ConcentricRingGauge
import com.vitalos.app.components.HrSparkline
import com.vitalos.app.components.RingSpec
import com.vitalos.app.components.SegmentedProgressBar
import com.vitalos.app.components.SleepHypnogramBar
import com.vitalos.app.components.StatusPill
import com.vitalos.app.components.VitalCard
import com.vitalos.app.designsystem.VitalOsTheme
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(VitalOsTheme.spacing.margin),
        verticalArrangement = Arrangement.spacedBy(VitalOsTheme.spacing.md),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Good morning, ${state.greetingName}", style = VitalOsTheme.typography.headlineLgMobile, color = VitalOsTheme.colors.readinessLime)
                    Text(state.tierBadge, style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.recoveryMint)
                }
                StatusPill(
                    text = if (state.bleConnected) "TAGG Verve · Connected" else "TAGG Verve · ${state.bleStatusLabel}",
                    dotColor = if (state.bleConnected) VitalOsTheme.colors.readinessLime else VitalOsTheme.colors.textDisabled,
                    pulsing = state.bleConnected,
                )
            }
        }

        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Text("COMPOSITE STATE", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
                ConcentricRingGauge(
                    modifier = Modifier.padding(vertical = VitalOsTheme.spacing.md),
                    rings = listOf(
                        RingSpec(progress = state.readinessPercent / 100f, color = VitalOsTheme.colors.readinessLime, strokeWidth = 8.dp),
                        RingSpec(progress = state.healthScore / 100f, color = VitalOsTheme.colors.recoveryMint, strokeWidth = 8.dp),
                        RingSpec(progress = (state.strainTarget / 20.0).toFloat(), color = VitalOsTheme.colors.diagnosticBlue, strokeWidth = 6.dp),
                    ),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${state.readinessPercent}%", style = VitalOsTheme.typography.dataTabularLg, color = VitalOsTheme.colors.readinessLime)
                        Text("READINESS", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
                    }
                }
                Text(
                    "Resting HR ${state.restingHr} bpm  ·  Health ${state.healthScore}  ·  Strain ${state.strainTarget}",
                    style = VitalOsTheme.typography.dataTabularSm,
                    color = VitalOsTheme.colors.onSurface,
                )
            }
        }

        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("DAILY ACTIVITY", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
                    Text("${state.steps} / ${state.stepGoal} steps", style = VitalOsTheme.typography.dataTabularSm, color = VitalOsTheme.colors.readinessLime)
                }
                SegmentedProgressBar(
                    progress = state.steps.toFloat() / state.stepGoal.coerceAtLeast(1),
                    modifier = Modifier.padding(top = VitalOsTheme.spacing.sm),
                )
                Text(
                    "${"%.1f".format(state.distanceMeters / 1000.0)} km · ${state.activeCalories} kcal",
                    style = VitalOsTheme.typography.bodySm,
                    color = VitalOsTheme.colors.textSecondary,
                    modifier = Modifier.padding(top = VitalOsTheme.spacing.xs),
                )
            }
        }

        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("REST & RECOVERY", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
                    Text("${state.sleepDurationMinutes / 60}h ${state.sleepDurationMinutes % 60}m · ${state.sleepScore}", style = VitalOsTheme.typography.dataTabularSm, color = VitalOsTheme.colors.recoveryMint)
                }
                SleepHypnogramBar(segments = state.sleepSegments, modifier = Modifier.padding(top = VitalOsTheme.spacing.sm))
            }
        }

        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("CONTINUOUS HEART RATE", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
                    Text("${state.liveHr ?: state.restingHr} BPM", style = VitalOsTheme.typography.dataTabularSm, color = VitalOsTheme.colors.criticalRed)
                }
                HrSparkline(samples = state.hrSparkline, modifier = Modifier.padding(top = VitalOsTheme.spacing.sm))
            }
        }

        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("INTAKE & FUEL", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
                    Text("${state.hydrationMl} / ${state.hydrationGoalMl} mL", style = VitalOsTheme.typography.dataTabularSm, color = VitalOsTheme.colors.diagnosticBlue)
                }
                SegmentedProgressBar(
                    progress = state.hydrationMl.toFloat() / state.hydrationGoalMl.coerceAtLeast(1),
                    fillColor = VitalOsTheme.colors.diagnosticBlue,
                    modifier = Modifier.padding(top = VitalOsTheme.spacing.sm),
                )
                Row(modifier = Modifier.fillMaxWidth().padding(top = VitalOsTheme.spacing.sm), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Text("${state.proteinGrams}g protein", style = VitalOsTheme.typography.bodySm, color = VitalOsTheme.colors.textSecondary)
                    Text("${state.carbsGrams}g carbs", style = VitalOsTheme.typography.bodySm, color = VitalOsTheme.colors.textSecondary)
                    Text("${state.fatGrams}g fat", style = VitalOsTheme.typography.bodySm, color = VitalOsTheme.colors.textSecondary)
                }
            }
        }

        item {
            Text(
                "Demonstration Biometric Stream",
                style = VitalOsTheme.typography.dataTabularSm,
                color = VitalOsTheme.colors.textSecondary,
                modifier = Modifier.fillMaxWidth().padding(top = VitalOsTheme.spacing.sm),
            )
        }
    }
}
```

- [ ] **Step 7: Wire the screen into the NavHost**

Modify `app/src/main/java/com/vitalos/app/navigation/VitalOsNavHost.kt` — replace:

```kotlin
        composable(VitalOsRoutes.HOME) { ComingSoonScreen("Home") }
```

with:

```kotlin
        composable(VitalOsRoutes.HOME) { com.vitalos.app.feature.home.HomeScreen() }
```

- [ ] **Step 8: Verify the app builds and run it**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`

Manually verify (emulator/device): launching the app shows the real Home dashboard populated with seeded demo data (readiness ring, activity/sleep/HR/hydration cards).

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/vitalos/app/feature/home app/src/main/java/com/vitalos/app/navigation/VitalOsNavHost.kt app/src/test/java/com/vitalos/app/feature/home
git commit -m "Implement Home Dashboard screen"
```

---

## Task 12: Activity Analytics

**Files:**
- Create: `app/src/main/java/com/vitalos/app/feature/activity/ActivityUiState.kt`
- Create: `app/src/main/java/com/vitalos/app/feature/activity/ActivityViewModel.kt`
- Create: `app/src/main/java/com/vitalos/app/feature/activity/ActivityScreen.kt`
- Modify: `app/src/main/java/com/vitalos/app/navigation/VitalOsNavHost.kt:composable(VitalOsRoutes.ACTIVITY)`
- Test: `app/src/test/java/com/vitalos/app/feature/activity/ActivityViewModelTest.kt`

**Interfaces:**
- Consumes: `HealthDataRepository.observeSnapshotRange`/`observeActivityRange` (Task 6), `FakeHealthDataRepository` (Task 6), `HrSparkline`/`VitalCard` (Task 8).
- Produces: `enum class ActivityTimeRange { DAILY, WEEKLY, MONTHLY }`, `data class ActivityUiState(...)`, `@HiltViewModel class ActivityViewModel` exposing `val uiState: StateFlow<ActivityUiState>` and `fun selectRange(range: ActivityTimeRange)`; `@Composable fun ActivityScreen(viewModel: ActivityViewModel = hiltViewModel())`.

- [ ] **Step 1: Write the UI state**

`app/src/main/java/com/vitalos/app/feature/activity/ActivityUiState.kt`:

```kotlin
package com.vitalos.app.feature.activity

enum class ActivityTimeRange(val days: Int, val label: String) {
    DAILY(1, "Day"),
    WEEKLY(7, "Week"),
    MONTHLY(30, "Month"),
}

data class ActivityUiState(
    val isLoading: Boolean = true,
    val selectedRange: ActivityTimeRange = ActivityTimeRange.WEEKLY,
    val totalSteps: Int = 0,
    val totalDistanceMeters: Double = 0.0,
    val totalActiveCalories: Int = 0,
    val activeMinutes: Int = 0,
    val sedentaryMinutes: Int = 0,
    val streakDays: Int = 0,
    val stepsTrend: List<Int> = emptyList(),
)
```

- [ ] **Step 2: Write the failing ViewModel test**

`app/src/test/java/com/vitalos/app/feature/activity/ActivityViewModelTest.kt`:

```kotlin
package com.vitalos.app.feature.activity

import app.cash.turbine.test
import com.vitalos.app.data.local.entity.ActivitySampleEntity
import com.vitalos.app.data.local.entity.DailyMetricsSnapshotEntity
import com.vitalos.app.testing.FakeHealthDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeHealthDataRepository
    private lateinit var viewModel: ActivityViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeHealthDataRepository()
        viewModel = ActivityViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun snapshot(day: Long, steps: Int) = DailyMetricsSnapshotEntity(
        dateEpochDay = day, readinessPercent = 0, healthScore = 0, strainTarget = 0.0,
        steps = steps, distanceMeters = steps * 0.8, activeCalories = steps / 20,
        hydrationMl = 0, proteinGrams = 0, carbsGrams = 0, fatGrams = 0, calorieTarget = 0, caloriesConsumed = 0,
    )

    @Test
    fun uiState_aggregatesSnapshotsAndActivityForSelectedRange() = runTest {
        repo.snapshotRangeFlow.value = listOf(snapshot(1, 7000), snapshot(2, 9000), snapshot(3, 10500))
        repo.activityRangeFlow.value = listOf(
            ActivitySampleEntity(dateEpochDay = 3, hourOfDay = 8, steps = 500, intensity = 3),
            ActivitySampleEntity(dateEpochDay = 3, hourOfDay = 9, steps = 0, intensity = 0),
        )

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()
            assertEquals(26500, state.totalSteps)
            assertEquals(2, state.streakDays)
            assertEquals(60, state.activeMinutes)
            assertEquals(60, state.sedentaryMinutes)
        }
    }

    @Test
    fun selectRange_updatesSelectedRangeInState() = runTest {
        viewModel.selectRange(ActivityTimeRange.MONTHLY)
        viewModel.uiState.test {
            var state = awaitItem()
            while (state.selectedRange != ActivityTimeRange.MONTHLY) state = awaitItem()
            assertEquals(ActivityTimeRange.MONTHLY, state.selectedRange)
        }
    }
}
```

- [ ] **Step 3: Run the test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.feature.activity.ActivityViewModelTest"`
Expected: FAIL — `ActivityViewModel` is unresolved.

- [ ] **Step 4: Write the ViewModel**

`app/src/main/java/com/vitalos/app/feature/activity/ActivityViewModel.kt`:

```kotlin
package com.vitalos.app.feature.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalos.app.data.repository.HealthDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val healthDataRepository: HealthDataRepository,
) : ViewModel() {

    private val selectedRange = MutableStateFlow(ActivityTimeRange.WEEKLY)

    val uiState: StateFlow<ActivityUiState> = selectedRange.flatMapLatest { range ->
        combine(
            healthDataRepository.observeSnapshotRange(range.days),
            healthDataRepository.observeActivityRange(range.days),
        ) { snapshots, hourly ->
            ActivityUiState(
                isLoading = false,
                selectedRange = range,
                totalSteps = snapshots.sumOf { it.steps },
                totalDistanceMeters = snapshots.sumOf { it.distanceMeters },
                totalActiveCalories = snapshots.sumOf { it.activeCalories },
                activeMinutes = hourly.count { it.intensity >= 2 } * 60,
                sedentaryMinutes = hourly.count { it.intensity == 0 } * 60,
                streakDays = snapshots.count { it.steps >= 8000 },
                stepsTrend = snapshots.map { it.steps },
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ActivityUiState())

    fun selectRange(range: ActivityTimeRange) {
        selectedRange.value = range
    }
}
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.feature.activity.ActivityViewModelTest"`
Expected: `BUILD SUCCESSFUL`, 2 tests passed.

- [ ] **Step 6: Write the screen**

`app/src/main/java/com/vitalos/app/feature/activity/ActivityScreen.kt`:

```kotlin
package com.vitalos.app.feature.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.vitalos.app.components.HrSparkline
import com.vitalos.app.components.VitalCard
import com.vitalos.app.designsystem.VitalOsTheme

@Composable
fun ActivityScreen(viewModel: ActivityViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(VitalOsTheme.spacing.margin),
        verticalArrangement = Arrangement.spacedBy(VitalOsTheme.spacing.md),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(VitalOsTheme.spacing.sm)) {
                ActivityTimeRange.entries.forEach { range ->
                    FilterChip(
                        selected = state.selectedRange == range,
                        onClick = { viewModel.selectRange(range) },
                        label = { Text(range.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VitalOsTheme.colors.elevatedSurface,
                            selectedLabelColor = VitalOsTheme.colors.onSurface,
                        ),
                    )
                }
            }
        }

        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Text("TOTAL STEPS", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
                Text("${state.totalSteps}", style = VitalOsTheme.typography.headlineLg, color = VitalOsTheme.colors.readinessLime)
                HrSparkline(samples = state.stepsTrend, lineColor = VitalOsTheme.colors.readinessLime)
            }
        }

        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Text("DISTANCE", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
                Text("${"%.1f".format(state.totalDistanceMeters / 1000.0)} km", style = VitalOsTheme.typography.headlineMd, color = VitalOsTheme.colors.onSurface)
            }
        }

        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Text("ACTIVE CALORIES", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
                Text("${state.totalActiveCalories} kcal", style = VitalOsTheme.typography.headlineMd, color = VitalOsTheme.colors.onSurface)
            }
        }

        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Active minutes: ${state.activeMinutes}", style = VitalOsTheme.typography.bodyMd, color = VitalOsTheme.colors.onSurface)
                    Text("Sedentary: ${state.sedentaryMinutes}m", style = VitalOsTheme.typography.bodyMd, color = VitalOsTheme.colors.textSecondary)
                }
                Text("Streak: ${state.streakDays} days ≥ 8,000 steps", style = VitalOsTheme.typography.bodySm, color = VitalOsTheme.colors.recoveryMint)
            }
        }
    }
}
```

- [ ] **Step 7: Wire the screen into the NavHost**

Modify `app/src/main/java/com/vitalos/app/navigation/VitalOsNavHost.kt` — replace:

```kotlin
        composable(VitalOsRoutes.ACTIVITY) { ComingSoonScreen("Activity") }
```

with:

```kotlin
        composable(VitalOsRoutes.ACTIVITY) { com.vitalos.app.feature.activity.ActivityScreen() }
```

- [ ] **Step 8: Verify the app builds**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/vitalos/app/feature/activity app/src/main/java/com/vitalos/app/navigation/VitalOsNavHost.kt app/src/test/java/com/vitalos/app/feature/activity
git commit -m "Implement Activity Analytics screen"
```

---

## Task 13: Heart Health Vitals

**Files:**
- Create: `app/src/main/java/com/vitalos/app/feature/heart/HeartHealthUiState.kt`
- Create: `app/src/main/java/com/vitalos/app/feature/heart/HeartHealthViewModel.kt`
- Create: `app/src/main/java/com/vitalos/app/feature/heart/HeartHealthScreen.kt`
- Modify: `app/src/main/java/com/vitalos/app/navigation/VitalOsNavHost.kt:composable(VitalOsRoutes.HEALTH)`
- Test: `app/src/test/java/com/vitalos/app/feature/heart/HeartHealthViewModelTest.kt`

**Interfaces:**
- Consumes: `HealthDataRepository.observeLatestHeartRate`/`observeRecentHeartRate` (Task 6), `BleRepository.liveHeartRate` (Task 5), `FakeHealthDataRepository`/`FakeBleRepository` (Tasks 5–6), `HrSparkline`/`VitalCard` (Task 8).
- Produces: `data class HeartHealthUiState(val isLoading: Boolean = true, val currentBpm: Int = 0, val isLiveMeasured: Boolean = false, val restingBpm: Int = 0, val hrTrend: List<Int> = emptyList(), val zoneMinutes: Map<String, Int> = emptyMap())`; `@HiltViewModel class HeartHealthViewModel` exposing `val uiState: StateFlow<HeartHealthUiState>`; `@Composable fun HeartHealthScreen(viewModel: HeartHealthViewModel = hiltViewModel())`.

- [ ] **Step 1: Write the UI state**

`app/src/main/java/com/vitalos/app/feature/heart/HeartHealthUiState.kt`:

```kotlin
package com.vitalos.app.feature.heart

data class HeartHealthUiState(
    val isLoading: Boolean = true,
    val currentBpm: Int = 0,
    val isLiveMeasured: Boolean = false,
    val restingBpm: Int = 0,
    val hrTrend: List<Int> = emptyList(),
    val zoneMinutes: Map<String, Int> = emptyMap(),
)

internal fun classifyZone(bpm: Int): String = when {
    bpm < 100 -> "Zone 1"
    bpm < 120 -> "Zone 2"
    bpm < 140 -> "Zone 3"
    bpm < 160 -> "Zone 4"
    else -> "Zone 5"
}
```

- [ ] **Step 2: Write the failing ViewModel test**

`app/src/test/java/com/vitalos/app/feature/heart/HeartHealthViewModelTest.kt`:

```kotlin
package com.vitalos.app.feature.heart

import app.cash.turbine.test
import com.vitalos.app.data.local.entity.HeartRateSampleEntity
import com.vitalos.app.testing.FakeBleRepository
import com.vitalos.app.testing.FakeHealthDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HeartHealthViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var healthRepo: FakeHealthDataRepository
    private lateinit var bleRepo: FakeBleRepository
    private lateinit var viewModel: HeartHealthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        healthRepo = FakeHealthDataRepository()
        bleRepo = FakeBleRepository()
        viewModel = HeartHealthViewModel(healthRepo, bleRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_prefersLiveHeartRateOverLatestSample() = runTest {
        healthRepo.latestHeartRateFlow.value = HeartRateSampleEntity(timestampEpochMillis = 1, bpm = 70, isResting = false)
        healthRepo.recentHeartRateFlow.value = listOf(
            HeartRateSampleEntity(timestampEpochMillis = 1, bpm = 58, isResting = true),
            HeartRateSampleEntity(timestampEpochMillis = 2, bpm = 130, isResting = false),
        )
        bleRepo.liveHeartRateFlow.value = 72

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()
            assertEquals(72, state.currentBpm)
            assertTrue(state.isLiveMeasured)
            assertEquals(58, state.restingBpm)
            assertEquals(1, state.zoneMinutes["Zone 1"])
            assertEquals(1, state.zoneMinutes["Zone 3"])
        }
    }
}
```

- [ ] **Step 3: Run the test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.feature.heart.HeartHealthViewModelTest"`
Expected: FAIL — `HeartHealthViewModel` is unresolved.

- [ ] **Step 4: Write the ViewModel**

`app/src/main/java/com/vitalos/app/feature/heart/HeartHealthViewModel.kt`:

```kotlin
package com.vitalos.app.feature.heart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalos.app.data.ble.BleRepository
import com.vitalos.app.data.repository.HealthDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HeartHealthViewModel @Inject constructor(
    healthDataRepository: HealthDataRepository,
    bleRepository: BleRepository,
) : ViewModel() {

    val uiState: StateFlow<HeartHealthUiState> = combine(
        healthDataRepository.observeLatestHeartRate(),
        healthDataRepository.observeRecentHeartRate(windowMinutes = 1440),
        bleRepository.liveHeartRate,
    ) { latest, recent, liveHr ->
        val restingSamples = recent.filter { it.isResting }.map { it.bpm }
        HeartHealthUiState(
            isLoading = false,
            currentBpm = liveHr ?: latest?.bpm ?: 0,
            isLiveMeasured = liveHr != null,
            restingBpm = restingSamples.minOrNull() ?: latest?.bpm ?: 0,
            hrTrend = recent.map { it.bpm },
            zoneMinutes = recent.groupingBy { classifyZone(it.bpm) }.eachCount(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HeartHealthUiState())
}
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.feature.heart.HeartHealthViewModelTest"`
Expected: `BUILD SUCCESSFUL`, 1 test passed.

- [ ] **Step 6: Write the screen**

`app/src/main/java/com/vitalos/app/feature/heart/HeartHealthScreen.kt`:

```kotlin
package com.vitalos.app.feature.heart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.vitalos.app.components.HrSparkline
import com.vitalos.app.components.StatusPill
import com.vitalos.app.components.VitalCard
import com.vitalos.app.designsystem.VitalOsTheme

@Composable
fun HeartHealthScreen(viewModel: HeartHealthViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(VitalOsTheme.spacing.margin),
        verticalArrangement = Arrangement.spacedBy(VitalOsTheme.spacing.md),
    ) {
        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${state.currentBpm} BPM", style = VitalOsTheme.typography.headlineLg, color = VitalOsTheme.colors.criticalRed)
                    StatusPill(
                        text = if (state.isLiveMeasured) "LIVE · MEASURED" else "LAST RECORDED",
                        dotColor = if (state.isLiveMeasured) VitalOsTheme.colors.recoveryMint else VitalOsTheme.colors.textDisabled,
                        pulsing = state.isLiveMeasured,
                    )
                }
                Text("Resting HR: ${state.restingBpm} bpm", style = VitalOsTheme.typography.bodyMd, color = VitalOsTheme.colors.textSecondary)
            }
        }

        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Text("24H TREND", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
                HrSparkline(samples = state.hrTrend)
            }
        }

        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Text("HEART RATE ZONES", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
                listOf("Zone 1", "Zone 2", "Zone 3", "Zone 4", "Zone 5").forEach { zone ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(zone, style = VitalOsTheme.typography.bodyMd, color = VitalOsTheme.colors.onSurface)
                        Text("${state.zoneMinutes[zone] ?: 0} min", style = VitalOsTheme.typography.dataTabularSm, color = VitalOsTheme.colors.textSecondary)
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 7: Wire the screen into the NavHost**

Modify `app/src/main/java/com/vitalos/app/navigation/VitalOsNavHost.kt` — replace:

```kotlin
        composable(VitalOsRoutes.HEALTH) { ComingSoonScreen("Health") }
```

with:

```kotlin
        composable(VitalOsRoutes.HEALTH) { com.vitalos.app.feature.heart.HeartHealthScreen() }
```

- [ ] **Step 8: Verify the app builds**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/vitalos/app/feature/heart app/src/main/java/com/vitalos/app/navigation/VitalOsNavHost.kt app/src/test/java/com/vitalos/app/feature/heart
git commit -m "Implement Heart Health Vitals screen"
```

---

## Task 14: Sleep Intelligence

**Files:**
- Create: `app/src/main/java/com/vitalos/app/feature/sleep/SleepUiState.kt`
- Create: `app/src/main/java/com/vitalos/app/feature/sleep/SleepViewModel.kt`
- Create: `app/src/main/java/com/vitalos/app/feature/sleep/SleepScreen.kt`
- Modify: `app/src/main/java/com/vitalos/app/navigation/VitalOsNavHost.kt` (add Sleep route and wire it into the Health tab's nested graph — see Step 7)
- Test: `app/src/test/java/com/vitalos/app/feature/sleep/SleepViewModelTest.kt`

**Interfaces:**
- Consumes: `HealthDataRepository.observeTodaySleepSession`/`observeRecentSleepSessions` (Task 6), `FakeHealthDataRepository` (Task 6), `SleepHypnogramBar`/`VitalCard` (Task 8).
- Produces: `data class SleepUiState(val isLoading: Boolean = true, val lastNightDurationMinutes: Int = 0, val sleepScore: Int = 0, val efficiencyPercent: Double = 0.0, val awakenings: Int = 0, val segments: List<SleepStageSegmentEntity> = emptyList(), val weeklyScores: List<Int> = emptyList(), val sleepDebtMinutes: Int = 0)`; `@HiltViewModel class SleepViewModel` exposing `val uiState: StateFlow<SleepUiState>`; `@Composable fun SleepScreen(viewModel: SleepViewModel = hiltViewModel())`. Added as its own route `VitalOsRoutes.SLEEP = "sleep"`, reachable from the Heart Health screen (Task 13) via a "View Sleep" row — Step 7 wires both the route and the entry point.

- [ ] **Step 1: Write the UI state**

`app/src/main/java/com/vitalos/app/feature/sleep/SleepUiState.kt`:

```kotlin
package com.vitalos.app.feature.sleep

import com.vitalos.app.data.local.entity.SleepStageSegmentEntity

data class SleepUiState(
    val isLoading: Boolean = true,
    val lastNightDurationMinutes: Int = 0,
    val sleepScore: Int = 0,
    val efficiencyPercent: Double = 0.0,
    val awakenings: Int = 0,
    val segments: List<SleepStageSegmentEntity> = emptyList(),
    val weeklyScores: List<Int> = emptyList(),
    val sleepDebtMinutes: Int = 0,
)

internal const val SLEEP_GOAL_MINUTES = 480
```

- [ ] **Step 2: Write the failing ViewModel test**

`app/src/test/java/com/vitalos/app/feature/sleep/SleepViewModelTest.kt`:

```kotlin
package com.vitalos.app.feature.sleep

import app.cash.turbine.test
import com.vitalos.app.data.local.entity.SleepSessionEntity
import com.vitalos.app.data.local.entity.SleepStageSegmentEntity
import com.vitalos.app.data.local.entity.SleepStageType
import com.vitalos.app.data.repository.SleepSessionWithSegments
import com.vitalos.app.testing.FakeHealthDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SleepViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeHealthDataRepository
    private lateinit var viewModel: SleepViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeHealthDataRepository()
        viewModel = SleepViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_computesDurationEfficiencyAndSleepDebt() = runTest {
        val session = SleepSessionEntity(id = 1, dateEpochDay = 1, startEpochMillis = 0, endEpochMillis = 100, efficiencyPercent = 96.4, awakenings = 0, sleepScore = 91)
        val segments = listOf(
            SleepStageSegmentEntity(sessionId = 1, stage = SleepStageType.DEEP, durationMinutes = 100, orderIndex = 0),
            SleepStageSegmentEntity(sessionId = 1, stage = SleepStageType.LIGHT, durationMinutes = 236, orderIndex = 1),
        )
        repo.todaySleepSessionFlow.value = SleepSessionWithSegments(session, segments)
        repo.recentSleepSessionsFlow.value = listOf(session)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()
            assertEquals(336, state.lastNightDurationMinutes)
            assertEquals(91, state.sleepScore)
            assertEquals(144, state.sleepDebtMinutes)
            assertEquals(listOf(91), state.weeklyScores)
        }
    }
}
```

- [ ] **Step 3: Run the test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.feature.sleep.SleepViewModelTest"`
Expected: FAIL — `SleepViewModel` is unresolved.

- [ ] **Step 4: Write the ViewModel**

`app/src/main/java/com/vitalos/app/feature/sleep/SleepViewModel.kt`:

```kotlin
package com.vitalos.app.feature.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalos.app.data.repository.HealthDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SleepViewModel @Inject constructor(
    healthDataRepository: HealthDataRepository,
) : ViewModel() {

    val uiState: StateFlow<SleepUiState> = combine(
        healthDataRepository.observeTodaySleepSession(),
        healthDataRepository.observeRecentSleepSessions(limit = 7),
    ) { today, recent ->
        val durationMinutes = today?.segments?.sumOf { it.durationMinutes } ?: 0
        SleepUiState(
            isLoading = false,
            lastNightDurationMinutes = durationMinutes,
            sleepScore = today?.session?.sleepScore ?: 0,
            efficiencyPercent = today?.session?.efficiencyPercent ?: 0.0,
            awakenings = today?.session?.awakenings ?: 0,
            segments = today?.segments ?: emptyList(),
            weeklyScores = recent.map { it.sleepScore },
            sleepDebtMinutes = (SLEEP_GOAL_MINUTES - durationMinutes).coerceAtLeast(0),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SleepUiState())
}
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.feature.sleep.SleepViewModelTest"`
Expected: `BUILD SUCCESSFUL`, 1 test passed.

- [ ] **Step 6: Write the screen**

`app/src/main/java/com/vitalos/app/feature/sleep/SleepScreen.kt`:

```kotlin
package com.vitalos.app.feature.sleep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.vitalos.app.components.SleepHypnogramBar
import com.vitalos.app.components.VitalCard
import com.vitalos.app.designsystem.VitalOsTheme

@Composable
fun SleepScreen(viewModel: SleepViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(VitalOsTheme.spacing.margin),
        verticalArrangement = Arrangement.spacedBy(VitalOsTheme.spacing.md),
    ) {
        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "${state.lastNightDurationMinutes / 60}h ${state.lastNightDurationMinutes % 60}m",
                        style = VitalOsTheme.typography.headlineLg,
                        color = VitalOsTheme.colors.recoveryMint,
                    )
                    Text("${state.sleepScore} score", style = VitalOsTheme.typography.dataTabularSm, color = VitalOsTheme.colors.onSurface)
                }
                SleepHypnogramBar(segments = state.segments)
                Text(
                    "${state.efficiencyPercent}% efficiency · ${state.awakenings} awakenings",
                    style = VitalOsTheme.typography.bodySm,
                    color = VitalOsTheme.colors.textSecondary,
                )
            }
        }

        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Text("SLEEP DEBT", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
                Text("${state.sleepDebtMinutes} min behind goal", style = VitalOsTheme.typography.bodyMd, color = VitalOsTheme.colors.warningAmber)
            }
        }

        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Text("WEEKLY SCORES", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
                Text(state.weeklyScores.joinToString(" · "), style = VitalOsTheme.typography.dataTabularSm, color = VitalOsTheme.colors.onSurface)
            }
        }
    }
}
```

- [ ] **Step 7: Add the Sleep route and an entry point from Heart Health**

Modify `app/src/main/java/com/vitalos/app/navigation/VitalOsRoutes.kt` — add `const val SLEEP = "sleep"` to `VitalOsRoutes`.

Modify `app/src/main/java/com/vitalos/app/navigation/VitalOsNavHost.kt` — add, alongside the other `composable(...)` entries:

```kotlin
        composable(VitalOsRoutes.SLEEP) { com.vitalos.app.feature.sleep.SleepScreen() }
```

Modify `app/src/main/java/com/vitalos/app/feature/heart/HeartHealthScreen.kt` — change the function signature to accept a navigation callback and add an entry row. Replace:

```kotlin
fun HeartHealthScreen(viewModel: HeartHealthViewModel = hiltViewModel()) {
```

with:

```kotlin
fun HeartHealthScreen(
    viewModel: HeartHealthViewModel = hiltViewModel(),
    onOpenSleep: () -> Unit = {},
) {
```

and add, as a new final `item { ... }` block inside the `LazyColumn`:

```kotlin
        item {
            VitalCard(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenSleep),
            ) {
                Text("View Sleep Intelligence →", style = VitalOsTheme.typography.bodyMd, color = VitalOsTheme.colors.recoveryMint)
            }
        }
```

adding `import androidx.compose.foundation.clickable` to that file's imports.

Modify `app/src/main/java/com/vitalos/app/navigation/VitalOsNavHost.kt` — update the Health route registration to pass the callback. Replace:

```kotlin
        composable(VitalOsRoutes.HEALTH) { com.vitalos.app.feature.heart.HeartHealthScreen() }
```

with:

```kotlin
        composable(VitalOsRoutes.HEALTH) {
            com.vitalos.app.feature.heart.HeartHealthScreen(onOpenSleep = { navController.navigate(VitalOsRoutes.SLEEP) })
        }
```

- [ ] **Step 8: Verify the app builds**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/vitalos/app/feature/sleep app/src/main/java/com/vitalos/app/feature/heart/HeartHealthScreen.kt app/src/main/java/com/vitalos/app/navigation app/src/test/java/com/vitalos/app/feature/sleep
git commit -m "Implement Sleep Intelligence screen"
```

---

## Task 15: Yoga/Workout Flow (Hub + Active Session + Summary)

**Files:**
- Create: `app/src/main/java/com/vitalos/app/feature/workout/WorkoutHubUiState.kt`
- Create: `app/src/main/java/com/vitalos/app/feature/workout/WorkoutHubViewModel.kt`
- Create: `app/src/main/java/com/vitalos/app/feature/workout/WorkoutHubScreen.kt`
- Create: `app/src/main/java/com/vitalos/app/feature/workout/ActiveSessionUiState.kt`
- Create: `app/src/main/java/com/vitalos/app/feature/workout/ActiveSessionViewModel.kt`
- Create: `app/src/main/java/com/vitalos/app/feature/workout/ActiveSessionScreen.kt`
- Create: `app/src/main/java/com/vitalos/app/feature/workout/WorkoutSummaryUiState.kt`
- Create: `app/src/main/java/com/vitalos/app/feature/workout/WorkoutSummaryViewModel.kt`
- Create: `app/src/main/java/com/vitalos/app/feature/workout/WorkoutSummaryScreen.kt`
- Modify: `app/src/main/java/com/vitalos/app/navigation/VitalOsNavHost.kt` (`WORKOUT_HUB`, `WORKOUT_ACTIVE`, `WORKOUT_SUMMARY` routes)
- Test: `app/src/test/java/com/vitalos/app/feature/workout/WorkoutHubViewModelTest.kt`
- Test: `app/src/test/java/com/vitalos/app/feature/workout/ActiveSessionViewModelTest.kt`
- Test: `app/src/test/java/com/vitalos/app/feature/workout/WorkoutSummaryViewModelTest.kt`

**Interfaces:**
- Consumes: `HealthDataRepository.observeRecentWorkouts`/`startWorkout`/`observeWorkout`/`finishWorkout` (Task 6), `BleRepository.liveHeartRate` (Task 5), `FakeHealthDataRepository`/`FakeBleRepository` (Tasks 5–6), `VitalCard`/`StatusPill` (Task 8).
- Produces:
  - `data class WorkoutHubUiState(val isLoading: Boolean = true, val recentWorkouts: List<WorkoutSessionEntity> = emptyList(), val weeklyYogaCount: Int = 0)`, `@HiltViewModel class WorkoutHubViewModel` exposing `uiState: StateFlow<WorkoutHubUiState>` and `fun startYoga(onStarted: (Long) -> Unit)`.
  - `data class ActiveSessionUiState(val isLoading: Boolean = true, val elapsedSeconds: Int = 0, val liveHr: Int? = null, val isRunning: Boolean = true)`, `@HiltViewModel class ActiveSessionViewModel(savedStateHandle: SavedStateHandle, ...)` reading `workoutId` from `savedStateHandle`, exposing `uiState: StateFlow<ActiveSessionUiState>`, `fun togglePause()`, `fun endSession(onEnded: (Long) -> Unit)`.
  - `data class WorkoutSummaryUiState(val isLoading: Boolean = true, val type: WorkoutType? = null, val durationSeconds: Int = 0, val estimatedCalories: Int = 0, val avgHeartRate: Int? = null, val maxHeartRate: Int? = null)`, `@HiltViewModel class WorkoutSummaryViewModel(savedStateHandle: SavedStateHandle, ...)` exposing `uiState: StateFlow<WorkoutSummaryUiState>`.
  - `@Composable fun WorkoutHubScreen(...)`, `@Composable fun ActiveSessionScreen(...)`, `@Composable fun WorkoutSummaryScreen(...)`.

- [ ] **Step 1: Write the Workout Hub UI state and failing test**

`app/src/main/java/com/vitalos/app/feature/workout/WorkoutHubUiState.kt`:

```kotlin
package com.vitalos.app.feature.workout

import com.vitalos.app.data.local.entity.WorkoutSessionEntity

data class WorkoutHubUiState(
    val isLoading: Boolean = true,
    val recentWorkouts: List<WorkoutSessionEntity> = emptyList(),
    val weeklyYogaCount: Int = 0,
)
```

`app/src/test/java/com/vitalos/app/feature/workout/WorkoutHubViewModelTest.kt`:

```kotlin
package com.vitalos.app.feature.workout

import app.cash.turbine.test
import com.vitalos.app.data.local.entity.WorkoutSessionEntity
import com.vitalos.app.data.local.entity.WorkoutStatus
import com.vitalos.app.data.local.entity.WorkoutType
import com.vitalos.app.testing.FakeHealthDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutHubViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeHealthDataRepository
    private lateinit var viewModel: WorkoutHubViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeHealthDataRepository()
        viewModel = WorkoutHubViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_countsYogaSessionsAmongRecentWorkouts() = runTest {
        repo.recentWorkoutsFlow.value = listOf(
            WorkoutSessionEntity(id = 1, type = WorkoutType.YOGA, startEpochMillis = 0, endEpochMillis = 1, durationSeconds = 1800, estimatedCalories = 180, avgHeartRate = 98, maxHeartRate = 120, status = WorkoutStatus.COMPLETED),
            WorkoutSessionEntity(id = 2, type = WorkoutType.RUNNING, startEpochMillis = 0, endEpochMillis = 1, durationSeconds = 1200, estimatedCalories = 220, avgHeartRate = 140, maxHeartRate = 160, status = WorkoutStatus.COMPLETED),
        )

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()
            assertEquals(2, state.recentWorkouts.size)
            assertEquals(1, state.weeklyYogaCount)
        }
    }

    @Test
    fun startYoga_invokesCallbackWithNewWorkoutId() = runTest {
        var startedId: Long? = null
        viewModel.startYoga { startedId = it }
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(1L, startedId)
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.feature.workout.WorkoutHubViewModelTest"`
Expected: FAIL — `WorkoutHubViewModel` is unresolved.

- [ ] **Step 3: Write `WorkoutHubViewModel`**

`app/src/main/java/com/vitalos/app/feature/workout/WorkoutHubViewModel.kt`:

```kotlin
package com.vitalos.app.feature.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalos.app.data.local.entity.WorkoutType
import com.vitalos.app.data.repository.HealthDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutHubViewModel @Inject constructor(
    private val healthDataRepository: HealthDataRepository,
) : ViewModel() {

    val uiState: StateFlow<WorkoutHubUiState> = healthDataRepository.observeRecentWorkouts(limit = 10).map { workouts ->
        WorkoutHubUiState(
            isLoading = false,
            recentWorkouts = workouts,
            weeklyYogaCount = workouts.count { it.type == WorkoutType.YOGA },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorkoutHubUiState())

    fun startYoga(onStarted: (Long) -> Unit) {
        viewModelScope.launch {
            val id = healthDataRepository.startWorkout(WorkoutType.YOGA)
            onStarted(id)
        }
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.feature.workout.WorkoutHubViewModelTest"`
Expected: `BUILD SUCCESSFUL`, 2 tests passed.

- [ ] **Step 5: Write `WorkoutHubScreen`**

`app/src/main/java/com/vitalos/app/feature/workout/WorkoutHubScreen.kt`:

```kotlin
package com.vitalos.app.feature.workout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.vitalos.app.components.VitalCard
import com.vitalos.app.designsystem.VitalOsTheme

@Composable
fun WorkoutHubScreen(
    onStartYoga: (Long) -> Unit,
    viewModel: WorkoutHubViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(VitalOsTheme.spacing.margin),
        verticalArrangement = Arrangement.spacedBy(VitalOsTheme.spacing.md),
    ) {
        item {
            VitalCard(
                modifier = Modifier.fillMaxWidth().clickable { viewModel.startYoga(onStartYoga) },
            ) {
                Text("Start Yoga Session", style = VitalOsTheme.typography.headlineSm, color = VitalOsTheme.colors.readinessLime)
                Text("Begin a guided, manually-tracked session", style = VitalOsTheme.typography.bodySm, color = VitalOsTheme.colors.textSecondary)
            }
        }

        item {
            Text("WEEKLY CONSISTENCY", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
            Text("${state.weeklyYogaCount} yoga sessions", style = VitalOsTheme.typography.headlineMd, color = VitalOsTheme.colors.onSurface)
        }

        items(state.recentWorkouts) { workout ->
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(workout.type.name, style = VitalOsTheme.typography.bodyMd, color = VitalOsTheme.colors.onSurface)
                    Text("${workout.durationSeconds / 60} min · ${workout.estimatedCalories} kcal", style = VitalOsTheme.typography.dataTabularSm, color = VitalOsTheme.colors.textSecondary)
                }
            }
        }
    }
}
```

- [ ] **Step 6: Write the Active Session UI state and failing test**

`app/src/main/java/com/vitalos/app/feature/workout/ActiveSessionUiState.kt`:

```kotlin
package com.vitalos.app.feature.workout

data class ActiveSessionUiState(
    val isLoading: Boolean = true,
    val elapsedSeconds: Int = 0,
    val liveHr: Int? = null,
    val isRunning: Boolean = true,
)
```

`app/src/test/java/com/vitalos/app/feature/workout/ActiveSessionViewModelTest.kt`:

```kotlin
package com.vitalos.app.feature.workout

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.vitalos.app.testing.FakeBleRepository
import com.vitalos.app.testing.FakeHealthDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActiveSessionViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var healthRepo: FakeHealthDataRepository
    private lateinit var bleRepo: FakeBleRepository
    private lateinit var viewModel: ActiveSessionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        healthRepo = FakeHealthDataRepository()
        bleRepo = FakeBleRepository()
        viewModel = ActiveSessionViewModel(SavedStateHandle(mapOf("workoutId" to 1L)), healthRepo, bleRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun elapsedSeconds_incrementsWhileRunning() = runTest {
        viewModel.uiState.test {
            awaitItem()
            dispatcher.scheduler.advanceTimeBy(2_100)
            dispatcher.scheduler.runCurrent()
            val state = awaitItem()
            assertEquals(2, state.elapsedSeconds)
        }
    }

    @Test
    fun togglePause_stopsElapsedTimeFromAdvancing() = runTest {
        viewModel.togglePause()
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isRunning)
        }
    }

    @Test
    fun endSession_finishesWorkoutAndInvokesCallback() = runTest {
        var endedId: Long? = null
        viewModel.endSession { endedId = it }
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(1L, endedId)
    }
}
```

- [ ] **Step 7: Run the test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.feature.workout.ActiveSessionViewModelTest"`
Expected: FAIL — `ActiveSessionViewModel` is unresolved.

- [ ] **Step 8: Write `ActiveSessionViewModel`**

`app/src/main/java/com/vitalos/app/feature/workout/ActiveSessionViewModel.kt`:

```kotlin
package com.vitalos.app.feature.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalos.app.data.ble.BleRepository
import com.vitalos.app.data.local.entity.WorkoutStatus
import com.vitalos.app.data.repository.HealthDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActiveSessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val healthDataRepository: HealthDataRepository,
    private val bleRepository: BleRepository,
) : ViewModel() {

    private val workoutId: Long = checkNotNull(savedStateHandle["workoutId"])
    private val elapsedSeconds = MutableStateFlow(0)
    private val isRunning = MutableStateFlow(true)

    val uiState: StateFlow<ActiveSessionUiState> = combine(
        elapsedSeconds,
        isRunning,
        bleRepository.liveHeartRate,
    ) { elapsed, running, liveHr ->
        ActiveSessionUiState(isLoading = false, elapsedSeconds = elapsed, liveHr = liveHr, isRunning = running)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ActiveSessionUiState())

    init {
        viewModelScope.launch {
            while (true) {
                delay(1_000)
                if (isRunning.value) {
                    elapsedSeconds.value += 1
                }
            }
        }
    }

    fun togglePause() {
        isRunning.value = !isRunning.value
    }

    fun endSession(onEnded: (Long) -> Unit) {
        viewModelScope.launch {
            val hr = bleRepository.liveHeartRate.value
            healthDataRepository.finishWorkout(
                id = workoutId,
                durationSeconds = elapsedSeconds.value,
                estimatedCalories = (elapsedSeconds.value / 60) * 6,
                avgHeartRate = hr,
                maxHeartRate = hr,
                status = WorkoutStatus.COMPLETED,
            )
            onEnded(workoutId)
        }
    }
}
```

- [ ] **Step 9: Run the test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.feature.workout.ActiveSessionViewModelTest"`
Expected: `BUILD SUCCESSFUL`, 3 tests passed.

- [ ] **Step 10: Write `ActiveSessionScreen`**

`app/src/main/java/com/vitalos/app/feature/workout/ActiveSessionScreen.kt`:

```kotlin
package com.vitalos.app.feature.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.vitalos.app.designsystem.VitalOsTheme

@Composable
fun ActiveSessionScreen(
    onEnded: (Long) -> Unit,
    viewModel: ActiveSessionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(VitalOsTheme.spacing.margin),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("YOGA SESSION", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
        Text(
            "${state.elapsedSeconds / 60}:${(state.elapsedSeconds % 60).toString().padStart(2, '0')}",
            style = VitalOsTheme.typography.displayHeroMobile,
            color = VitalOsTheme.colors.readinessLime,
        )
        Text("${state.liveHr ?: "--"} BPM", style = VitalOsTheme.typography.dataTabularMd, color = VitalOsTheme.colors.criticalRed)

        Column(modifier = Modifier.padding(top = VitalOsTheme.spacing.lg)) {
            OutlinedButton(onClick = { viewModel.togglePause() }) {
                Text(if (state.isRunning) "Pause" else "Resume")
            }
            Button(
                onClick = { viewModel.endSession(onEnded) },
                modifier = Modifier.padding(top = VitalOsTheme.spacing.sm),
            ) {
                Text("End Session")
            }
        }
    }
}
```

- [ ] **Step 11: Write the Workout Summary UI state and failing test**

`app/src/main/java/com/vitalos/app/feature/workout/WorkoutSummaryUiState.kt`:

```kotlin
package com.vitalos.app.feature.workout

import com.vitalos.app.data.local.entity.WorkoutType

data class WorkoutSummaryUiState(
    val isLoading: Boolean = true,
    val type: WorkoutType? = null,
    val durationSeconds: Int = 0,
    val estimatedCalories: Int = 0,
    val avgHeartRate: Int? = null,
    val maxHeartRate: Int? = null,
)
```

`app/src/test/java/com/vitalos/app/feature/workout/WorkoutSummaryViewModelTest.kt`:

```kotlin
package com.vitalos.app.feature.workout

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.vitalos.app.data.local.entity.WorkoutStatus
import com.vitalos.app.data.local.entity.WorkoutType
import com.vitalos.app.testing.FakeHealthDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutSummaryViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeHealthDataRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeHealthDataRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_loadsCompletedWorkoutById() = runTest {
        val id = repo.startWorkout(WorkoutType.YOGA)
        repo.finishWorkout(id, durationSeconds = 1800, estimatedCalories = 180, avgHeartRate = 98, maxHeartRate = 120, status = WorkoutStatus.COMPLETED)
        val viewModel = WorkoutSummaryViewModel(SavedStateHandle(mapOf("workoutId" to id)), repo)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()
            assertEquals(WorkoutType.YOGA, state.type)
            assertEquals(1800, state.durationSeconds)
            assertEquals(180, state.estimatedCalories)
        }
    }
}
```

- [ ] **Step 12: Run the test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.feature.workout.WorkoutSummaryViewModelTest"`
Expected: FAIL — `WorkoutSummaryViewModel` is unresolved.

- [ ] **Step 13: Write `WorkoutSummaryViewModel`**

`app/src/main/java/com/vitalos/app/feature/workout/WorkoutSummaryViewModel.kt`:

```kotlin
package com.vitalos.app.feature.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalos.app.data.repository.HealthDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class WorkoutSummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    healthDataRepository: HealthDataRepository,
) : ViewModel() {

    private val workoutId: Long = checkNotNull(savedStateHandle["workoutId"])

    val uiState: StateFlow<WorkoutSummaryUiState> = healthDataRepository.observeWorkout(workoutId).map { workout ->
        WorkoutSummaryUiState(
            isLoading = false,
            type = workout?.type,
            durationSeconds = workout?.durationSeconds ?: 0,
            estimatedCalories = workout?.estimatedCalories ?: 0,
            avgHeartRate = workout?.avgHeartRate,
            maxHeartRate = workout?.maxHeartRate,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorkoutSummaryUiState())
}
```

- [ ] **Step 14: Run the test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.feature.workout.WorkoutSummaryViewModelTest"`
Expected: `BUILD SUCCESSFUL`, 1 test passed.

- [ ] **Step 15: Write `WorkoutSummaryScreen`**

`app/src/main/java/com/vitalos/app/feature/workout/WorkoutSummaryScreen.kt`:

```kotlin
package com.vitalos.app.feature.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.vitalos.app.components.VitalCard
import com.vitalos.app.designsystem.VitalOsTheme

@Composable
fun WorkoutSummaryScreen(
    onDone: () -> Unit,
    viewModel: WorkoutSummaryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(VitalOsTheme.spacing.margin),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("SESSION COMPLETE", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
        Text("${state.type?.name.orEmpty()}", style = VitalOsTheme.typography.headlineLg, color = VitalOsTheme.colors.readinessLime)

        VitalCard(modifier = Modifier.padding(top = VitalOsTheme.spacing.md)) {
            Text("Duration: ${state.durationSeconds / 60} min", style = VitalOsTheme.typography.bodyMd, color = VitalOsTheme.colors.onSurface)
            Text("Calories: ${state.estimatedCalories} kcal", style = VitalOsTheme.typography.bodyMd, color = VitalOsTheme.colors.onSurface)
            Text("Avg HR: ${state.avgHeartRate ?: "--"} bpm · Max HR: ${state.maxHeartRate ?: "--"} bpm", style = VitalOsTheme.typography.bodyMd, color = VitalOsTheme.colors.onSurface)
        }

        Button(onClick = onDone, modifier = Modifier.padding(top = VitalOsTheme.spacing.lg)) {
            Text("Done")
        }
    }
}
```

- [ ] **Step 16: Wire all three screens into the NavHost**

Modify `app/src/main/java/com/vitalos/app/navigation/VitalOsNavHost.kt` — replace the three workout placeholder entries:

```kotlin
        composable(VitalOsRoutes.WORKOUT_HUB) { ComingSoonScreen("Workout Hub") }
        composable(
            VitalOsRoutes.WORKOUT_ACTIVE,
            arguments = listOf(navArgument("workoutId") { type = NavType.LongType }),
        ) { ComingSoonScreen("Active Session") }
        composable(
            VitalOsRoutes.WORKOUT_SUMMARY,
            arguments = listOf(navArgument("workoutId") { type = NavType.LongType }),
        ) { ComingSoonScreen("Workout Summary") }
```

with:

```kotlin
        composable(VitalOsRoutes.WORKOUT_HUB) {
            com.vitalos.app.feature.workout.WorkoutHubScreen(
                onStartYoga = { workoutId -> navController.navigate(VitalOsRoutes.workoutActive(workoutId)) },
            )
        }
        composable(
            VitalOsRoutes.WORKOUT_ACTIVE,
            arguments = listOf(navArgument("workoutId") { type = NavType.LongType }),
        ) {
            com.vitalos.app.feature.workout.ActiveSessionScreen(
                onEnded = { workoutId ->
                    navController.navigate(VitalOsRoutes.workoutSummary(workoutId)) {
                        popUpTo(VitalOsRoutes.WORKOUT_HUB)
                    }
                },
            )
        }
        composable(
            VitalOsRoutes.WORKOUT_SUMMARY,
            arguments = listOf(navArgument("workoutId") { type = NavType.LongType }),
        ) {
            com.vitalos.app.feature.workout.WorkoutSummaryScreen(
                onDone = { navController.popBackStack(VitalOsRoutes.HOME, inclusive = false) },
            )
        }
```

- [ ] **Step 17: Verify the app builds**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`

Manually verify (emulator/device): from the Quick-Add FAB, "Start Yoga" launches the Active Session timer, "End Session" navigates to the Workout Summary, and "Done" returns to Home.

- [ ] **Step 18: Commit**

```bash
git add app/src/main/java/com/vitalos/app/feature/workout app/src/main/java/com/vitalos/app/navigation/VitalOsNavHost.kt app/src/test/java/com/vitalos/app/feature/workout
git commit -m "Implement Yoga/Workout flow (Hub, Active Session, Summary)"
```

---

## Task 16: TAGG Verve Device Management

**Files:**
- Create: `app/src/main/java/com/vitalos/app/feature/device/DeviceManagementUiState.kt`
- Create: `app/src/main/java/com/vitalos/app/feature/device/DeviceManagementViewModel.kt`
- Create: `app/src/main/java/com/vitalos/app/feature/device/DeviceManagementScreen.kt`
- Modify: `app/src/main/java/com/vitalos/app/navigation/VitalOsNavHost.kt:composable(VitalOsRoutes.DEVICE_MANAGEMENT)`
- Modify: `app/src/main/java/com/vitalos/app/feature/home/HomeScreen.kt` (make the sync status pill navigate to Device Management)
- Test: `app/src/test/java/com/vitalos/app/feature/device/DeviceManagementViewModelTest.kt`

**Interfaces:**
- Consumes: `BleRepository` (Task 5), `HealthDataRepository.observeRecentSyncLogs` (Task 6), `FakeBleRepository`/`FakeHealthDataRepository` (Tasks 5–6), `rememberBlePermissionState` (Task 10), `VitalCard`/`StatusPill` (Task 8).
- Produces: `data class DeviceManagementUiState(val isLoading: Boolean = true, val phase: BleConnectionPhase = BleConnectionPhase.DISCONNECTED, val connectedDeviceName: String? = null, val batteryPercent: Int? = null, val lastSyncEpochMillis: Long? = null, val discoveredDevices: List<BleDeviceInfo> = emptyList(), val recentSyncLogs: List<DeviceSyncLogEntity> = emptyList())`; `@HiltViewModel class DeviceManagementViewModel` exposing `uiState: StateFlow<DeviceManagementUiState>`, `fun scan()`, `fun connect(deviceId: String)`, `fun disconnect()`, `fun manualSync()`; `@Composable fun DeviceManagementScreen(viewModel: DeviceManagementViewModel = hiltViewModel())`.

- [ ] **Step 1: Write the UI state**

`app/src/main/java/com/vitalos/app/feature/device/DeviceManagementUiState.kt`:

```kotlin
package com.vitalos.app.feature.device

import com.vitalos.app.data.ble.BleConnectionPhase
import com.vitalos.app.data.ble.BleDeviceInfo
import com.vitalos.app.data.local.entity.DeviceSyncLogEntity

data class DeviceManagementUiState(
    val isLoading: Boolean = true,
    val phase: BleConnectionPhase = BleConnectionPhase.DISCONNECTED,
    val connectedDeviceName: String? = null,
    val batteryPercent: Int? = null,
    val lastSyncEpochMillis: Long? = null,
    val discoveredDevices: List<BleDeviceInfo> = emptyList(),
    val recentSyncLogs: List<DeviceSyncLogEntity> = emptyList(),
)
```

- [ ] **Step 2: Write the failing ViewModel test**

`app/src/test/java/com/vitalos/app/feature/device/DeviceManagementViewModelTest.kt`:

```kotlin
package com.vitalos.app.feature.device

import app.cash.turbine.test
import com.vitalos.app.data.ble.BleConnectionPhase
import com.vitalos.app.data.ble.BleConnectionState
import com.vitalos.app.data.ble.BleDeviceInfo
import com.vitalos.app.testing.FakeBleRepository
import com.vitalos.app.testing.FakeHealthDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceManagementViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var bleRepo: FakeBleRepository
    private lateinit var healthRepo: FakeHealthDataRepository
    private lateinit var viewModel: DeviceManagementViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        bleRepo = FakeBleRepository()
        healthRepo = FakeHealthDataRepository()
        viewModel = DeviceManagementViewModel(bleRepo, healthRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_reflectsConnectionStateAndDiscoveredDevices() = runTest {
        bleRepo.connectionStateFlow.value = BleConnectionState(phase = BleConnectionPhase.CONNECTED, connectedDevice = BleDeviceInfo("TAGG-1234", "TAGG Verve · 1234", -40), batteryPercent = 82, lastSyncEpochMillis = 100)
        bleRepo.discoveredDevicesFlow.value = listOf(BleDeviceInfo("TAGG-1234", "TAGG Verve · 1234", -40))

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()
            assertEquals(BleConnectionPhase.CONNECTED, state.phase)
            assertEquals("TAGG Verve · 1234", state.connectedDeviceName)
            assertEquals(82, state.batteryPercent)
        }
    }

    @Test
    fun scan_delegatesToRepository() = runTest {
        viewModel.scan()
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(bleRepo.scanCalled)
    }

    @Test
    fun connect_delegatesToRepositoryWithDeviceId() = runTest {
        viewModel.connect("TAGG-1234")
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals("TAGG-1234", bleRepo.connectCalledWith)
    }

    @Test
    fun disconnect_delegatesToRepository() = runTest {
        viewModel.disconnect()
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(bleRepo.disconnectCalled)
    }
}
```

- [ ] **Step 3: Run the test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.feature.device.DeviceManagementViewModelTest"`
Expected: FAIL — `DeviceManagementViewModel` is unresolved.

- [ ] **Step 4: Write the ViewModel**

`app/src/main/java/com/vitalos/app/feature/device/DeviceManagementViewModel.kt`:

```kotlin
package com.vitalos.app.feature.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalos.app.data.ble.BleRepository
import com.vitalos.app.data.repository.HealthDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceManagementViewModel @Inject constructor(
    private val bleRepository: BleRepository,
    healthDataRepository: HealthDataRepository,
) : ViewModel() {

    val uiState: StateFlow<DeviceManagementUiState> = combine(
        bleRepository.connectionState,
        bleRepository.discoveredDevices,
        healthDataRepository.observeRecentSyncLogs(limit = 10),
    ) { connection, discovered, logs ->
        DeviceManagementUiState(
            isLoading = false,
            phase = connection.phase,
            connectedDeviceName = connection.connectedDevice?.name,
            batteryPercent = connection.batteryPercent,
            lastSyncEpochMillis = connection.lastSyncEpochMillis,
            discoveredDevices = discovered,
            recentSyncLogs = logs,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DeviceManagementUiState())

    fun scan() {
        viewModelScope.launch { bleRepository.startScan() }
    }

    fun connect(deviceId: String) {
        viewModelScope.launch { bleRepository.connect(deviceId) }
    }

    fun disconnect() {
        viewModelScope.launch { bleRepository.disconnect() }
    }

    fun manualSync() {
        viewModelScope.launch { bleRepository.sync() }
    }
}
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.vitalos.app.feature.device.DeviceManagementViewModelTest"`
Expected: `BUILD SUCCESSFUL`, 4 tests passed.

- [ ] **Step 6: Write the screen**

`app/src/main/java/com/vitalos/app/feature/device/DeviceManagementScreen.kt`:

```kotlin
package com.vitalos.app.feature.device

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.vitalos.app.components.StatusPill
import com.vitalos.app.components.VitalCard
import com.vitalos.app.data.ble.BleConnectionPhase
import com.vitalos.app.designsystem.VitalOsTheme
import com.vitalos.app.permissions.rememberBlePermissionState

@Composable
fun DeviceManagementScreen(viewModel: DeviceManagementViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val permissionState = rememberBlePermissionState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(VitalOsTheme.spacing.margin),
        verticalArrangement = Arrangement.spacedBy(VitalOsTheme.spacing.md),
    ) {
        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(state.connectedDeviceName ?: "No device connected", style = VitalOsTheme.typography.headlineSm, color = VitalOsTheme.colors.onSurface)
                    StatusPill(
                        text = state.phase.name,
                        dotColor = if (state.phase == BleConnectionPhase.CONNECTED) VitalOsTheme.colors.readinessLime else VitalOsTheme.colors.textDisabled,
                        pulsing = state.phase == BleConnectionPhase.CONNECTED || state.phase == BleConnectionPhase.SYNCING,
                    )
                }
                state.batteryPercent?.let {
                    Text("Battery: $it%", style = VitalOsTheme.typography.bodyMd, color = VitalOsTheme.colors.textSecondary)
                }
                state.lastSyncEpochMillis?.let {
                    Text("Last sync: ${it}ms epoch", style = VitalOsTheme.typography.bodySm, color = VitalOsTheme.colors.textSecondary)
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(VitalOsTheme.spacing.sm)) {
                if (!permissionState.allGranted) {
                    Button(onClick = { permissionState.request() }) { Text("Grant Bluetooth permission") }
                } else {
                    Button(onClick = { viewModel.scan() }) { Text("Scan") }
                    if (state.phase == BleConnectionPhase.CONNECTED) {
                        OutlinedButton(onClick = { viewModel.disconnect() }) { Text("Disconnect") }
                        OutlinedButton(onClick = { viewModel.manualSync() }) { Text("Sync now") }
                    }
                }
            }
        }

        items(state.discoveredDevices) { device ->
            VitalCard(
                modifier = Modifier.fillMaxWidth().clickable { viewModel.connect(device.id) },
            ) {
                Text(device.name, style = VitalOsTheme.typography.bodyMd, color = VitalOsTheme.colors.onSurface)
                Text("RSSI ${device.rssi} dBm", style = VitalOsTheme.typography.dataTabularSm, color = VitalOsTheme.colors.textSecondary)
            }
        }

        item {
            Text("SYNC HISTORY", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
        }

        items(state.recentSyncLogs) { log ->
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (log.success) "${log.recordsMerged} records merged" else "Sync failed", style = VitalOsTheme.typography.bodyMd, color = if (log.success) VitalOsTheme.colors.onSurface else VitalOsTheme.colors.criticalRed)
                    Text("${log.timestampEpochMillis}", style = VitalOsTheme.typography.dataTabularSm, color = VitalOsTheme.colors.textSecondary)
                }
                log.errorMessage?.let {
                    Text(it, style = VitalOsTheme.typography.bodySm, color = VitalOsTheme.colors.criticalRed)
                }
            }
        }
    }
}
```

- [ ] **Step 7: Wire the screen into the NavHost and make Home's sync pill navigate to it**

Modify `app/src/main/java/com/vitalos/app/navigation/VitalOsNavHost.kt` — replace:

```kotlin
        composable(VitalOsRoutes.DEVICE_MANAGEMENT) { ComingSoonScreen("TAGG Verve") }
```

with:

```kotlin
        composable(VitalOsRoutes.DEVICE_MANAGEMENT) { com.vitalos.app.feature.device.DeviceManagementScreen() }
```

Modify `app/src/main/java/com/vitalos/app/feature/home/HomeScreen.kt` — change the function signature to accept a navigation callback. Replace:

```kotlin
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
```

with:

```kotlin
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onOpenDeviceManagement: () -> Unit = {},
) {
```

and wrap the existing `StatusPill(...)` call (inside the top `Row` item) with a clickable modifier — replace:

```kotlin
                StatusPill(
                    text = if (state.bleConnected) "TAGG Verve · Connected" else "TAGG Verve · ${state.bleStatusLabel}",
                    dotColor = if (state.bleConnected) VitalOsTheme.colors.readinessLime else VitalOsTheme.colors.textDisabled,
                    pulsing = state.bleConnected,
                )
```

with:

```kotlin
                StatusPill(
                    text = if (state.bleConnected) "TAGG Verve · Connected" else "TAGG Verve · ${state.bleStatusLabel}",
                    dotColor = if (state.bleConnected) VitalOsTheme.colors.readinessLime else VitalOsTheme.colors.textDisabled,
                    pulsing = state.bleConnected,
                    modifier = Modifier.clickable(onClick = onOpenDeviceManagement),
                )
```

adding `import androidx.compose.foundation.clickable` to that file's imports.

Modify `app/src/main/java/com/vitalos/app/navigation/VitalOsNavHost.kt` — update the Home route registration. Replace:

```kotlin
        composable(VitalOsRoutes.HOME) { com.vitalos.app.feature.home.HomeScreen() }
```

with:

```kotlin
        composable(VitalOsRoutes.HOME) {
            com.vitalos.app.feature.home.HomeScreen(onOpenDeviceManagement = { navController.navigate(VitalOsRoutes.DEVICE_MANAGEMENT) })
        }
```

- [ ] **Step 8: Verify the app builds**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`

Manually verify (emulator/device): tapping the Home dashboard's sync pill opens Device Management; "Scan" (after granting permission) discovers a simulated "TAGG Verve" device; tapping it connects and shows battery/sync history.

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/vitalos/app/feature/device app/src/main/java/com/vitalos/app/feature/home/HomeScreen.kt app/src/main/java/com/vitalos/app/navigation/VitalOsNavHost.kt app/src/test/java/com/vitalos/app/feature/device
git commit -m "Implement TAGG Verve Device Management screen"
```

---

## Task 17: Final Assembly & Smoke Verification

**Files:**
- Create: `README.md`
- Modify: none (verification-only task, beyond the README)

**Interfaces:**
- Consumes: the entire app built in Tasks 1–16.
- Produces: a documented, fully-verified Phase 1 build with every unit test passing and all 8 designed screens reachable end-to-end.

- [ ] **Step 1: Run the full unit test suite**

Run: `./gradlew :app:testDebugUnitTest`
Expected: `BUILD SUCCESSFUL` — every test from Tasks 3–16 passes (DB, demo seeder, BLE, repository, sync worker, permissions, and all 6 screen ViewModels).

- [ ] **Step 2: Run a full release-config assembly check**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Manually smoke-test every screen**

Install and launch on an emulator or device (`./gradlew :app:installDebug`), then verify:
- App launches straight to **Home**, populated with seeded demo data (readiness rings, activity/sleep/HR/hydration cards), with the "Demonstration Biometric Stream" footer visible.
- Bottom nav reaches **Activity** (filter chips switch Day/Week/Month and totals update), **Health** (current/resting HR, zones, and the "View Sleep Intelligence" row opens **Sleep**), **Insights** and **Profile** (both show the "coming in a later phase" placeholder).
- The center **Quick-Add FAB** opens the bottom sheet; "Log Water" shows a snackbar and increases Home's hydration bar; "Start Yoga" opens the **Active Session** timer, which counts up, pauses/resumes, and "End Session" navigates to **Workout Summary** with correct duration/calories, and "Done" returns to Home.
- Tapping Home's TAGG Verve status pill opens **Device Management**; after granting Bluetooth permission, "Scan" discovers a simulated device, tapping it connects and shows battery + updates the status pill on Home; "Sync now" adds a new row to sync history; "Disconnect" returns to the disconnected state.
- Force-closing and relaunching the app preserves all data (Room persistence) without re-seeding duplicate rows.

- [ ] **Step 4: Write the project README**

`README.md`:

```markdown
# VitalOS (Phase 1)

A premium, offline-first personal health Android app. Phase 1 implements the
8 screens produced by the Stitch design (`stitch_vitalos_health_intelligence_system/`)
on a Compose + Room + mock-BLE foundation. See
`docs/superpowers/specs/2026-11-11-vitalos-phase1-design.md` for the full design
rationale and `docs/superpowers/plans/2026-11-11-vitalos-phase1.md` for the
implementation plan this app was built from.

## Requirements

- Android Studio Ladybug or newer / JDK 17
- Android SDK Platform 35, minSdk 26

## Build & run

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

## Test

```bash
./gradlew :app:testDebugUnitTest
```

## What's real vs. simulated

- All screens read from a local Room database (`vitalos.db`) and work fully offline.
- The TAGG Verve smartwatch connection is simulated end-to-end (`MockTaggVerveBleRepository`) —
  no physical device is contacted. Swapping in the real BLE GATT protocol is a contained
  change behind the `BleRepository` interface once hardware docs/SDK are available.
- Seeded data on first launch is demonstration data, labeled as such in the UI.

## Scope

Phase 1 covers Home, Activity Analytics, Heart Health Vitals, Sleep Intelligence,
the Yoga/Workout flow (Hub, Active Session, Summary), and TAGG Verve Device
Management. Nutrition, Hydration, Digital Wellbeing, Goals, Reports, full
Profile/Settings, the AI Assistant, Lifestyle Correlation, and light theme are
future phases — see the design spec's roadmap section.
```

- [ ] **Step 5: Commit**

```bash
git add README.md
git commit -m "Add project README and complete Phase 1"
```

- [ ] **Step 6: Push (only if the user confirms)**

Phase 1 is complete and fully committed locally. Before running `git push -u origin master`, confirm with the user — pushing is a visible, hard-to-reverse action and should not happen automatically even though a remote (`origin` → the user's GitHub repo) is already configured.
