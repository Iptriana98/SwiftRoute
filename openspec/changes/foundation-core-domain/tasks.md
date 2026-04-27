# Task Breakdown: Foundation (Core Domain)

## Pre-requisites

- [x] Clear demo code from domain layer.
- [x] Create Specs (openspec/changes/foundation-core-domain/specs/).
- [x] Create Technical Design (openspec/changes/foundation-core-domain/design.md).

## Implementation Tasks

### 1. Error Handling Infrastructure

- [x] Create `composeApp/src/commonMain/kotlin/com/swiftroute/app/domain/model/Try.kt`
  - Implement the `Try<out T>` sealed interface (`Success`, `Failure`).
  - Implement the `AppError` sealed interface (`Network`, `Database`, `Geocoding`, `Unknown`).

### 2. Domain Models

- [x] Create `composeApp/src/commonMain/kotlin/com/swiftroute/app/domain/model/Stop.kt`
  - Implement `data class Stop`.
- [x] Create `composeApp/src/commonMain/kotlin/com/swiftroute/app/domain/model/RouteStatus.kt`
  - Implement `enum class RouteStatus`.
- [x] Create `composeApp/src/commonMain/kotlin/com/swiftroute/app/domain/model/Route.kt`
  - Implement `data class Route` (needs `Stop` and `RouteStatus`).
- [x] Create `composeApp/src/commonMain/kotlin/com/swiftroute/app/domain/model/OptimizationCriterion.kt`
  - Implement `enum class OptimizationCriterion`.

### 3. Repository Interfaces

- [x] Create `composeApp/src/commonMain/kotlin/com/swiftroute/app/domain/repository/RouteRepository.kt`
  - Implement interface with `saveRoute`, `observeAllRoutes`, `getRouteById`, `deleteRoute`.
- [x] Create `composeApp/src/commonMain/kotlin/com/swiftroute/app/domain/repository/GeocodingRepository.kt`
  - Implement interface with `searchAddress`.
- [x] Create `composeApp/src/commonMain/kotlin/com/swiftroute/app/domain/repository/OptimizationRepository.kt`
  - Implement interface with `optimizeRoute`.

## Next Phase

Once these tasks are approved, we will enter the **Apply** phase and execute the code writing step by step.
