# SwiftRoute - Route Planner App

Cross-platform mobile application for multi-stop route planning and optimization.

## Tech Stack

- **UI**: JetBrains Compose Multiplatform
- **Backend**: Firebase (Auth + Firestore)
- **Maps**: MapLibre
- **DI**: Koin
- **Optimization**: On-device Nearest Neighbor + 2-opt algorithm

## Project Structure

```
composeApp/     → Shared UI (Compose Multiplatform)
shared/         → Pure Kotlin (domain, data, optimizer)
androidApp/     → Android entry point
iosApp/         → iOS entry point
```

## Build

```bash
# Android
./gradlew :androidApp:assembleDebug

# iOS (requires Xcode)
./gradlew :composeApp:linkDebugFrameworkIosArm64
```

## License

Apache 2.0
