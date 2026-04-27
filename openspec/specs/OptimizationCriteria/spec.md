# Spec: Optimization Criteria

## Description
Defines how a route can be optimized based on user preferences.

## Scenarios

### Scenario: Optimization by Time
  GIVEN a route with multiple stops
  WHEN the user selects "Fastest Time" optimization
  THEN the system must reorder stops to minimize total travel duration

### Scenario: Optimization by Distance
  GIVEN a route with multiple stops
  WHEN the user selects "Shortest Distance" optimization
  THEN the system must reorder stops to minimize total miles/kilometers

### Scenario: Fixed Start and End Optimization
  GIVEN a route with fixed starting and ending locations
  WHEN optimization is triggered
  THEN the system must only reorder the intermediate stops
  AND preserve the start and end points as defined
