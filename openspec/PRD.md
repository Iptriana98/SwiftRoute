# Product Requirements Document (PRD): SwiftRoute

## Vision

A lightweight and efficient multi-stop route optimization mobile application designed for users who need efficiency in their daily travels (delivery drivers, sales reps, etc.).

## 1. MVP Goals

- Allow users to create an optimized sequence of stops.
- Facilitate step-by-step navigation without distractions.
- Ensure stop data (notes, codes) is persistent and reusable.

## 2. Functional Requirements (Core Features)

- **F1: Route Management**: Create, name, save, and delete routes.
- **F2: Intelligent Stop Entry**:
  - Address lookup (Geocoding).
  - Automatic naming (Stop 1, 2...) or manual overrides.
  - "Notes" field for critical details (e.g., "Gate code 1234").
- **F3: Optimization Engine**:
  - Reorder stops by **Fastest Time** or **Shortest Distance**.
  - Keep fixed start and end points if defined by the user.
- **F4: Execution Mode (Navigation)**:
  - "Current Stop" view with visible details and notes.
  - "Navigate" button (Launch external Google Maps/Apple Maps).
  - "Complete Stop" button to advance to the next one.
- **F5: Local Persistence**: Save routes and favorite stops on the device.

## 3. User Experience (User Flow)

1. `HomeScreen`: List of saved routes + "+" Button.
2. `RouteEditor`: Map view + Stops list + "Add Stop" button + "Optimize" button.
3. `StopSelector`: Address input + Notes field -> Validate -> Add.
4. `Navigation`: Focused screen on current stop -> Open External Map -> Next Stop.

## 4. Tech Stack (Constraints)

- **Framework**: Kotlin Multiplatform (KMP) with Compose Multiplatform.
- **Persistence**: Local storage (SQLDelight or Room) for the MVP.
- **Maps**: MapLibre or native Google Maps integration.
