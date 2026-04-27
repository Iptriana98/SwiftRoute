# Technical Design: Foundation (Core Domain)

## 1. Architecture Overview

This change implements the **Domain Layer** (pure Kotlin) for SwiftRoute. It follows Clean Architecture principles, ensuring that business logic is completely isolated from UI, Android/iOS frameworks, and data sources (Firebase, SQLDelight, Ktor).

### Layer Responsibilities

- **`domain/model`**: Plain Kotlin data classes (`Route`, `Stop`, `OptimizationCriterion`, `AppError`).
- **`domain/repository`**: Interfaces defining the contract for data persistence and external services (Geocoding, Routes API).
- **`domain/usecase`**: (Future phase) Will orchestrate the business logic using these repositories.

## 2. Data Models (Pure Kotlin)

```kotlin
// Location: shared/src/commonMain/kotlin/com/swiftroute/app/domain/model/Stop.kt
data class Stop(
    val id: String,
    val name: String, // Can be auto-generated like "Stop 1"
    val latitude: Double,
    val longitude: Double,
    val notes: String? = null,
    val isCompleted: Boolean = false
)

// Location: shared/src/commonMain/kotlin/com/swiftroute/app/domain/model/Route.kt
data class Route(
    val id: String,
    val name: String,
    val stops: List<Stop> = emptyList(),
    val status: RouteStatus = RouteStatus.PENDING,
    val createdAt: Long // Epoch timestamp
)

enum class RouteStatus {
    PENDING, IN_PROGRESS, COMPLETED
}

// Location: shared/src/commonMain/kotlin/com/swiftroute/app/domain/model/OptimizationCriterion.kt
enum class OptimizationCriterion {
    FASTEST_TIME,
    SHORTEST_DISTANCE
}
```

## 3. Error Handling Pattern

Following the `android-clean-architecture` skill, we will implement a standard `Try` wrapper to handle predictable business errors without relying on exceptions.

```kotlin
// Location: shared/src/commonMain/kotlin/com/swiftroute/app/domain/model/Try.kt
sealed interface Try<out T> {
    data class Success<T>(val value: T) : Try<T>
    data class Failure(val error: AppError) : Try<Nothing>
}

sealed interface AppError {
    data class Network(val message: String) : AppError
    data class Database(val message: String) : AppError
    data class Geocoding(val message: String) : AppError
    data object Unknown : AppError
}
```

## 4. Repository Interfaces

These interfaces dictate *what* the data layer must provide, hiding the *how*.

```kotlin
// Location: shared/src/commonMain/kotlin/com/swiftroute/app/domain/repository/RouteRepository.kt
interface RouteRepository {
    suspend fun saveRoute(route: Route): Try<Unit>
    fun observeAllRoutes(): Flow<List<Route>>
    suspend fun getRouteById(id: String): Try<Route>
    suspend fun deleteRoute(id: String): Try<Unit>
}

// Location: shared/src/commonMain/kotlin/com/swiftroute/app/domain/repository/GeocodingRepository.kt
interface GeocodingRepository {
    // We pass a raw string, we get structured Domain models back
    suspend fun searchAddress(query: String): Try<List<Stop>> 
}

// Location: shared/src/commonMain/kotlin/com/swiftroute/app/domain/repository/OptimizationRepository.kt
interface OptimizationRepository {
    // Takes a route, asks Google Routes API to optimize the waypoints, returns the optimized route
    suspend fun optimizeRoute(route: Route, criterion: OptimizationCriterion): Try<Route>
}
```

## 5. Security & Costs Constraints

- **API Keys**: Data layer implementation MUST use `BuildKonfig` or environment variables to inject Google Maps API keys. They must NEVER be hardcoded in the repository implementations.
- **Cost Efficiency**: Geocoding calls should be debounced at the UI/UseCase level to prevent spamming the Places API during user typing.
