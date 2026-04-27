# Core Domain Specification

## Purpose

Defines the core business entities (`Route`, `Stop`) and repository contracts for the SwiftRoute MVP. This domain handles the representation of routes, the management of individual stops, and the contracts for persistence and optimization.

## Requirements

### Requirement: REQ-CORE-001 Route Entity Definition

The system MUST represent a Route containing a unique identifier, a name, a list of Stops, a creation timestamp, and a current execution status.

#### Scenario: Instantiate a new route

- GIVEN the user wants to start a new journey
- WHEN the system creates a new Route
- THEN the Route MUST be initialized with an empty list of stops
- AND its status MUST be `PENDING`
- AND the creation timestamp MUST be recorded

### Requirement: REQ-CORE-002 Stop Entity Definition

The system MUST represent a Stop with geographical coordinates, a name, a completion status, and optional user notes.

#### Scenario: Create a valid stop

- GIVEN valid coordinates (Latitude and Longitude) from the Geocoding service
- WHEN a Stop is created
- THEN the Stop MUST store the coordinates
- AND it MUST default to an uncompleted state (`isCompleted = false`)
- AND it MAY contain user-defined notes (e.g., "Gate code 1234")

#### Scenario: Auto-naming a stop

- GIVEN the user does not provide a custom name for a Stop
- WHEN the Stop is added to the Route
- THEN the system SHOULD auto-generate a sequential name (e.g., "Stop 1", "Stop 2")

### Requirement: REQ-CORE-003 Route Repository Contract

The system MUST define a pure Kotlin interface `RouteRepository` for CRUD operations, ensuring the domain is decoupled from the data layer.

#### Scenario: Save a route

- GIVEN an active Route with multiple stops
- WHEN `saveRoute(route)` is called
- THEN the interface MUST guarantee a `Result` or `Try` indicating success or failure of the persistence operation

#### Scenario: Observe routes

- GIVEN the HomeScreen needs to display active routes
- WHEN `observeAllRoutes()` is called
- THEN the repository MUST return a reactive stream (e.g., `Flow<List<Route>>`) of the user's routes

### Requirement: REQ-CORE-004 Optimization Criteria

The system MUST define the criteria by which a route can be optimized, delegating the actual computation to the external Optimization Engine.

#### Scenario: Select optimization mode

- GIVEN the user is configuring a Route
- WHEN they choose an optimization preference
- THEN the system MUST represent this using an enum or sealed class (e.g., `OptimizationCriterion.FASTEST_TIME` or `OptimizationCriterion.SHORTEST_DISTANCE`)
