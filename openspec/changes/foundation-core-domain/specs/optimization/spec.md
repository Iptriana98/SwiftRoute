# Optimization Specification

## Purpose

Defines the requirements for reordering stops within a route to find the most efficient sequence, delegating the complex TSP (Traveling Salesperson Problem) computation to the Google Maps Platform Routes API.

## Requirements

### Requirement: REQ-OPT-001 Route Optimization Request

The system MUST provide a contract to request the optimal sequence of a given list of stops based on a specified criterion.

#### Scenario: Requesting fastest time

- GIVEN a list of 5 stops and the criterion `FASTEST_TIME`
- WHEN the domain layer invokes the optimization service
- THEN the system MUST format a request to the Google Routes API (`Compute Routes`)
- AND set `optimizeWaypointOrder` to `true`
- AND request routing preference for shortest time

### Requirement: REQ-OPT-002 Fixed Start/End Points

The system MUST respect the user's fixed start and end points if defined.

#### Scenario: Preserving start and end points

- GIVEN a route with a fixed starting location and a fixed destination
- WHEN the optimization request is sent
- THEN the system MUST ensure the external API keeps the first and last waypoints fixed
- AND only reorders the intermediate stops

### Requirement: REQ-OPT-003 Error Handling & Fallback

The system MUST gracefully handle optimization failures (e.g., no internet, API error).

#### Scenario: Network failure during optimization

- GIVEN the user requests optimization but the device has no internet
- WHEN the request fails
- THEN the system MUST return a structured `Failure` result
- AND the original, unoptimized sequence of stops MUST be preserved without data loss
