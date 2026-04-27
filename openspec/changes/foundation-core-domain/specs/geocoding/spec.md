# Geocoding Specification

## Purpose

Defines the requirements for converting user input (addresses) into geographic coordinates (Latitude/Longitude) and retrieving place details using the Google Maps Platform Places API (New) via Ktor.

## Requirements

### Requirement: REQ-GEO-001 Address Lookup

The system MUST provide an interface to search for addresses and return structured geographical data.

#### Scenario: Successful address search

- GIVEN a user types "1600 Amphitheatre Parkway"
- WHEN the domain layer requests address validation
- THEN the system MUST call the external Places API
- AND return a list of matching results containing the formatted address and coordinates

#### Scenario: No results found

- GIVEN a user types an invalid or non-existent address
- WHEN the domain layer requests address validation
- THEN the system MUST return an empty list or a specific `NoResults` error state
- AND the UI MUST NOT crash

### Requirement: REQ-GEO-002 Pure Domain Abstraction

The system MUST NOT expose external library types (e.g., Ktor HTTP responses or specific JSON DTOs) to the domain layer.

#### Scenario: Mapping external data

- GIVEN the Places API returns a complex JSON response
- WHEN the data source processes the response
- THEN it MUST map the DTOs into a pure Kotlin domain model (e.g., `LocationResult`)
- AND only return the mapped domain model to the UseCase
