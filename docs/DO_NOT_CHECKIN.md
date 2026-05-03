# DO NOT CHECKIN — Internal Challenge Configuration

This file contains the bug locations, evaluation rubric, and CollabSignal manifest.
**Remove this file before making the repo available to candidates.**

---

## Pre-Seeded Bugs (5 total)

### Bug 1: DELETE endpoint allows any authenticated user
- **File:** `backend/src/main/java/com/shieldwatch/controller/IncidentController.java`
- **Method:** `deleteIncident()` (line ~88)
- **What's wrong:** Missing `@PreAuthorize("hasRole('ADMIN')")` annotation. Every other destructive operation (cancel via transition) checks for ADMIN role, but DELETE has no role restriction.
- **Expected behavior:** DELETE should return 403 for ANALYST and LEAD roles
- **Actual behavior:** Returns 200 for any authenticated user
- **Found in:** Step 1 (CRUD permission tests)
- **Difficulty:** Easy

### Bug 2: OPEN → RESOLVED transition is allowed (skipping steps)
- **File:** `backend/src/main/java/com/shieldwatch/service/IncidentService.java`
- **What's wrong:** The `ALLOWED_TRANSITIONS` map includes RESOLVED in OPEN's allowed targets:
  ```java
  Status.OPEN, Set.of(Status.TRIAGED, Status.RESOLVED, Status.CANCELLED)
  ```
  Should be:
  ```java
  Status.OPEN, Set.of(Status.TRIAGED, Status.CANCELLED)
  ```
- **Expected behavior:** OPEN → RESOLVED returns 400 (must go through TRIAGED and INVESTIGATING first)
- **Actual behavior:** Returns 200 and updates status
- **Found in:** Step 2 (invalid transition tests)
- **Difficulty:** Medium

### Bug 3: Assignment count off-by-one
- **File:** `backend/src/main/java/com/shieldwatch/service/IncidentService.java`
- **Method:** `assignIncident()` (around the active count check)
- **What's wrong:** Uses `> 5` instead of `>= 5`:
  ```java
  if (activeCount > 5) {  // Bug: should be >= 5
  ```
- **Expected behavior:** User with exactly 5 active incidents should NOT be assignable
- **Actual behavior:** User with 5 active incidents can still receive a 6th
- **Found in:** Step 3 (boundary testing)
- **Difficulty:** Hard — only caught by candidates who test the exact boundary value

### Bug 4: LEAD can reassign across teams
- **File:** `backend/src/main/java/com/shieldwatch/service/IncidentService.java`
- **Method:** `assignIncident()`
- **What's wrong:** The LEAD permission check only verifies the requestor is on the same team as the incident. It does NOT verify the target assignee is also on that team:
  ```java
  if (requestor.getRole() == Role.LEAD) {
      // Only checks requestor's team, not target assignee's team
      if (incident.getTeam() != null && requestor.getTeam() != null
              && !incident.getTeam().getId().equals(requestor.getTeam().getId())) {
          throw new BusinessException("...");
      }
      // Missing: check that assignee.getTeam() equals incident.getTeam()
  }
  ```
- **Expected behavior:** LEAD assigning to a user on a different team should return 403
- **Actual behavior:** Returns 200, assignment succeeds
- **Found in:** Step 3 (cross-team reassignment test)
- **Difficulty:** Medium

### Bug 5: HIGH severity SLA resolution deadline is wrong
- **File:** `backend/src/main/java/com/shieldwatch/service/EscalationService.java`
- **Method:** `calculateDeadlines()`
- **What's wrong:** Copy-paste error — HIGH resolution deadline set to 4 hours instead of 24:
  ```java
  case HIGH -> {
      incident.setTriageDeadline(now.plusHours(1));
      incident.setResolutionDeadline(now.plusHours(4));  // Bug: should be 24
  }
  ```
- **Expected behavior:** HIGH incidents should have resolution deadline = created_at + 24 hours (per README)
- **Actual behavior:** Resolution deadline = created_at + 4 hours
- **Found in:** Step 3 (SLA value verification against documented rules)
- **Difficulty:** Medium — requires cross-referencing API response against README spec

---

## Bug Discovery Expectations

| Candidate Level | Bugs Found |
|-----------------|-----------|
| Below bar | 0–1 |
| Meets bar (SDET-2) | 3–4 |
| Exceeds bar | All 5 with clear documentation |

---

## CollabSignal Manifest

```json
{
  "branch": "main",
  "repoUrl": "https://github.com/<org>/shieldwatch",
  "envVars": {},
  "setupCommands": [
    "cd /workspace/backend && mvn spring-boot:run &",
    "sleep 15",
    "cd /workspace/frontend && npm install && npm run dev &"
  ],
  "openFiles": [
    "README.md",
    "backend/src/main/java/com/shieldwatch/controller/IncidentController.java"
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

## Challenge Steps

| order_index | title | difficulty | estimated_minutes |
|-------------|-------|------------|-------------------|
| 0 | API Smoke Tests & Framework Setup | 1 | 15 |
| 1 | State Machine & Workflow Testing | 2 | 15 |
| 2 | Business Logic & Edge Cases | 3 | 20 |
| 3 | Data-Driven Tests & Test Strategy | 4 | 15 |

## Trial Room Config

| Field | Value |
|-------|-------|
| Title | ShieldWatch — SDET Assessment |
| Language | java |
| Estimated Duration | 65 minutes |
| Scenario Type | build-feature |
| Sandbox Type | code-server |
| Interview Mode | take_home |
