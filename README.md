

# SkillBridge API — README

## 1. Overview

**SkillBridge API** is a Spring Boot REST API for a “skill bridge” app: users track **learning items** (e.g. courses, topics) and record **applied outcomes** (projects, tasks, blogs, work) for each. It uses JWT for auth and PostgreSQL for persistence.

---

## 2. Tech Stack

| Layer | Technology |
|-------|------------|
| Framework | Spring Boot 4.0.2 |
| Java | 17 |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| Data | Spring Data JPA, Hibernate, PostgreSQL |
| Build | Maven |
| Utilities | Lombok |

---

## 3. Project Structure

```
skillbridge-api/
├── pom.xml
├── src/main/
│   ├── java/com/skillbridge/skillbridge_api/
│   │   ├── SkillbridgeApiApplication.java    # Entry point
│   │   ├── config/
│   │   │   └── SecurityConfig.java            # Security, CORS, JWT filter
│   │   ├── controller/
│   │   │   ├── AuthController.java            # POST /auth/login
│   │   │   ├── LearningController.java        # CRUD /learning
│   │   │   ├── AppliedOutcomeController.java  # CRUD /learning/{id}/apply
│   │   │   ├── DashboardController.java       # GET /dashboard
│   │   │   └── HealthController.java          # GET /health
│   │   ├── dto/
│   │   │   ├── LoginRequest.java, LoginResponse.java
│   │   │   ├── CreateLearningRequest.java, LearningResponse.java
│   │   │   ├── CreateAppliedOutcomeRequest.java, AppliedOutcomeResponse.java
│   │   │   └── DashboardResponse.java
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── LearningItem.java, LearningCategory.java, LearningItemStatus.java
│   │   │   └── AppliedOutcome.java, OutcomeType.java
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   ├── LearningItemRepository.java
│   │   │   └── AppliedOutcomeRepository.java
│   │   ├── security/
│   │   │   ├── JwtUtil.java
│   │   │   └── JwtAuthenticationFilter.java
│   │   └── service/
│   │       ├── AuthService.java
│   │       ├── LearningService.java
│   │       ├── AppliedOutcomeService.java
│   │       └── DashboardService.java
│   └── resources/
│       ├── application.yaml / application.yml
│       └── application-prod.yml
└── src/test/...
```

---

## 4. Data Model (Entities & Enums)

### 4.1 User

- **Table:** `users`
- **Fields:** `id`, `email` (unique), `password` (BCrypt), `createdAt`
- **Role:** Owns all learning items (and thus applied outcomes).

### 4.2 LearningItem

- **Table:** `learning_items`
- **Fields:** `id`, `title`, `status`, `category`, `createdAt`, `user_id` (FK → User)
- **Enums:**
  - **LearningItemStatus:** `PENDING`, `APPLIED`
  - **LearningCategory:** `TECHNICAL`, `PROFESSIONAL_SKILLS`, `NEW_LEARNINGS`, `ECONOMICS`, `WORLD_TRADE`, `UPSC`, `BANK_EXAM`, `OTHER`

### 4.3 AppliedOutcome

- **Table:** `applied_outcomes`
- **Fields:** `id`, `description`, `type`, `createdAt`, `learning_item_id` (FK → LearningItem)
- **Enum OutcomeType:** `PROJECT`, `TASK`, `BLOG`, `WORK`

### 4.4 Relationships

- **User** → **LearningItem:** one-to-many (user owns many learning items).
- **LearningItem** → **AppliedOutcome:** one-to-many (one learning item can have many outcomes).
- When the first outcome is added to a learning item, its status becomes `APPLIED`; when the last outcome is deleted, it goes back to `PENDING`.

---

## 5. API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/auth/login` | No | Login or signup; returns JWT. |
| GET | `/health` | No | Health check. |
| GET | `/dashboard` | Yes | Aggregates: total, applied, pending learning count. |
| POST | `/learning` | Yes | Create learning item. |
| GET | `/learning` | Yes | List current user’s learning items. |
| GET | `/learning/{id}` | Yes | Get one learning item (owner only). |
| PUT | `/learning/{id}` | Yes | Update learning item (owner only). |
| DELETE | `/learning/{id}` | Yes | Delete learning item (owner only). |
| POST | `/learning/{learningId}/apply` | Yes | Add applied outcome; sets item to APPLIED. |
| GET | `/learning/{learningId}/apply` | Yes | List outcomes for a learning item. |
| DELETE | `/learning/{learningId}/apply/{outcomeId}` | Yes | Delete outcome; if none left, set item to PENDING. |

Protected endpoints expect: `Authorization: Bearer <token>`.

---

## 6. Code Flow (Request → Response)

### 6.1 Request lifecycle (protected endpoints)

1. **HTTP request** → Tomcat.
2. **Security filter chain** (`SecurityConfig`):
   - CORS applied.
   - `JwtAuthenticationFilter` runs before `UsernamePasswordAuthenticationFilter`.
3. **JwtAuthenticationFilter:**
   - Reads `Authorization: Bearer <token>`.
   - If missing or invalid → continues without setting auth (later “anyRequest().authenticated()” returns 401).
   - If valid → parses JWT, sets `SecurityContext` principal to **email** (no roles).
4. **DispatcherServlet** routes to the right **Controller**.
5. **Controller** calls **Service** (e.g. `LearningService.create(request)`).
6. **Service:**
   - Resolves current user via `SecurityContextHolder.getContext().getAuthentication().getPrincipal()` (email) and `UserRepository.findByEmail(email)`.
   - Performs business rules (e.g. ownership checks, status updates).
   - Uses **Repository** (JPA) to read/write DB.
   - Maps entities to **DTOs** and returns.
7. **Controller** wraps result in `ResponseEntity` and returns.
8. Response is sent back to the client.

### 6.2 Auth flow (login/signup)

- **AuthController** `POST /auth/login` receives `LoginRequest` (email, password, mode).
- **AuthService.login(request):**
  - If user exists: check password with `PasswordEncoder`; if wrong → throw; if ok → generate JWT and return `LoginResponse(token)`.
  - If user does not exist and `mode == "SIGNUP"`: create `User` (password encoded), save, generate JWT, return.
  - If user does not exist and mode is login → throw `UserNotFoundException` → controller returns 404 with `{"error":"USER_NOT_FOUND"}`.
- **JwtUtil** builds token with subject = email, claim `userId`, 24h expiry, signed with `jwt.secret`.

### 6.3 Learning flow (example: create learning item)

- **LearningController** `POST /learning` → **LearningService.create(CreateLearningRequest)**.
- Service gets current user (from JWT → email → `UserRepository`).
- Builds `LearningItem` (title, category from request, status = PENDING, user = current user).
- Saves via **LearningItemRepository**.
- Maps to **LearningResponse** (id, title, status, category, createdAt) and returns.

### 6.4 Applied outcome flow (example: add outcome)

- **AppliedOutcomeController** `POST /learning/{learningId}/apply` → **AppliedOutcomeService.create(learningId, CreateAppliedOutcomeRequest)**.
- Service gets current user, loads **LearningItem** by `learningId`, checks ownership.
- Creates **AppliedOutcome** (description, type, learningItem), saves.
- Sets `LearningItem.status = APPLIED` and saves learning item.
- Returns **AppliedOutcomeResponse**.

### 6.5 Dashboard flow

- **DashboardController** `GET /dashboard` → **DashboardService.getDashboard()**.
- Service gets current user, loads all learning items for that user.
- Counts total, applied (status = APPLIED), pending (status = PENDING).
- Returns **DashboardResponse(totalLearning, appliedCount, pendingCount)**.

---

## 7. Security

- **Stateless:** session creation policy is `STATELESS`; no server-side session.
- **Public:** `/auth/login`, `/health`.
- **Protected:** all other paths require authentication (JWT).
- **CORS:** allowed origin `http://localhost:5173`; methods GET, POST, PUT, DELETE; all headers.
- **CSRF:** disabled (typical for JWT REST API).
- **401:** missing/invalid JWT handled by `authenticationEntryPoint` (sends 401 Unauthorized).
- **Password:** stored with BCrypt via `PasswordEncoder` bean in `SecurityConfig`.
- **JWT:** HMAC key from `jwt.secret`; token contains email (subject) and `userId`; 24h expiry.

---

## 8. Configuration

- **Default (application.yaml / application.yml):** app name, JPA (dialect PostgreSQL, `ddl-auto: update`, no open-in-view), server port 8080, `jwt.secret`.
- **application.yaml** in the repo can contain explicit DB URL and credentials (dev); avoid committing real secrets—use env or `application-prod.yml`.
- **application-prod.yml:** expects `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (and typically `JWT_SECRET` from base profile).

---

## 9. How to Run

- **Requirements:** Java 17, Maven, PostgreSQL (or use env/DB URL from config).
- **Build:** `mvn clean install`
- **Run:** `mvn spring-boot:run` (or run `SkillbridgeApiApplication` from IDE).
- **Health:** `GET http://localhost:8080/health`
- **Login:** `POST http://localhost:8080/auth/login` with JSON body:  
  `{"email":"...","password":"...","mode":"LOGIN"}` or `"SIGNUP"`.

---
## to add env in application.yaml follow application.yaml.example file

## 10. Summary

SkillBridge API is a layered Spring Boot app: **Controllers** expose REST; **Services** enforce auth (current user from JWT), ownership, and business rules; **Repositories** do JPA access; **Security** is JWT + filter + SecurityConfig. Data flows **User → LearningItem → AppliedOutcome**, with learning status (PENDING/APPLIED) driven by the presence of applied outcomes. You can save this as `README.md` in the project root; if you want it written to the file automatically, switch to Agent mode and ask to create/update the README.
