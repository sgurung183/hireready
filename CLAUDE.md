# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

HireReady is a **resume optimizer** Spring Boot backend. Users upload a resume (PDF) and paste a job description; the backend sends both to an LLM (user's choice — not locked to Claude) and returns an optimized resume.

**Stack:** Java 21, Spring Boot 3.5, Spring Security + JWT (JJWT 0.11.5), Spring Data JPA, PostgreSQL, Lombok.

## Commands

```bash
# Start the database (required before running the app)
docker-compose up -d

# Run the application
./mvnw spring-boot:run          # macOS/Linux
mvnw.cmd spring-boot:run        # Windows

# Build (skip tests)
./mvnw clean package -DskipTests

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=MyTestClassName
```

## Architecture

The package root is `com.hireready.hireready`. Layers to build follow the standard Spring pattern: `entity` → `repository` → `service` → `controller`.

### DTOs
DTOs live in `dto.request` (incoming) and `dto.response` (outgoing). No `DTO` suffix on class names. Request DTOs use `@Data` + Jakarta validation. Response DTOs use `@Data` + `@AllArgsConstructor`.

### Data Model

Three core entities forming a single ownership chain: `User` → `Resume` → `OptimizationResult`.

- **`User`** (`users` table) — holds `fullName`, `email`, `password`, `visaStatus` (F1/OPT/CPT/H1B/CITIZEN/OTHER), `role` (USER/ADMIN), `createdAt`. Has `@OneToMany` to `Resume`.
- **`Resume`** (`resumes` table) — stores resume content as `TEXT` (`@Lob`), `fileName`, `isMain`, `createdAt`. Belongs to one `User` via `user_id` FK. Has `@OneToMany` to `OptimizationResult`.
- **`OptimizationResult`** (`optimization_results` table) — stores `jobDescriptionText`, `optimizedText`, `matchScore`, `createdAt`. Belongs to one `Resume` via `resume_id` FK. No direct `user` FK — user is reached through `resume.user`.

### Multi-LLM Design Goal

The LLM integration layer should be **provider-agnostic**. Design a `LlmProvider` interface (or similar abstraction) so users can plug in Claude, OpenAI, Gemini, etc. via configuration (e.g., API key + provider name stored per user or in app config). The core optimization logic should call this interface, not any provider SDK directly.

### Security

Spring Security + JWT. Stateless auth — no sessions, no cookies. A JWT is issued on login/register and the client attaches it to every future request via the `Authorization: Bearer <token>` header.

**Files built so far (`security` package):**

- **`JwtUtil`** — the JWT toolbox. Three jobs: generate a token (on login/register), extract the email from a token, validate a token. Uses JJWT 0.11.5. Secret key and expiration are read from environment variables `JWT_SECRET` and `JWT_EXPIRATION` (defaults to 24h). Never hardcoded.

- **`CustomUserDetailsService`** — implements Spring Security's `UserDetailsService` interface. One method: `loadUserByUsername(email)` — hits the DB via `UserRepository` and returns the `User`. Called by `JwtFilter` to confirm the email in the token belongs to a real user.

- **`JwtFilter`** — extends `OncePerRequestFilter`, runs on every request before any controller. Reads the `Authorization` header, extracts the token, validates it via `JwtUtil`, loads the user via `CustomUserDetailsService`, then stores the authenticated user in `SecurityContextHolder` so any controller can access them via `@AuthenticationPrincipal`.

- **`User` implements `UserDetails`** — required so Spring Security can work directly with our `User` entity. Implements `getAuthorities()` (returns `ROLE_USER` or `ROLE_ADMIN`), `getUsername()` (returns email), and four account-state methods (all return true for now).

**Token flow:**
1. User registers or logs in → `AuthService` saves user, calls `JwtUtil.generateToken(email)` → token returned in `AuthResponse`
2. Every future request → `JwtFilter` intercepts → validates token → sets user in `SecurityContextHolder` → controller runs
3. Public endpoints (`/api/auth/**`) → `JwtFilter` sees no token → skips → `SecurityConfig` allows through

### Database

PostgreSQL 15 runs via Docker Compose (`docker-compose.yml`). Hibernate `ddl-auto=update` auto-migrates schema from entity changes — suitable for development. Connection: `localhost:5432/hireready`, user `postgres`, password `password123`.

### Resume Storage Strategy

`Resume.content` stores extracted plain text (not the raw PDF binary). PDF-to-text extraction uses **Apache PDFBox** (`Loader.loadPDF` + `PDFTextStripper`) inside `ResumeService.extractFile()`. The binary PDF is not persisted — only the extracted text is saved to the DB.

---

## What Has Been Built

### Fully implemented
- **Entities:** `User` (implements `UserDetails`), `Resume`, `OptimizationResult`, `Role` enum, `VisaStatus` enum
- **Repositories:** `UserRepository`, `ResumeRepository`, `OptimizationResultRepository`
- **Security:** `JwtUtil`, `JwtFilter`, `CustomUserDetailsService`, `SecurityConfig`
- **Auth flow:** `AuthController`, `AuthService` — register and login endpoints, both return a JWT in `AuthResponse`
- **Resume:** `ResumeController` (`POST /api/resumes/upload`, `GET /api/resumes/{id}`, `GET /api/resumes/findAll`, `DELETE /api/resumes/{id}`), `ResumeService` (PDF extraction via PDFBox, save to DB, fetch by id+owner, list all, delete, isMain uniqueness enforced)
- **LLM layer:** `LlmProvider` interface, `GeminiProvider` implementation (Gemini 2.5 Flash — free tier model, returns `optimizedText` + `matchScore` as JSON, strips markdown fences before parsing)
- **Optimize:** `OptimizeService` (fetch resume → call LLM → save `OptimizationResult` → return response with DB-generated id), `OptimizeController` (`POST /api/optimize/{resumeId}`, `GET /api/optimize/{resumeId}/history`)
- **Error handling:** `GlobalExceptionHandler` (`@RestControllerAdvice`) — `ResourceNotFoundException` → 404, `InvalidCredentialsException` → 401, `MethodArgumentNotValidException` → 400
- **DTOs:** `RegisterRequest`, `LoginRequest`, `AuthResponse`, `OptimizeRequest`, `OptimizeResponse`, `ResumeResponse`

### Runtime fixes applied during testing
- Removed `@Lob` from `Resume.content` — PostgreSQL's `TEXT` type doesn't need LOB streaming and it caused `Unable to access lob stream` errors on reads outside a transaction
- `GeminiProvider` uses `GOOGLE_API_KEY` env var (not `GEMINI_API_KEY`) — must be set before starting the app
- `OptimizeService` now builds the response from the saved `OptimizationResult` entity so the DB-generated `id` is returned instead of null
- Gemini model changed from `gemini-2.0-flash` to `gemini-2.5-flash` — 2.0 Flash is not included in the free tier

## Current Status

Core feature set is complete and manually tested via Postman. The app is in a **demo/early stage** — only Gemini 2.5 Flash (free tier) is wired up as the LLM provider. Optimization quality reflects that constraint.

## Next To-Dos

### Multi-LLM integration (main focus)
1. Add `UserLlmConfig` entity — `provider` (GEMINI/OPENAI/CLAUDE/OLLAMA), `apiKey`, `model`, one-to-one with `User`
2. Add `UserLlmConfigRepository` and a `POST /api/user/llm-config` endpoint to let users save their config
3. Implement `OpenAiProvider` (OpenAI Chat Completions API)
4. Implement `ClaudeProvider` (Anthropic Messages API)
5. Implement `OllamaProvider` (local models, no API key)
6. Add provider factory/selector in `OptimizeService` — read user's config, instantiate the right `LlmProvider` at runtime

### Environment setup note
- `GOOGLE_API_KEY` must be set before running the app
- Option 1: create a `.env` file in the project root (loaded automatically via `spring-dotenv`)
- Option 2: set in the current PowerShell session with `$env:GOOGLE_API_KEY = "your_key"`

