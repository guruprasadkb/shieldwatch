# ShieldWatch — Security Incident Management Platform

ShieldWatch is a REST API + React dashboard for managing security incidents. Security events are triaged, assigned to analysts, investigated, and resolved — with role-based access control, SLA enforcement, and a full audit trail.

## Quick Start

### Backend (Spring Boot + H2)

```bash
cd backend
mvn spring-boot:run
```

API available at `http://localhost:8080`. H2 console at `http://localhost:8080/h2-console`.

### Frontend (React + Vite)

```bash
cd frontend
npm install
npm run dev
```

Dashboard at `http://localhost:3000`.

---

## Authentication

All API endpoints (except auth) require a JWT token.

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin1", "password": "password123"}'

# Use the returned token
curl http://localhost:8080/api/incidents \
  -H "Authorization: Bearer <token>"
```

You can also register new users:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "newuser", "password": "password123", "role": "ANALYST", "teamId": 1}'
```

---

## Business Rules

### Incident Lifecycle

```
OPEN → TRIAGED → INVESTIGATING → RESOLVED → CLOSED
                                  RESOLVED → REOPENED → INVESTIGATING
Any state → CANCELLED (ADMIN only)

CLOSED and CANCELLED are terminal states — no further transitions allowed.
```

**Transition endpoint:** `POST /api/incidents/{id}/transition` with body `{ "status": "TRIAGED" }`

### SLA Rules

| Severity | Triage Deadline | Resolution Deadline |
|----------|----------------|---------------------|
| CRITICAL | 15 minutes | 4 hours |
| HIGH | 1 hour | 24 hours |
| MEDIUM | 4 hours | 72 hours |
| LOW | No SLA | No SLA |

- SLA deadlines are automatically set on incident creation based on severity.
- `GET /api/incidents?sla_status=breached` returns incidents past their deadline.
- SLA fields in the response: `triageDeadline`, `resolutionDeadline` (ISO 8601 timestamps).

### Assignment Rules

- **CRITICAL incidents** are auto-assigned to the team lead on creation.
- Only **LEAD** or **ADMIN** can reassign: `PUT /api/incidents/{id}/assign` with body `{ "assigneeId": "..." }`
- Cannot assign to a user on a **different team** than the incident's assigned team.
- Cannot assign to a user who already has **5 or more** active (non-RESOLVED/CLOSED/CANCELLED) incidents.
- Cannot assign a CLOSED or CANCELLED incident.

### Permissions

| Action | ANALYST | LEAD | ADMIN |
|--------|---------|------|-------|
| Create incident | Yes | Yes | Yes |
| View all incidents | Yes | Yes | Yes |
| Transition own incidents | Yes | Yes | Yes |
| Transition any team incident | No | Yes | Yes |
| Reassign within team | No | Yes | Yes |
| Reassign across teams | No | No | Yes |
| Delete incident | No | No | Yes |
| Cancel incident | No | No | Yes |

### Audit Trail

Every status transition, assignment change, and deletion creates an audit log entry.

- `GET /api/incidents/{id}/audit` returns the audit trail for an incident.
- Each entry: `{ "who", "action", "timestamp", "incidentId", "oldValue", "newValue" }`

---

## API Reference

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/login` | Public | Returns JWT token |
| POST | `/api/auth/register` | Public | Create account |
| GET | `/api/incidents` | Auth | List incidents (paginated, filterable) |
| GET | `/api/incidents/{id}` | Auth | Single incident with linked data |
| POST | `/api/incidents` | Auth | Create incident |
| PUT | `/api/incidents/{id}` | Auth | Update incident fields |
| DELETE | `/api/incidents/{id}` | ADMIN | Delete incident |
| POST | `/api/incidents/{id}/transition` | Auth | Change status |
| PUT | `/api/incidents/{id}/assign` | LEAD/ADMIN | Reassign incident |
| GET | `/api/incidents/{id}/audit` | Auth | Audit trail |
| GET | `/api/incidents?sla_status=breached` | Auth | SLA breach filter |
| GET | `/api/dashboard/metrics` | Auth | Aggregated stats |
| GET | `/api/teams` | Auth | List teams |
| GET | `/api/teams/{id}/members` | Auth | Team members |

---

## Seed Data

The application starts with pre-loaded data for immediate use.

### Teams

| Team | Focus Area |
|------|------------|
| Alpha | Threat Detection |
| Bravo | Incident Response |
| Charlie | Forensics |

### Users

| Login | Role | Team | Password |
|-------|------|------|----------|
| analyst1 | ANALYST | Alpha | password123 |
| analyst2 | ANALYST | Bravo | password123 |
| lead_alpha | LEAD | Alpha | password123 |
| lead_bravo | LEAD | Bravo | password123 |
| admin1 | ADMIN | All teams | password123 |

### Incidents

25 seed incidents across all severities (CRITICAL, HIGH, MEDIUM, LOW) and various statuses, distributed across teams Alpha and Bravo. Some incidents have breached SLA deadlines.

---

## Testing

Test dependencies are pre-installed but no tests have been written yet:

- **Backend:** JUnit 5, RestAssured, Mockito (in `pom.xml`). Test directory: `backend/src/test/java/`
- **Frontend:** React Testing Library (in `package.json`)

Run tests:

```bash
# Backend
cd backend
mvn test

# Frontend
cd frontend
npm test
```

---

## Project Structure

```
shieldwatch/
├── backend/
│   └── src/main/java/com/shieldwatch/
│       ├── controller/        # REST endpoints
│       ├── service/           # Business logic
│       ├── model/             # JPA entities and enums
│       ├── dto/               # Request/response objects
│       ├── exception/         # Global error handler
│       ├── config/            # Security, CORS, web config
│       └── auth/              # JWT authentication
│   └── src/main/resources/
│       ├── application.yml
│       └── data.sql           # Seed data
│   └── src/test/java/         # Empty — write your tests here
│
├── frontend/
│   └── src/
│       ├── pages/             # Login, Dashboard, IncidentDetail, TeamView
│       ├── components/        # Reusable UI components
│       ├── hooks/             # Auth hook
│       ├── stores/            # Zustand auth store
│       ├── api/               # Axios client with JWT interceptor
│       └── types/             # TypeScript types
│
└── README.md
```
