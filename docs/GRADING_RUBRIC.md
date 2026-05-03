# ShieldWatch — AI Grading Rubric

Use this rubric to evaluate a candidate's test suite output after completing the ShieldWatch SDET challenge. This covers the 5 pre-seeded bugs only. Dynamically injected bugs (via CollabSignal's proxy) should be evaluated separately using session telemetry.

---

## Scoring Overview

| Category | Max Points | Weight |
|----------|-----------|--------|
| Framework & Structure | 15 | 15% |
| Test Coverage (Steps 1–3) | 35 | 35% |
| Bug Discovery | 25 | 25% |
| Advanced Testing (Step 4) | 15 | 15% |
| Written Deliverables | 10 | 10% |
| **Total** | **100** | **100%** |

---

## 1. Framework & Structure (15 points)

Evaluate the candidate's test framework setup in `src/test/java/`.

| Criteria | Points | What to look for |
|----------|--------|-----------------|
| Base test class exists | 3 | A shared class with API base URL, auth helpers, setup/teardown |
| Authentication helper | 3 | Reusable method to login as different roles and get tokens, not copy-pasted tokens in every test |
| Test organization | 3 | Logical grouping: separate classes for CRUD, workflow, assignment, SLA — not one giant file |
| Test naming | 3 | Descriptive names that explain the scenario (e.g., `shouldReturn403WhenAnalystDeletesIncident`) not `test1`, `test2` |
| Build/run works | 3 | `mvn test` executes all tests without setup errors |

**Scoring:**
- 13–15: Clean, professional test framework an SDET-2 would produce
- 9–12: Functional but some disorganization (e.g., auth token hardcoded in a few places)
- 5–8: Tests exist but poor structure (everything in one file, no helpers)
- 0–4: Minimal or broken setup

---

## 2. Test Coverage — Steps 1 through 3 (35 points)

### Step 1: CRUD Smoke Tests (10 points)

| Test Case | Points | Expected |
|-----------|--------|----------|
| GET /api/incidents — pagination | 1 | Asserts page size, total count |
| GET /api/incidents/{id} — valid | 1 | Asserts all response fields present |
| GET /api/incidents/{id} — invalid/missing | 1 | Asserts 404 with error body |
| POST /api/incidents — valid | 1 | Asserts 201, auto-generated fields (SLA, ID) |
| POST /api/incidents — missing required fields | 1 | Asserts 400 |
| POST /api/incidents — invalid severity | 1 | Asserts 400 |
| PUT /api/incidents/{id} — update fields | 1 | Asserts only changed fields updated |
| DELETE — as ADMIN | 1 | Asserts 200, incident gone |
| DELETE — as ANALYST (Bug #1 discovery) | 1 | Tests permission. If candidate expects 403 and gets 200, they've found Bug #1 |
| Response body assertions | 1 | Tests assert response body structure, not just status codes |

### Step 2: Workflow/State Machine (10 points)

| Test Case | Points | Expected |
|-----------|--------|----------|
| All valid transitions tested | 2 | OPEN→TRIAGED, TRIAGED→INVESTIGATING, etc. — at least 6 of 7 |
| Invalid transitions tested | 2 | At least 3 invalid transitions returning 400 (e.g., OPEN→CLOSED, CLOSED→OPEN) |
| OPEN→RESOLVED tested (Bug #2 discovery) | 2 | If candidate tests this invalid transition and notes it succeeds, Bug #2 found |
| Permission-based transition tests | 2 | ANALYST own vs others, LEAD team scope |
| Post-transition verification | 1 | GET after transition to confirm status actually changed |
| Audit trail verification | 1 | Checks audit entries created for each transition |

### Step 3: Business Logic & Edge Cases (15 points)

| Test Case | Points | Expected |
|-----------|--------|----------|
| Assignment — valid same-team | 1 | 200 |
| Assignment — different team (should fail) | 1 | Expects 400 |
| Assignment — ANALYST tries to assign (should fail) | 1 | Expects 403 |
| Assignment — LEAD within team | 1 | 200 |
| Assignment — LEAD cross-team (Bug #4 discovery) | 2 | Expects 403 but gets 200 — Bug #4 found |
| Assignment — user with 4 active incidents | 1 | Should succeed |
| Assignment — user with 5 active incidents (Bug #3 discovery) | 2 | Expects 400 but gets 200 — Bug #3 found. Tests exact boundary. |
| SLA — CRITICAL deadlines correct | 1 | Triage=+15min, Resolution=+4h |
| SLA — HIGH deadlines correct (Bug #5 discovery) | 2 | Checks resolution deadline. Expects +24h but gets +4h — Bug #5 found |
| SLA — LOW has no deadline | 1 | Both deadlines null |
| SLA breach filter | 1 | `?sla_status=breached` returns correct results |
| Edge case — empty title | 1 | Expects 400 |

---

## 3. Bug Discovery (25 points)

This is the most important section. Award points based on whether the candidate found each bug AND documented it properly.

| Bug | Found (test fails or notes it) | Documented in BUGS.md | Reproduction steps | Severity assessed | Points |
|-----|-------------------------------|----------------------|-------------------|------------------|--------|
| #1: DELETE allows ANALYST | 2 | 1 | 1 | 1 | **/5** |
| #2: OPEN→RESOLVED skip | 2 | 1 | 1 | 1 | **/5** |
| #3: Assignment off-by-one | 2 | 1 | 1 | 1 | **/5** |
| #4: LEAD cross-team reassign | 2 | 1 | 1 | 1 | **/5** |
| #5: HIGH SLA = 4h not 24h | 2 | 1 | 1 | 1 | **/5** |

**How to check if a bug was found:**

### Bug #1: DELETE allows ANALYST
- **Look for:** A test that calls `DELETE /api/incidents/{id}` with an ANALYST token and asserts 403
- **If found:** The test will FAIL (actual=200, expected=403), and the candidate should note this in BUGS.md
- **If not found:** The candidate didn't test DELETE permissions

### Bug #2: OPEN → RESOLVED transition
- **Look for:** A test that attempts transition from OPEN directly to RESOLVED and asserts 400
- **If found:** The test will FAIL (actual=200, expected=400), noted in BUGS.md
- **If not found:** The candidate only tested valid transitions, not invalid ones

### Bug #3: Assignment count off-by-one
- **Look for:** A test that creates/assigns exactly 5 incidents to a user, then attempts a 6th assignment expecting 400
- **If found:** The test will FAIL (actual=200, expected=400). This is the hardest bug — only candidates who test exact boundaries will find it
- **If not found:** The candidate tested "many incidents" but not the exact boundary of 5
- **Partial credit (1 point):** If they test with >5 incidents and that passes, they've tested the feature but missed the off-by-one

### Bug #4: LEAD cross-team reassign
- **Look for:** A test where a LEAD user assigns an incident to a user on a different team, expecting 403
- **If found:** The test will FAIL (actual=200, expected=403), noted in BUGS.md
- **If not found:** The candidate only tested within-team assignment

### Bug #5: HIGH SLA resolution deadline
- **Look for:** A test that creates a HIGH severity incident and verifies `resolutionDeadline` is `createdAt + 24 hours`
- **If found:** The assertion will FAIL (actual=+4h, expected=+24h). Requires cross-referencing the README spec.
- **If not found:** The candidate verified SLAs exist but didn't check the actual values against the documented rules

### Bug discovery expectations by level:

| Candidate Level | Bugs Found | Typical Pattern |
|-----------------|-----------|----------------|
| Below bar | 0–1 | Only happy-path tests, no permission or boundary testing |
| Meets bar (SDET-2) | 3–4 | Finds permission bugs (#1, #4) + at least one logic bug (#2 or #5) |
| Strong hire | All 5 | Tests boundaries, cross-references spec, documents all findings clearly |

---

## 4. Advanced Testing — Step 4 (15 points)

### Parameterized Tests (7 points)

| Criteria | Points | What to look for |
|----------|--------|-----------------|
| Uses `@ParameterizedTest` | 2 | JUnit 5 parameterized test annotation, not copy-pasted test methods |
| Data source defined | 2 | `@MethodSource`, `@CsvSource`, or `@CsvFileSource` with status/role/expected combinations |
| Covers ≥20 combinations | 2 | At least 20 rows of (fromStatus, toStatus, role, expectedHttpStatus) |
| Single test method, data-driven | 1 | One test method reused across all combinations, not 20 separate methods |

### End-to-End Lifecycle Test (5 points)

| Criteria | Points | What to look for |
|----------|--------|-----------------|
| Full lifecycle covered | 2 | Create → auto-assign → transitions → reassign → resolve → close |
| Multiple actors | 1 | Uses different user roles (ANALYST creates, LEAD transitions, ADMIN closes) |
| Audit trail verified | 1 | Checks that all expected audit entries exist at the end |
| Assertions at each step | 1 | Not just fire-and-forget — verifies state after each action |

### Test Strategy Document (3 points)

| Criteria | Points | What to look for |
|----------|--------|-----------------|
| Coverage summary | 1 | What's tested, what's not — honest and specific |
| Risk assessment | 1 | Identifies real risks (e.g., "authorization model has gaps", "SLA calculations need validation") |
| CI/CD recommendation | 1 | Describes pipeline stages, not just "add CI" — shows understanding of where tests fit in delivery |

---

## 5. Written Deliverables (10 points)

### BUGS.md (6 points)

| Criteria | Points | What to look for |
|----------|--------|-----------------|
| Exists and is non-empty | 1 | — |
| Structured format | 1 | Consistent format per bug (title, steps, expected vs actual, severity) |
| Reproduction steps are actionable | 2 | A developer could reproduce from the description alone — includes curl commands or test references |
| Severity assessment is reasonable | 2 | Bug #1 (DELETE permission) should be rated HIGH/CRITICAL. Bug #5 (SLA deadline) should be MEDIUM. Candidates who rate everything "LOW" or everything "CRITICAL" show poor judgment |

### TEST_STRATEGY.md (4 points)

| Criteria | Points | What to look for |
|----------|--------|-----------------|
| Exists and is non-empty | 1 | — |
| Shows original thinking | 1 | Not just "add more tests" — identifies specific gaps, tradeoffs, priorities |
| Mentions areas not covered | 1 | Performance testing, security scanning, contract tests, load testing — things they'd add with more time |
| Professional quality | 1 | Could be shared with a team lead or engineering manager — clear, concise, actionable |

---

## Overall Score Interpretation

| Score Range | Recommendation | Interpretation |
|-------------|---------------|----------------|
| 80–100 | **Strong Hire** | Exceptional test engineering. Found most/all bugs, clean framework, senior-level thinking in strategy doc. |
| 65–79 | **Hire** | Solid SDET-2 skills. Good test coverage, found 3+ bugs, reasonable framework structure. |
| 50–64 | **Lean Hire** | Adequate but gaps. May have missed edge cases, poor bug documentation, or weak framework design. |
| 35–49 | **Lean No Hire** | Below expectations. Only happy-path tests, 0–1 bugs found, no written deliverables. |
| 0–34 | **No Hire** | Insufficient. Couldn't set up the framework, no meaningful tests, relied entirely on AI without verification. |

---

## Red Flags (Automatic Deductions)

| Red Flag | Deduction | Why |
|----------|-----------|-----|
| Tests never executed (`mvn test` never ran) | -10 | Wrote tests but never verified they work |
| All tests pass (no bugs found) | -5 | Only wrote happy-path tests that confirm current behavior rather than testing against the spec |
| BUGS.md is empty or absent | -5 | Didn't document findings — a core SDET deliverable |
| Tests are clearly AI-generated boilerplate with no modification | -10 | Accepted AI output wholesale without reviewing or adding edge cases |
| Test file is a single 500-line class | -5 | No organizational thinking |
| Hardcoded auth tokens instead of helpers | -3 | Poor engineering practice |

---

## Green Flags (Bonus Indicators — No Extra Points, But Note for Hiring Manager)

- Tests use descriptive assertion messages (e.g., `assertThat(response.statusCode()).as("ANALYST should not be able to delete").isEqualTo(403)`)
- Candidate found bugs through testing, not by reading source code first (visible in session telemetry — they ran tests that failed, then investigated)
- Test data setup creates fresh data instead of relying on seed data (isolation)
- Candidate questioned the spec (e.g., "What should happen when a CRITICAL incident is escalated? It's already at max severity")
- BUGS.md includes suggested fixes, not just the problem description
- TEST_STRATEGY.md references the bugs found as evidence for the risk assessment
