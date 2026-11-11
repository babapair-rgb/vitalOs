# VitalOS — Phase 1 Design Spec

**Date:** 2026-11-11
**Status:** Approved by user
**Scope:** Phase 1 of a multi-phase build. Covers only the 8 screens actually produced by the Stitch design (`stitch_vitalos_health_intelligence_system/`), plus the app shell/navigation and an offline-first data + mock-BLE foundation that the rest of the app (Phase 2+) will build on.

## Background

The user supplied a master UI/UX prompt describing a ~150-screen, 17-module "Personal Health OS" Android app (VitalOS), covering activity, heart health, sleep, nutrition, hydration, digital wellbeing, an AI health assistant, lifestyle correlation, routines, device management, reports, goals, and settings. That prompt was fed to Google Stitch to generate visual designs, but Stitch only actually produced 8 concrete screens (with `code.html` + `screen.png`) plus one shared `DESIGN.md` token/style doc:

- `vitalos_home_dashboard`
- `vitalos_activity_analytics`
- `vitalos_heart_health_vitals`
- `vitalos_sleep_intelligence`
- `vitalos_yoga_workout_hub`
- `vitalos_active_yoga_session`
- `vitalos_workout_summary`
- `vitalos_tagg_verve_management`
- `vitalos_kinetic_intelligence/DESIGN.md` (design tokens/system doc only, no screen)

Building the full master-prompt scope in one pass isn't realistic. This spec covers **Phase 1 only**: a real, running native Android app implementing exactly the 8 designed screens, on a solid architectural foundation so later phases (Nutrition, Hydration, Digital Wellbeing, AI Assistant, Lifestyle Correlation, Goals, Reports, full Profile/Settings, light theme, real BLE protocol) can be added without rework.

## Decisions (confirmed with user)

1. **Phase 1 = the 8 Stitch-designed screens only.** Everything else from the master prompt is deferred to later phases/specs.
2. **UI toolkit: Jetpack Compose** (Material3 theme fully overridden with Stitch's own design tokens), not XML Views.
3. **BLE: mock layer for now.** No TAGG Verve protocol docs/SDK are available. Build the real Android BLE permission/scanning/pairing UX, but back it with a simulated device (`MockTaggVerveBleRepository`) behind a `BleRepository` interface, so swapping in the real GATT protocol later is a contained change.

## Architecture

- **Language/UI:** Kotlin, Jetpack Compose, Material3 (colors/typography/shapes fully replaced with Stitch tokens — see Design System section).
- **Pattern:** MVVM. One ViewModel per screen/feature, exposing `StateFlow<UiState>`. Repository pattern between ViewModels and data sources.
- **DI:** Hilt.
- **Navigation:** Single Activity, Compose Navigation (`NavHost`). Persistent bottom nav bar with 5 destinations (Home, Activity, Health, Insights, Profile) matching the design's nav bar icons/labels exactly, plus a center Quick-Add FAB that opens a bottom sheet.
- **Persistence:** Room (SQLite). The app is offline-first — all UI reads from Room via `Flow`; there is no network dependency in Phase 1.
- **Background work:** `WorkManager` periodic worker triggers `BleRepository.sync()`, which (in mock mode) generates new demo records and writes them to Room, producing the same "live telemetry" feel as the Stitch mockup's HR ticker/sync toast.
- **Custom visuals:** Hand-built Compose `Canvas` composables — no chart library — since the design's visuals (concentric multi-ring gauge, sleep hypnogram ribbon, HR sparkline with gradient fill, segmented linear progress) are simple enough to draw directly and must match pixel-for-pixel:
  - `ConcentricRingGauge` (readiness/health/strain triple ring, used on Home)
  - `SleepHypnogramBar` (segmented Awake/Light/REM/Deep ribbon)
  - `HrSparkline` (gradient-filled line chart)
  - `SegmentedProgressBar` (steps/hydration/macro bars)

### Data model (Room entities)

- `Profile` — name, demo-tier badge ("PRO LAB"), goals snapshot.
- `DailyMetricsSnapshot` — one row per day: readiness %, health score, strain target, steps, distance, calories, hydration mL, macro grams (protein/carbs/fat).
- `HeartRateSample` — timestamped BPM readings (feeds current HR + sparkline + zones).
- `SleepSession` + `SleepStageSegment` — session start/end, efficiency, awakenings; segments typed Awake/Light/REM/Deep with duration.
- `ActivitySample` — hourly step/intensity buckets (feeds the Activity Analytics trend charts and Home's hour-block bars).
- `WorkoutSession` — type (yoga/other), start/end, duration, estimated calories, avg/max HR, completion state (feeds Yoga Hub, Active Session, Workout Summary).
- `DeviceSyncLog` — timestamp, records merged, success/failure, error detail (feeds TAGG Verve Management's sync history).

All entities are seeded with realistic demo data on first launch (first-run seeding), explicitly labeled as demonstration data in the UI (small "Demo Data" footer label), matching the master prompt's requirement to never present fabricated data as real.

### BLE abstraction

```
interface BleRepository {
    val connectionState: Flow<BleConnectionState>  // Disconnected / Scanning / Connecting / Connected / Syncing
    val discoveredDevices: Flow<List<BleDeviceInfo>>
    suspend fun startScan()
    suspend fun connect(deviceId: String)
    suspend fun disconnect()
    suspend fun sync(): SyncResult
}
```

`MockTaggVerveBleRepository` simulates: a short scan delay then one discovered device ("TAGG Verve · XXXX"), connect with simulated battery %, a live HR value that gently varies (as in the Stitch mockup's JS simulator), and a `sync()` that appends new rows across the relevant Room tables and returns a `SyncResult` (records merged, timestamp, or simulated occasional failure to exercise the error state).

Real Android Bluetooth permission requests (`BLUETOOTH_SCAN`/`BLUETOOTH_CONNECT` on API 31+) are implemented for real against the system, even though no physical device is ever actually contacted in Phase 1.

## Design System (translated from `DESIGN.md`)

- **Theme:** Dark only in Phase 1 (Stitch only designed dark). Light theme is Phase 2.
- **Colors:** Ported 1:1 from the `DESIGN.md` token table (surface/background/primary/secondary/tertiary/error families) into a Compose `ColorScheme` + an extra `VitalOsExtendedColors` object for tokens Material3 doesn't have a slot for (e.g. `surfaceContainerLowest`, `onSurfaceVariant` nuances).
- **Typography:** Space Grotesk (headlines/hero numbers), Inter (body/labels), JetBrains Mono (all numeric/timestamp/telemetry values) — loaded as Compose `FontFamily`s via Google Fonts provider, matching the exact size/weight/letter-spacing table in `DESIGN.md`.
- **Shape:** 24px card radius, 8px control radius, pill-shaped badges/status capsules — defined as a Compose `Shapes` set plus reusable modifiers.
- **Elevation:** Tonal stacking (no diffuse drop shadows) — implemented as background-color steps (`surface` → `surfaceContainer` → `surfaceContainerHigh/Highest`) plus a 1px top-edge highlight border, per the "Specular Edge Technique" in `DESIGN.md`.

## Screens (Phase 1)

1. **Home Dashboard** — greeting, BLE sync status pill, concentric readiness/health/strain rings, AI-insight card (static/demo copy, no live AI call), activity card w/ hour-block bars, sleep card w/ hypnogram, live HR card w/ sparkline, hydration+macro card, quick-action command strip (Log Water, Start Yoga, Record Meal, Sync Watch).
2. **Activity Analytics** — daily/weekly/monthly filter, steps/distance/calories trend charts, active-minutes & sedentary time, streaks.
3. **Heart Health Vitals** — current HR, resting HR trend, HR zones breakdown, clearly labeled measured-vs-estimated.
4. **Sleep Intelligence** — last night's duration/score, hypnogram, weekly trend, sleep debt estimate.
5. **Yoga/Workout Hub** — dashboard listing workout types, entry point to start a session, weekly consistency.
6. **Active Yoga Session** — live timer, pose/phase indicator, live HR if connected, pause/end controls.
7. **Workout Summary** — post-session duration, estimated calories, avg/max HR, save/discard.
8. **TAGG Verve Device Management** — discovery, pairing, connection status, battery, last sync, sync history list (success/error), manual sync, disconnect/reconnect.

**Insights** and **Profile** bottom-nav tabs exist (per the design's nav bar) but render a lightweight "coming in a later phase" placeholder screen in Phase 1. The Quick-Add FAB bottom sheet's "Record Meal" and "Log Water" actions perform a minimal local insert + toast (no full Nutrition/Hydration screens yet).

## Project setup

- Package: `com.vitalos.app`
- `minSdk` 26, `targetSdk`/`compileSdk` 35
- Gradle Kotlin DSL (`build.gradle.kts`)
- Single `:app` module for Phase 1 (no multi-module split yet — premature at this scope)

## Explicitly out of scope for Phase 1 (future specs)

- **Phase 2:** Nutrition, Hydration, Digital Wellbeing (UsageStats), Goals/Achievements, Reports/History, full Profile/Settings, light theme.
- **Phase 3:** AI Health Assistant + Lifestyle Correlation Engine (needs an LLM provider decision), Daily Routine Tracker.
- **Phase 4:** Real TAGG Verve BLE GATT protocol integration, once hardware docs/SDK are available.

## Testing

- Unit tests for repositories and ViewModels (state mapping, mock BLE sync behavior).
- No instrumented/UI tests required for Phase 1 given scope; may be added later.
