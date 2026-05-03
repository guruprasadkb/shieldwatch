# ShieldWatch — SDET Challenge Handoff

## What This Is

This document is a complete handoff spec for building the **ShieldWatch** project — a Security Incident Management API with a React dashboard. It will be used as a **take-home assessment for Senior SDET candidates** on the CollabSignal platform.

**Critical constraints:**
- The project must be **completely generic**. No mention of CoreShield, CollabSignal, or any client name anywhere in the repo — not in code, comments, package names, README, or metadata.
- The project has **5 pre-seeded bugs** in the source code. These must be subtle and natural-looking — no `// BUG HERE` comments, no obvious markers. A candidate (or their AI agent) reading the source should not be able to identify them without actually writing tests.
- There is a companion file `DO_NOT_CHECKIN.md` at the repo root that documents all bugs, the rubric, and the CollabSignal manifest. **This file must never be committed to the repo that candidates see.** It lives in the repo only during development and is removed (or gitignored) before the repo is used.

---

## Project Overview

### Domain: Security Incident Management

**ShieldWatch** is a REST API + React dashboard for managing security incidents. Security events come in → get triaged → assigned to analysts → investigated → resolved. The app has role-based access (ANALYST, LEAD, ADMIN), SLA rules by severity, assignment constraints, and an audit trail.

Candidates receive a **fully functional but untested** application. Their job is to build a comprehensive test suite from scratch across 4 progressively harder steps.

### Why This Domain

- Instantly understandable — no specialized domain knowledge needed
- Rich testable surface: status workflows, RBAC permissions, SLA calculations, assignment constraints, audit logging
- Realistic bugs: authorization gaps, state machine violations, off-by-one errors, copy-paste mistakes

---

## Tech Stack

| Layer | Tech | Notes |
|-------|------|-------|
| Backend | Java 21, Spring Boot 3.2, H2 in-memory DB | H2 so no external DB dependency in sandbox |
| Frontend | React 18, TypeScript, Tailwind CSS | Pre-built dashboard, candidates may write component tests |
| Testing (pre-installed, NOT configured) | JUnit 5, RestAssured, Mockito, React Testing Library | Dependencies in pom.xml / package.json but `src/test/` is empty |
| Build | Maven (backend), Vite (frontend) | |

---

## Application Structure

```
shieldwatch/
├── backend/
│   └── src/main/java/com/shieldwatch/
│       ├── controller/
│       │   ├── IncidentController.java       # CRUD + status transitions + DELETE
│       │   ├── UserController.java           # Auth (login/register) + user management
│       │   ├── TeamController.java           # Team assignment endpoints
│       │   └── DashboardController.java      # Aggregation/stats endpoints
│       ├── service/
│       │   ├── IncidentService.java          # Core business logic (transitions, assignment)
│       │   ├── EscalationService.java        # SLA deadline calculation + auto-escalation
│       │   ├── NotificationService.java      # Alert dispatch (mocked/no-op, just interface)
│       │   └── AuditService.java             # Audit trail logging
│       ├── model/
│       │   ├── Incident.java                 # severity, status, assignee, SLA deadlines, team
│       │   ├── User.java                     # login, role (ANALYST, LEAD, ADMIN), team
│       │   ├── Team.java                     # name, members
│       │   ├── AuditLog.java                 # who, what, when, incident_id, old_value, new_value
│       │   └── enums/
│       │       ├── Severity.java             # LOW, MEDIUM, HIGH, CRITICAL
│       │       ├── Status.java               # OPEN, TRIAGED, INVESTIGATING, RESOLVED, CLOSED, REOPENED, CANCELLED
│       │       └── Role.java                 # ANALYST, LEAD, ADMIN
│       ├── dto/                              # Request/response objects
│       ├── exception/                        # Global error handler (@ControllerAdvice)
│       ├── config/                           # SecurityConfig, CORS, WebConfig
│       └── auth/                             # JWT token provider, filter, UserDetailsService
│   └── src/main/resources/
│       ├── application.yml
│       └── data.sql                          # Seed data
│   └── src/test/java/                        # EMPTY — candidate builds this
│
├── frontend/
│   └── src/
│       ├── pages/
│       │   ├── Login.tsx                     # Working login
│       │   ├── Dashboard.tsx                 # Incident list with filters
│       │   ├── IncidentDetail.tsx            # Detail view with status transition buttons
│       │   └── TeamView.tsx                  # Team assignment panel
│       ├── components/                       # Reusable UI components
│       ├── hooks/useAuth.ts                  # Auth hook
│       ├── stores/authStore.ts               # Zustand auth store
│       ├── api/client.ts                     # Axios instance with JWT interceptor
│       └── types/index.ts                    # TypeScript types
│
├── README.md                                 # Business rules (what candidates see)
├── pom.xml                                   # Backend deps (includes JUnit 5, RestAssured, Mockito)
└── package.json                              # Frontend deps (includes React Testing Library)
```

---

## Seed Data (data.sql)

Load on startup via Spring Boot's `data.sql`:

**3 Teams:**
- Alpha (Threat Detection)
- Bravo (Incident Response)
- Charlie (Forensics)

**5 Users:**
| Login | Role | Team | Password |
|-------|------|------|----------|
| analyst1 | ANALYST | Alpha | password123 |
| analyst2 | ANALYST | Bravo | password123 |
| lead_alpha | LEAD | Alpha | password123 |
| lead_bravo | LEAD | Bravo | password123 |
| admin1 | ADMIN | — (all teams) | password123 |

**25 Seed Incidents:**
- Mix of all severities (5 CRITICAL, 7 HIGH, 8 MEDIUM, 5 LOW)
- Mix of all statuses (some OPEN, some TRIAGED, some INVESTIGATING, some RESOLVED, some CLOSED)
- Distributed across teams Alpha and Bravo
- Include timestamps spread over the last 48 hours
- Some should have breached SLA deadlines (for testing the SLA breach filter)

**Postman Collection:**
Include a `shieldwatch.postman.json` with 5 example requests:
1. Login as admin
2. GET /api/incidents (list)
3. GET /api/incidents/{id} (single)
4. POST /api/incidents (create)
5. POST /api/incidents/{id}/transition (status change)

---

## Business Rules (Goes in README.md — What Candidates See)

This is the spec candidates test against. Write it clearly — they need to understand the rules to write meaningful tests.

### Incident Lifecycle

```
OPEN → TRIAGED → INVESTIGATING → RESOLVED → CLOSED
                                  RESOLVED → REOPENED → INVESTIGATING
Any state → CANCELLED (ADMIN only)

CLOSED and CANCELLED are terminal states — no further transitions allowed.
```

Transition endpoint: `POST /api/incidents/{id}/transition` with body `{ "status": "TRIAGED" }`

### SLA Rules

| Severity | Triage Deadline | Resolution Deadline |
|----------|----------------|---------------------|
| CRITICAL | 15 minutes | 4 hours |
| HIGH | 1 hour | **24 hours** |
| MEDIUM | 4 hours | 72 hours |
| LOW | No SLA | No SLA |

- SLA deadlines auto-set on incident creation based on severity
- `GET /api/incidents?sla_status=breached` returns incidents past their deadline
- SLA fields: `triageDeadline`, `resolutionDeadline` (ISO 8601 timestamps in response)

### Assignment Rules

- CRITICAL incidents auto-assign to the team lead on creation
- Only LEAD or ADMIN can reassign: `PUT /api/incidents/{id}/assign` with body `{ "assigneeId": "..." }`
- Cannot assign to a user on a **different team** than the incident's assigned team
- Cannot assign to a user who already has **5 or more** active (non-RESOLVED/CLOSED/CANCELLED) incidents
- Cannot assign a CLOSED or CANCELLED incident

### Permissions

| Action | ANALYST | LEAD | ADMIN |
|--------|---------|------|-------|
| Create incident | ✅ | ✅ | ✅ |
| View all incidents | ✅ | ✅ | ✅ |
| Transition own incidents | ✅ | ✅ | ✅ |
| Transition any team incident | ❌ | ✅ | ✅ |
| Reassign within team | ❌ | ✅ | ✅ |
| Reassign across teams | ❌ | ❌ | ✅ |
| Delete incident | ❌ | ❌ | ✅ |
| Cancel incident | ❌ | ❌ | ✅ |

### Audit Trail

Every status transition, assignment change, and deletion creates an audit log entry:
- `GET /api/incidents/{id}/audit` returns the audit trail
- Each entry: `{ who, action, timestamp, incidentId, oldValue, newValue }`

### API Endpoints Summary

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | /api/auth/login | Public | Returns JWT token |
| POST | /api/auth/register | Public | Create account |
| GET | /api/incidents | Auth | List incidents (paginated, filterable) |
| GET | /api/incidents/{id} | Auth | Single incident with linked data |
| POST | /api/incidents | Auth | Create incident |
| PUT | /api/incidents/{id} | Auth | Update incident fields |
| DELETE | /api/incidents/{id} | ADMIN | Delete incident |
| POST | /api/incidents/{id}/transition | Auth | Change status |
| PUT | /api/incidents/{id}/assign | LEAD/ADMIN | Reassign incident |
| GET | /api/incidents/{id}/audit | Auth | Audit trail |
| GET | /api/incidents?sla_status=breached | Auth | SLA breach filter |
| GET | /api/dashboard/metrics | Auth | Aggregated stats |
| GET | /api/teams | Auth | List teams |
| GET | /api/teams/{id}/members | Auth | Team members |

---

## The 4 Challenge Steps

These are presented to the candidate one at a time via CollabSignal's challenge step UI.

### Step 1: API Smoke Tests & Framework Setup
**Difficulty:** 1 · **Estimated time:** 15 minutes

```
Set up the test framework and write smoke tests for the Incident CRUD API.

Your tasks:
1. Configure the test project — JUnit 5 + RestAssured are in pom.xml 
   but src/test/java/ is empty. Set up a base test class with:
   - API base URL configuration
   - Authentication helper (login as different roles)
   - Test data setup/teardown using the seed data

2. Write tests for the Incident CRUD endpoints:
   - GET  /api/incidents        → list all, verify pagination works
   - GET  /api/incidents/{id}   → valid ID returns incident, invalid ID returns 404
   - POST /api/incidents        → create with valid payload, missing required fields, 
                                   invalid severity value
   - PUT  /api/incidents/{id}   → update summary, update severity
   - DELETE /api/incidents/{id} → as ADMIN (should succeed), as ANALYST (should fail)

3. Verify response bodies — don't just check status codes. Assert that:
   - List endpoint returns correct count
   - Created incident has all fields populated (including auto-generated SLA deadlines)
   - Error responses have meaningful messages
```

### Step 2: State Machine & Workflow Testing
**Difficulty:** 2 · **Estimated time:** 15 minutes

```
The incident status workflow has strict rules about which transitions 
are allowed. Write a test suite that validates every valid AND invalid 
transition.

Your tasks:
1. Create IncidentWorkflowTest with tests for:

   VALID transitions (should return 200):
   - OPEN → TRIAGED
   - TRIAGED → INVESTIGATING  
   - INVESTIGATING → RESOLVED
   - RESOLVED → CLOSED
   - RESOLVED → REOPENED
   - REOPENED → INVESTIGATING
   - Any state → CANCELLED (as ADMIN)

   INVALID transitions (should return 400):
   - OPEN → RESOLVED (can't skip steps)
   - OPEN → CLOSED (can't skip steps)
   - CLOSED → anything (terminal state)
   - CANCELLED → anything (terminal state)
   - INVESTIGATING → TRIAGED (can't go backward)
   - RESOLVED → OPEN (invalid reverse)

2. Test permission rules on transitions:
   - ANALYST can transition their own incidents
   - ANALYST cannot transition another analyst's incident
   - LEAD can transition any incident in their team

3. For each test, assert:
   - Response status code
   - Incident status actually changed in the database (GET after transition)
   - Audit log entry was created for valid transitions

4. Document: Which transitions work correctly? Which don't?
   File findings in tests/BUGS.md
```

### Step 3: Business Logic & Edge Cases
**Difficulty:** 3 · **Estimated time:** 20 minutes

```
The assignment and SLA rules are the core business logic. QA has 
reported bugs in this area but hasn't been specific. Build a test 
suite that covers the rules AND finds the bugs.

Your tasks:

1. ASSIGNMENT TESTS (AssignmentTest):
   - Assign incident to valid team member → 200
   - Assign to member of a DIFFERENT team → should return 400
   - Assign to user who already has 5 active incidents → should return 400
   - Assign to user who has exactly 4 active incidents → should succeed
   - ANALYST tries to reassign any incident → should return 403
   - LEAD reassigns within their team → 200
   - LEAD reassigns to a member of another team → should return 403
   - ADMIN reassigns across teams → 200
   - Assign a CLOSED incident → should return 400

2. SLA TESTS (SlaTest):
   - Create CRITICAL incident → verify triage SLA = created_at + 15 min
   - Create HIGH incident → verify triage SLA = created_at + 1 hour
   - Create MEDIUM incident → verify triage SLA = created_at + 4 hours
   - Create LOW incident → verify NO SLA deadline is set
   - GET /api/incidents?sla_status=breached → returns correct incidents
   - Verify SLA deadlines match the documented rules EXACTLY

3. EDGE CASES:
   - Create incident with empty string title
   - Create incident with a 10,000 character description
   - Assign to a user ID that doesn't exist
   - Transition an incident that was just deleted by another request
   - Create two incidents rapidly with the same data

4. Document ALL bugs found in tests/BUGS.md with:
   - Steps to reproduce (as a curl command or test case reference)
   - Expected vs actual behavior
   - Your severity assessment (critical/high/medium/low)
```

### Step 4: Data-Driven Tests & Test Strategy
**Difficulty:** 4 · **Estimated time:** 15 minutes

```
The team is preparing for a major release and needs confidence in 
the test suite. Show senior-level test engineering.

Your tasks:

1. PARAMETERIZED TESTS:
   Write a SINGLE parameterized test class that validates status 
   transitions across ALL combinations of:
   - fromStatus (7 values) × toStatus (7 values) × role (3 values)
   
   Use JUnit 5 @ParameterizedTest with @MethodSource or @CsvSource:
   - Each row: fromStatus, toStatus, role, expectedHttpStatus
   - Cover at least 20 meaningful combinations
   - ONE test method, no copy-paste — the data drives the test

2. END-TO-END LIFECYCLE TEST:
   Write a single test that simulates a full incident lifecycle:
   a. ANALYST creates a CRITICAL incident
   b. Verify system auto-assigned it to the team lead
   c. LEAD transitions: OPEN → TRIAGED → INVESTIGATING
   d. LEAD reassigns to an ANALYST on the team
   e. ANALYST transitions: INVESTIGATING → RESOLVED
   f. ADMIN transitions: RESOLVED → CLOSED
   g. Verify the complete audit trail has all expected entries

3. TEST STRATEGY DOCUMENT (tests/TEST_STRATEGY.md):
   Based on everything you've tested, write a 1-page strategy:
   - Summary of current test coverage and gaps
   - What you'd add with more time (performance, security, etc.)
   - Top 3 quality risks in this codebase
   - Recommended CI pipeline stages (describe, don't implement)
   - What metrics you'd track (coverage %, bug escape rate, etc.)
```

---

## The 5 Pre-Seeded Bugs

**CRITICAL: These bugs must look like natural developer mistakes, not planted defects. No comments, no TODOs, no hints in the code.**

### Bug 1: DELETE endpoint allows ANALYST role
**Location:** `IncidentController.java` — the `deleteIncident()` method
**What's wrong:** The `@PreAuthorize("hasRole('ADMIN')")` annotation is missing. The method has `@DeleteMapping("/{id}")` but no role restriction. Any authenticated user can delete incidents.
**How to implement:** Simply omit the `@PreAuthorize` annotation. Every other sensitive endpoint (cancel, bulk ops) has it — this one doesn't. Make it look like it was forgotten.
**Found in:** Step 1 (DELETE permission test)
**Difficulty to find:** Easy

### Bug 2: OPEN → RESOLVED transition is allowed
**Location:** `IncidentService.java` — the `transitionStatus()` method
**What's wrong:** The state machine validation checks that the *target* status is a valid status value, but the allowed transitions map is missing the OPEN→RESOLVED restriction. The transitions map defines what each status CAN go to, but RESOLVED is accidentally included in OPEN's allowed targets.
**How to implement:** In the transitions map/switch statement, include RESOLVED in OPEN's allowed list:
```java
case OPEN -> Set.of(TRIAGED, RESOLVED);  // Bug: RESOLVED shouldn't be here
// Should be: case OPEN -> Set.of(TRIAGED);
```
**Found in:** Step 2 (invalid transition test)
**Difficulty to find:** Medium

### Bug 3: Assignment count off-by-one
**Location:** `IncidentService.java` — the `assignIncident()` method
**What's wrong:** The active incident count check uses `> 5` instead of `>= 5`. A user with exactly 5 active incidents can still be assigned a 6th.
**How to implement:**
```java
long activeCount = incidentRepository.countByAssigneeAndStatusNotIn(assignee, terminalStatuses);
if (activeCount > 5) {  // Bug: should be >= 5
    throw new BusinessException("User has too many active incidents");
}
```
**Found in:** Step 3 (boundary test — assign to user with exactly 5 active incidents)
**Difficulty to find:** Hard — only caught by candidates who test the exact boundary

### Bug 4: LEAD can reassign across teams
**Location:** `IncidentService.java` — the `assignIncident()` method
**What's wrong:** The reassignment permission check validates that the requesting user has LEAD role, but doesn't verify that the *target assignee* belongs to the same team as the incident. It only checks the requester's role.
**How to implement:**
```java
// Check requester has permission
if (requester.getRole() == Role.LEAD || requester.getRole() == Role.ADMIN) {
    // Bug: missing check that target user is on the same team as incident
    // Should also check: if (requester.getRole() == Role.LEAD && !targetUser.getTeam().equals(incident.getTeam()))
    incident.setAssignee(targetUser);
}
```
**Found in:** Step 3 (LEAD cross-team reassignment test)
**Difficulty to find:** Medium

### Bug 5: HIGH severity SLA resolution deadline is wrong
**Location:** `EscalationService.java` — the `calculateDeadlines()` method
**What's wrong:** The HIGH severity resolution deadline is set to 4 hours instead of 24 hours. Copy-paste error from MEDIUM's triage deadline.
**How to implement:**
```java
case HIGH -> {
    incident.setTriageDeadline(now.plusHours(1));
    incident.setResolutionDeadline(now.plusHours(4));  // Bug: should be plusHours(24)
}
case MEDIUM -> {
    incident.setTriageDeadline(now.plusHours(4));
    incident.setResolutionDeadline(now.plusHours(72));
}
```
**Found in:** Step 3 (SLA deadline verification against documented rules)
**Difficulty to find:** Medium — requires cross-referencing API response values against the README spec

---

## CollabSignal Configuration

### Manifest (for trial room creation)

```json
{
  "branch": "main",
  "repoUrl": "https://github.com/<org>/shieldwatch",
  "envVars": {},
  "setupCommands": [
    "cd /workspace && mvn spring-boot:run -Dspring-boot.run.profiles=test &",
    "sleep 10",
    "cd /workspace/frontend && npm install && npm run dev &"
  ],
  "openFiles": [
    "README.md",
    "src/main/java/com/shieldwatch/controller/IncidentController.java"
  ],
  "aiConfig": {
    "enabled": true,
    "model": "gpt-4",
    "leashLevel": "moderate"
  },
  "agent": {
    "injection": {
      "enabled": true,
      "max_bugs": 3,
      "categories": [],
      "cutoff_pct": 0.6,
      "difficulty": "medium",
      "min_turn_gap": 2
    }
  }
}
```

**Note on bug strategy:** This challenge uses BOTH pre-seeded bugs (the 5 above, baked into source code) AND dynamic injection (CollabSignal injects additional bugs into AI responses mid-session). The pre-seeded bugs test whether candidates write thorough tests. The dynamic injection tests whether candidates catch hallucinated code from the AI. Combined, this gives maximum signal.

### Challenge Steps (for `challenges` table)

| order_index | title | difficulty | estimated_minutes |
|-------------|-------|------------|-------------------|
| 0 | API Smoke Tests & Framework Setup | 1 | 15 |
| 1 | State Machine & Workflow Testing | 2 | 15 |
| 2 | Business Logic & Edge Cases | 3 | 20 |
| 3 | Data-Driven Tests & Test Strategy | 4 | 15 |

### Trial Room Config

| Field | Value |
|-------|-------|
| Title | ShieldWatch — SDET Assessment |
| Language | java |
| Complexity | intermediate |
| Estimated Duration | 65 minutes |
| Scenario Type | build-feature |
| Sandbox Type | code-server |
| Interview Mode | take_home |
| Context Type | INTERVIEW |

---

## What Candidates Should See When They Open the Sandbox

1. **README.md** — the business rules documentation (incident lifecycle, SLA rules, assignment rules, permissions, API endpoints). This is their spec. No hints about bugs.
2. **Fully working application** — API on localhost:8080, frontend on localhost:3000
3. **Empty test directory** — `src/test/java/` exists but is empty. JUnit 5 and RestAssured are in `pom.xml`.
4. **Postman collection** — 5 example requests to get started
5. **H2 console** — accessible at localhost:8080/h2-console for DB inspection

The challenge steps appear in the CollabSignal UI, guiding them through the 4 phases.

---

## What Candidates Should NOT See

- This handoff document
- The `DO_NOT_CHECKIN.md` file
- Any comments in code referencing "bug", "injection", "assessment", "challenge", or "CollabSignal"
- Any test files or example tests that hint at what's broken

---

## Expected Candidate Deliverables

By the end of 65 minutes, a strong candidate's workspace should contain:

```
src/test/java/com/shieldwatch/
├── BaseTest.java                    # Auth helpers, base URL, setup/teardown
├── IncidentCrudTest.java            # Step 1
├── IncidentWorkflowTest.java        # Step 2
├── AssignmentTest.java              # Step 3
├── SlaTest.java                     # Step 3
├── EdgeCaseTest.java                # Step 3
├── ParameterizedTransitionTest.java # Step 4
└── IncidentLifecycleE2ETest.java    # Step 4

tests/
├── BUGS.md                          # Bug reports with reproduction steps
└── TEST_STRATEGY.md                 # Test strategy document
```

---

## Bug Discovery Expectations

| Candidate Level | Pre-seeded Bugs Found | Dynamic Injection Response |
|-----------------|----------------------|---------------------------|
| Below bar | 0–1 | Accepts AI output without review |
| Meets bar (SDET-2) | 3–4 | Catches some injected bugs, runs tests to verify |
| Exceeds bar | All 5, with documentation | Catches injected bugs, investigates root cause, verifies fixes |

---

## Implementation Checklist for the Builder

- [ ] Create Spring Boot 3.2 project with Java 21
- [ ] Implement all models, enums, repositories
- [ ] Implement IncidentService with full business logic (transitions, assignment, SLA) — **with the 5 bugs planted naturally**
- [ ] Implement all controllers and DTOs
- [ ] Implement JWT auth (login, register, token refresh)
- [ ] Implement AuditService and audit logging
- [ ] Implement EscalationService with SLA calculation — **with Bug #5**
- [ ] Implement SecurityConfig with role-based endpoint protection — **with Bug #1 (missing @PreAuthorize on DELETE)**
- [ ] Create seed data (data.sql) with 25 incidents, 5 users, 3 teams
- [ ] Create Postman collection
- [ ] Build React frontend (dashboard, incident list, detail view, team view)
- [ ] Write README.md with all business rules (no bug hints)
- [ ] Verify all 5 bugs are present and behave as described
- [ ] Verify `src/test/java/` is empty
- [ ] Verify JUnit 5 + RestAssured + Mockito are in pom.xml
- [ ] Verify React Testing Library is in package.json
- [ ] Verify no code comments reference bugs, testing, assessment, or CollabSignal
- [ ] Create Postman collection with 5 example requests
- [ ] Test the full app runs cleanly with `mvn spring-boot:run`
- [ ] Test the frontend runs cleanly with `npm run dev`
- [ ] Remove DO_NOT_CHECKIN.md before making repo available to candidates
