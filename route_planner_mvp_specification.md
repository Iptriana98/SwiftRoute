# 📦 SwiftRoute App — MVP Specification

**Project Codename:** SwiftRoute
**Last Updated:** 2026-03-26
**Version:** 1.1

---

# 1. 📌 Product Overview

## 1.1 Vision

Create a **cross-platform mobile application** that helps users efficiently plan and execute routes involving multiple stops, optimizing time, distance, and workflow.

The application is **not limited to delivery drivers**. It is a universal productivity tool for anyone managing multiple locations in a single trip.

---

## 1.2 Problem Statement

Many existing work applications:

- Provide only destination lists
- Do not allow route customization
- Do not optimize stop order
- Force users into rigid workflows

Users must manually reorganize routes, wasting time and increasing travel costs.

---

## 1.3 Solution

A mobile app that allows users to:

- Add multiple addresses manually (one by one)
- Select starting and ending points
- Automatically optimize stop order using an efficient on-device algorithm
- Navigate stops using existing navigation apps
- Track progress during route execution

---

## 1.4 Target Users

Primary users:

- Delivery drivers
- Field technicians
- Sales representatives
- Real estate agents
- Freelancers visiting clients
- People running multiple errands

---

# 2. 🎯 MVP Goals

The MVP must validate:

✅ Users want optimized multi-stop routing
✅ Users repeatedly use saved routes
✅ Route optimization saves measurable time

Success metric (initial):

- User completes ≥1 optimized route per day
- User returns to app within 3 days

---

# 3. ⭐ Core MVP Features

## 3.1 Route Creation

Users can:

- Add addresses manually (one at a time)
- Edit/remove stops

Requirements:

- Address validation
- Automatic geocoding via Google Geocoding API (free tier)
- Store latitude & longitude per stop

> ❌ No quick import from clipboard or text parsing in MVP.

---

## 3.2 Route Optimization (Core Feature)

User selects:

- Start location
  - Current location OR custom address
- End location
  - Same as start OR custom destination

System calculates on-device:

- Optimal stop order
- Estimated total time (sum of travel time + service time per stop)
- Total distance (straight-line via Haversine for optimization; road distance shown after via Directions API)

> See Section 6 for optimization algorithm detail.

---

## 3.3 Route Map View

Displays:

- Embedded map via **MapLibre** (open source, free)
- Numbered pins per stop
- Route path visualization (polyline between stops)
- Current position optional (GPS)
- Optimized order reflected in pin numbers

---

## 3.4 Interactive Stop List

Capabilities:

- Drag & drop reorder
- Mark stop status:
  - Pending
  - Completed
  - Skipped

Syncs bidirectionally with map visualization.

---

## 3.5 External Navigation Integration

User taps **Navigate →** on a stop.

App opens (user preference from Settings):

- Google Maps
- Apple Maps
- Waze (if available)

Navigation targets **next pending stop only** via deep link.

No in-app turn-by-turn navigation (out of scope, and avoids SDK costs).

---

## 3.6 Route Execution Mode

When user presses **Start Route**, app shows:

- Current stop address + name
- Remaining stops count
- ETA remaining (based on service times + average travel time)
- Progress percentage
- Navigate button → opens preferred nav app
- Complete / Skip actions

---

## 3.7 Save & Reuse Routes

Users can:

- Save routes with a name
- Rename routes
- Duplicate routes
- Reuse saved routes as templates for new sessions

Examples:

- "Monday Clients"
- "North Zone"
- "Weekly Errands"

---

## 3.8 Stop Service Time

User defines estimated time at each stop (in minutes) when adding it.

App aggregates service times to calculate expected route completion time.

Default service time: 15 minutes (configurable in Settings).

---

## 3.9 Stop Notes (Persistent Address Memory)

Users can attach private notes to any stop when adding or editing it.

Use cases:

- Gate access codes (e.g. "#4821 at side entrance")
- Parking instructions ("park on Oak St, not the main driveway")
- Contact info ("call John before arriving")
- Special instructions ("ring bell twice, dog is friendly")

Behavior:

- Notes are **saved permanently to the stop address** in Firestore
- When the same address is added to a future route, saved notes appear automatically
- Visible in: Add Stop screen, Create Route stop card (note indicator chip), Active Route Mode current stop card
- Notes are private to the user — not shared
- Optional field — no note required

---

# 4. 📱 Application Screens (MVP)

## 4.1 Home Screen

- List of saved routes (name, stop count, last used date)
- Create new route button
- Empty state for first-time users

---

## 4.2 Create / Edit Route Screen

- Route name input
- Add stop button → go to Add Stop Screen (4.3)
- Stop list with reorder (drag & drop) and delete
- Stop list items show a "📝 Notes" chip indicator if the stop has saved notes
- Save route button

---

## 4.3 Add Stop Screen

- Address search/input field with autocomplete (Google Places Autocomplete — free tier)
- Stop name (optional label)
- Service time in minutes (defaults to global setting)
- **Notes textarea** (optional): multi-line text field for gate codes, parking instructions, access notes, etc.
  - Placeholder: "e.g. Gate code: 1234, ring bell 2x, park on side street..."
  - Helper text: "Saved permanently for this address — visible on every visit"
  - Pre-filled if notes already exist for this address
- Confirm button

---

## 4.4 Optimization Setup Screen

- Select start point: current location OR custom address
- Select end point: same as start OR custom address
- Optimize button → triggers on-device optimization
- Shows estimated total time and distance after optimization

---

## 4.5 Route Overview Screen (Primary Screen)

- MapLibre map with numbered pins
- Route path polyline
- Ordered stop list below
- "Start Route" button
- Edit route button (goes back to 4.2)

---

## 4.6 Active Route Mode Screen

- Current stop card (name, address, service time)
- Navigate button → opens preferred nav app via deep link
- "Mark Complete" and "Skip" actions
- Progress bar (X of N stops)
- ETA remaining
- List of upcoming stops (collapsed)
- End route button

---

## 4.7 Route Summary Screen

Shown when all stops are done or route is ended manually:

- Total time elapsed
- Stops completed vs skipped
- Route name
- Save session button
- Start again button (restart same route)

---

## 4.8 Route History Screen

- List of completed route sessions
- Filter by route name
- Each session shows: date, stops completed, total time

---

## 4.9 Settings Screen

- Preferred navigation app (Google Maps / Apple Maps / Waze)
- Default service time per stop (minutes)
- Account info (email)
- Sign out button

---

# 5. 🧱 Technical Architecture

## 5.1 Frontend

**Technology:** Compose Multiplatform (CMP)

Shared UI and shared business logic across platforms.

### Platforms

- Android → Compose Multiplatform
- iOS → Compose Multiplatform (UIKit host via ComposeUIViewController)

### Module Structure

```
composeApp/          ← Compose Multiplatform shared UI
  ├── commonMain/    ← Shared screens, components, ViewModels
  ├── androidMain/   ← Android-specific: Maps, Navigation deep links
  └── iosMain/       ← iOS-specific: Navigation deep links

shared/              ← Pure Kotlin shared module
  ├── domain/        ← Models, UseCases, Repository interfaces
  ├── data/          ← Repository implementations, Firebase datasources
  ├── optimizer/     ← Route optimization algorithm
  └── utils/         ← Haversine distance, ETA calculation

androidApp/          ← Android entry point
iosApp/              ← iOS entry point (Swift wrapper)
```

### Key Libraries

| Library | Purpose | Cost |
|---|---|---|
| Compose Multiplatform | UI framework | Free (JetBrains) |
| Koin | Dependency injection (KMP-ready) | Free |
| MapLibre SDK | Map rendering | Free / OSS |
| Ktor | HTTP client (geocoding) | Free |
| Kotlinx.serialization | JSON parsing | Free |
| Firebase SDK (KMP) | Auth + Firestore | Free tier |
| Kotlinx.coroutines | Async / Flow | Free |

---

## 5.2 Backend (Serverless)

**Platform:** Firebase (Spark Plan — free tier)

### Services Used

| Service | Purpose | Free Tier |
|---|---|---|
| Firebase Auth | User authentication | Unlimited |
| Firestore | Route and session storage | 50k reads/day, 20k writes/day |
| Cloud Functions | API key proxy (Geocoding) | 125k invocations/month |

> ⚠️ Cloud Functions are used exclusively to proxy Google API calls so API keys are never exposed in the client.

---

## 5.3 External Services

**Google Maps Platform** (via Cloud Functions proxy)

| API | Purpose | Free Tier |
|---|---|---|
| Geocoding API | Address → lat/lng | $200 credit/month (~40k requests) |
| Places Autocomplete API | Address search with suggestions | $200 credit/month |
| Directions API | Road distance for final route display only | $200 credit/month |

> ⚠️ Distance Matrix API is NOT used. Distances for optimization are computed on-device using the Haversine formula (see Section 6).

---

# 6. 🧠 Route Optimization Strategy

## Algorithm: Nearest Neighbor + 2-opt (Pure Kotlin, On-Device)

### Why not OR-Tools?

Google OR-Tools is the industry-standard TSP solver but it:
- Has no native Kotlin Multiplatform support (no iOS compilation)
- Would require a backend call, adding latency and cost

OR-Tools is reserved for a **future backend phase** if the app scales beyond ~50 stops per route.

### MVP Algorithm: Nearest Neighbor + 2-opt

**Phase 1 — Nearest Neighbor (Greedy Initialization):**

```
Start at origin
While unvisited stops exist:
  Select nearest unvisited stop
  Add to route
Add end destination
```

**Phase 2 — 2-opt Improvement:**

```
For each pair of edges (i, k) in the route:
  If reversing the segment between i and k reduces total distance:
    Reverse the segment
Repeat until no improvement found
```

**Distance metric:** Haversine formula (straight-line great-circle distance between lat/lng coordinates). Fast, free, and sufficiently accurate for urban routing.

**Complexity:** O(n²) per iteration, typically converges in <10ms for ≤30 stops.

**Quality:** 2-opt consistently achieves within 5% of optimal for real-world routes.

### Optimization Flow

```
User Stops (lat/lng)
     ↓
Haversine Distance Matrix (on-device, O(n²) space)
     ↓
Nearest Neighbor Initialization
     ↓
2-opt Improvement Loop
     ↓
Ordered Stop List
     ↓
Directions API call (Cloud Function) → road distance + ETA for display only
```

### Future Upgrade Path

When average route size exceeds 50 stops or quality needs improvement:
- Move optimization to Cloud Functions
- Integrate Google OR-Tools (Python/C++ backend)
- Expose as internal API → enables future SDK licensing

---

# 7. 🗄️ Data Model (Firestore)

## User

```
id: String
email: String
preferredNavApp: String  (google_maps | apple_maps | waze)
defaultServiceTimeMinutes: Int
createdAt: Timestamp
```

---

## Route

```
id: String
userId: String
name: String
startLocation: GeoPoint
endLocation: GeoPoint
estimatedTotalMinutes: Int
totalDistanceKm: Float
createdAt: Timestamp
updatedAt: Timestamp
```

---

## Stop

```
id: String
routeId: String
address: String
label: String?
lat: Double
lng: Double
order: Int
status: StopStatus  (PENDING | COMPLETED | SKIPPED)
serviceTimeMinutes: Int
notes: String?          ← gate codes, parking, access notes (optional, persistent per address)
```

---

## RouteSession

```
id: String
routeId: String
userId: String
startedAt: Timestamp
completedAt: Timestamp?
completedStops: List<String>  (stop IDs)
skippedStops: List<String>    (stop IDs)
currentStopIndex: Int
totalDurationMinutes: Int?
endedManually: Boolean
```

---

# 8. 🔐 Security Principles

- Firebase Authentication required for all operations
- Firestore security rules scoped strictly by `userId`
- Google API keys stored only in Cloud Functions environment variables — never in client code
- All Geocoding and Directions API calls proxied through Cloud Functions
- No sensitive data logged

---

# 9. 🚀 SaaS Future Readiness (Non-MVP)

Architecture intentionally enables:

- Public API (optimize route endpoint)
- Company accounts with shared routes
- Web dashboard
- SDK licensing (optimizer module as standalone)
- Platform integrations (delivery management systems)
- OR-Tools backend for large-scale route optimization

The optimizer module in `shared/optimizer/` is designed as a self-contained engine — it can be wrapped as an SDK with minimal refactoring.

---

# 10. ❌ Out of Scope (MVP)

Not included initially:

- Quick import / paste address list
- Payments / subscriptions
- Multi-user teams
- Live GPS tracking
- Advanced analytics / route reporting
- Messaging
- AI predictions
- In-app turn-by-turn navigation
- Distance Matrix API integration (replaced by Haversine)
- OR-Tools (reserved for backend phase)

---

# 11. 📊 Success Metrics

Primary:

- Routes created per user
- Routes completed
- Weekly active users

Secondary:

- Average stops per route
- Route reuse frequency
- Session duration

---

# 12. 🗺️ Development Phases

## Phase 1 — Foundation (2–3 weeks)

- KMM + Compose Multiplatform project setup
- Firebase initialization (Auth + Firestore + security rules)
- Koin DI setup
- Domain models and repository interfaces
- Basic navigation structure

## Phase 2 — Route Creation (2 weeks)

- Add Stop screen with Google Places Autocomplete (via Cloud Function)
- Geocoding integration
- Create / Edit Route screen
- Firestore persistence

## Phase 3 — Optimization (1–2 weeks)

- Haversine distance matrix
- Nearest Neighbor algorithm (shared module)
- 2-opt improvement loop
- Optimization Setup screen
- Directions API integration for ETA display

## Phase 4 — Map & Execution (2 weeks)

- MapLibre map integration (Compose Multiplatform)
- Route Overview screen with pins and polyline
- Active Route Mode screen
- Navigation deep links (Google Maps / Apple Maps / Waze)
- Stop status management (Complete / Skip)

## Phase 5 — History & Settings (1 week)

- RouteSession persistence
- Route Summary screen
- Route History screen
- Settings screen

## Phase 6 — Polish & MVP Done (1 week)

- Error handling (network, geocoding failures)
- Empty states
- Loading states
- Performance tuning
- Basic manual QA

---

# 13. ✅ MVP Definition of Done

The MVP is complete when a user can:

1. Create a route with multiple stops (manually, one by one)
2. Optimize the route (on-device, Nearest Neighbor + 2-opt)
3. View the optimized route on an embedded map (MapLibre)
4. Navigate stop-by-stop via external app (Google Maps / Apple Maps / Waze)
5. Mark stops as completed or skipped during execution
6. Complete a full route session
7. Save and reuse the route
8. View session history

---

**End of Document**
