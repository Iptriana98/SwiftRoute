# SwiftRoute - Route Planner App

Cross-platform mobile application for multi-stop route planning and optimization.

## Tech Stack

- **UI**: JetBrains Compose Multiplatform
- **Backend**: Firebase (Auth + Firestore)
- **Maps**: MapLibre / Mapbox
- **DI**: Koin
- **Optimization**: On-device Nearest Neighbor + 2-opt algorithm

## Project Structure

```
composeApp/     → Shared UI (Compose Multiplatform)
shared/         → Pure Kotlin (domain, data, optimizer)
androidApp/     → Android entry point
iosApp/         → iOS entry point
```

## Setup

### 1. Environment Variables

SwiftRoute requires API keys for map services. Copy the example file and add your credentials:

```bash
cp .env.example .env
```

Edit `.env` and add your API keys:

```bash
# Google Maps API Key (Android)
GOOGLE_MAPS_API_KEY=your_google_maps_api_key_here

# Mapbox Access Token (all platforms)
MAPBOX_ACCESS_TOKEN=pk.your_mapbox_token_here
```

**IMPORTANT**: The `.env` file is already in `.gitignore`. Never commit actual API keys to GitHub.

### 2. API Keys Setup

#### Mapbox (Recommended)
1. Go to [Mapbox Account](https://account.mapbox.com/)
2. Create a free account if you don't have one
3. Copy your default public token from the Tokens page
4. Add it to `.env` as `MAPBOX_ACCESS_TOKEN`

#### Google Maps (Android only)
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a project and enable Maps SDK for Android
3. Create an API key in Credentials
4. Add it to `.env` as `GOOGLE_MAPS_API_KEY`

### 3. Firebase (Optional)

If using real Firestore instead of mock data:

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a project and add Android/iOS apps
3. Download configuration files:
   - Android: `google-services.json` → `androidApp/`
   - iOS: `GoogleService-Info.plist` → `iosApp/iosApp/`
4. These files are in `.gitignore` and won't be committed

## Build

```bash
# Android
./gradlew :androidApp:assembleDebug

# iOS (requires Xcode)
./gradlew :composeApp:linkDebugFrameworkIosArm64
```

## Development

### Running the App

```bash
# Android
./gradlew :androidApp:installDebug
adb shell am start -n com.swiftroute.app/.MainActivity

# Desktop
./gradlew :composeApp:run
```

### Security Notes

- Never commit `.env` or any file containing real API keys
- The `.env.example` file contains template values for reference
- If you accidentally commit secrets, rotate them immediately in the provider's console

## License

Apache 2.0
