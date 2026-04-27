# Design: Route Management Core (001)

## Architecture Overview
We will implement the **Domain Layer** within the `commonMain` source set. This layer will contain pure business logic and models, adhering to Clean Architecture principles.

## Proposed Changes

### 1. Domain Models (`com.swiftroute.app.domain.model`)
- `Stop`: A data class representing a location.
  - `id: String` (UUID)
  - `name: String` (custom or auto-generated)
  - `address: String` (validated string)
  - `latitude/longitude: Double`
  - `notes: String?`
- `OptimizationCriterion`: An enum or sealed class.
  - `TIME`, `DISTANCE`, `FIXED_ENDPOINTS`.
- `Route`: A data class representing the optimized sequence.
  - `id: String`
  - `stops: List<Stop>`
  - `criterion: OptimizationCriterion`
  - `isCompleted: Boolean`

### 2. Repository Interfaces (`com.swiftroute.app.domain.repository`)
- `RouteRepository`: Interface defining CRUD operations for Routes and Stops.
  - `saveRoute(route: Route)`
  - `getRoutes(): Flow<List<Route>>`
  - `saveStop(stop: Stop)` (for reuse)

## Rationale
- **Inmutability**: Using data classes ensures thread safety and predictable state updates in Compose.
- **Dependency Rule**: The domain layer will have zero dependencies on Android or iOS specific libraries.
- **Scalability**: The repository interface allows us to start with an in-memory implementation for testing and later swap to SQLDelight or Firebase.

## Technical Decisions
- **UUID**: We will use a cross-platform UUID generator (or a simple string ID for now) to identify entities.
- **Validation**: Validation logic will reside in the `Stop` factory or a specific `StopValidator` class.
