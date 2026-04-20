# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

HireReady is a **resume optimizer** Spring Boot backend. Users upload a resume (PDF) and paste a job description; the backend sends both to an LLM (user's choice — not locked to Claude) and returns an optimized resume. The app also tracks job applications per user.

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

Spring Security + JWT is declared but not yet implemented. JWT signing/validation will use JJWT (`io.jsonwebtoken`). Stateless auth: issue a JWT on login, validate it on every protected request via a `OncePerRequestFilter`.

### Database

PostgreSQL 15 runs via Docker Compose (`docker-compose.yml`). Hibernate `ddl-auto=update` auto-migrates schema from entity changes — suitable for development. Connection: `localhost:5432/hireready`, user `postgres`, password `password123`.

### Resume Storage Strategy (to be decided)

`Resume.content` is currently a `TEXT` column (extracted text). For PDF storage, choose between: storing the binary in a `BYTEA` column, or saving to disk/S3 and storing a path. The LLM call will need the raw text extracted from the PDF, so PDF-to-text extraction is required regardless.
