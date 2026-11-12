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
