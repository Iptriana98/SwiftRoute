# Skill Registry: SwiftRoute

This file contains the engineering conventions and skill mappings for the SwiftRoute project. Agents must consult these rules before making any changes.

## Project Standards (Compact Rules)

### 🏗 Architecture: Clean Architecture & KMP

- **Domain Purity**: The `domain/` package must be pure Kotlin. FORBIDDEN to import Android classes or UI libraries.
- **Immutability**: Use `data class` with `val` for all domain models.
- **Repository Pattern**: UI never accesses the database or network directly. Always through interfaces defined in `domain/repository`.
- **Mappers**: Conversions between DTO/Entity and domain models must be extension functions in the `data` layer.

### 🎨 UI & Design: Compose Multiplatform

- **Material Design 3**: Always use `MaterialTheme.colorScheme` and `MaterialTheme.typography`. Do not use hardcoded colors.
- **State Hoisting**: Composables should be stateless when possible, hoisting state to the ViewModel.
- **Previews**: Every UI component must have at least one `@Preview` with Light/Dark mode configuration.

### 🔥 Backend & Integration

- **English Only**: All code, comments, variables, and documentation must be in English.
- **Geocoding**: Address validation must be handled via a dedicated `GeocodingDataSource`.
- **Domain Decoupling**: Do not expose framework-specific types (e.g., Firebase, SQLDelight types) outside the `data` layer.

## User Skills Trigger Table

| Pattern / Context | Skill to Load | Trigger Condition |
| :--- | :--- | :--- |
| **Architecture / Domain / Repository** | `android-clean-architecture` | Files in `**/domain/**` or `**/repository/**` |
| **UI / Compose / Material 3** | `mobile-android-design` | `.kt` files with `@Composable` |
| **Firebase / Integration** | `firebase` | Backend or remote persistence tasks |
| **Planning / Specs / SDD** | `sdd-*` | `/sdd-` commands or spec creation |

## Project Context

- **Stack**: Kotlin Multiplatform (KMP) + Compose Multiplatform (CMP).
- **Modules**: Single-module (`:composeApp`) with package-based layer separation.
- **Base Package**: `com.swiftroute.app`
