# Spec: Stop Validation

## Description
Rules for defining and validating stops within a route.

## Scenarios

### Scenario: Automatic Stop Naming
  GIVEN a user adding a new stop
  AND the user leaves the name field empty
  WHEN the stop is added to the route
  THEN the system must assign a default name following the pattern "Stop {N}"
  AND N must be the current count of stops in the route session + 1

### Scenario: Custom Stop Naming
  GIVEN a user adding a new stop
  AND the user enters "My Office" as the name
  WHEN the stop is added
  THEN the system must preserve the name "My Office"

### Scenario: Address Validation Requirement
  GIVEN a user entering stop details
  WHEN the address provided is invalid or empty
  THEN the system must mark the stop as invalid
  AND prevent it from being added to a route

### Scenario: Persistent Notes
  GIVEN a stop with door codes or specific instructions in the "Notes" field
  WHEN the stop is saved
  THEN the notes must be persisted and retrievable for future use
