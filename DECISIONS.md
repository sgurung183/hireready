# HireReady — Decisions & Changes Log

## Entity Layer

### Resume.java
- Renamed `resumeName` → `fileName` to match `ResumeRepository.findByFileNameIgnoreCase()`
- Added `isMain` (boolean) — no annotation needed, maps to a plain boolean column in Postgres. Used by `ResumeRepository.findByUserIdAndIsMain()`
- Added `createdAt` (LocalDateTime) with `@Column(nullable = false, updatable = false)` and `@PrePersist` to auto-set on insert
- Added `@Builder` — entities are not Spring beans, they are constructed manually in the service layer. Builder keeps that clean without needing an all-args constructor call
- Added `@Table(name = "resumes")` for explicit table naming, consistent with `User`

### JobApplication.java — removed
- Replaced by `OptimizationResult` — the app is a resume optimizer, not a job application tracker
- Users cannot apply to jobs through this app, so status tracking (APPLIED/ACCEPTED/DENIED) has no purpose

### OptimizationResult.java
- Stores one optimization session: the job description pasted, the LLM-returned optimized text, and the match score
- Belongs to `Resume` via `resume_id` FK only — no direct `user` FK needed since user is reachable through `resume.user`
- Relationship chain: `User` → `Resume` → `OptimizationResult`

## Design Decisions

### DTOs over direct Entity mapping in controllers
- Controllers will receive DTOs (e.g., `RegisterRequest`), not entities directly
- Prevents clients from setting server-side fields like `role`, `id`, or `createdAt`
- Service layer is responsible for mapping DTO → Entity using the Builder pattern
- DTOs use no `DTO` suffix — the package name makes the type clear
- Split into two subpackages: `dto.request` (client → server) and `dto.response` (server → client)
- Request DTOs use `@Data` only — Spring deserializes JSON via setters, no constructor needed
- Response DTOs use `@Data` + `@AllArgsConstructor` — service layer constructs them directly
- Request DTOs use Jakarta validation (`@NotBlank`, `@Email`, `@NotNull`) to reject bad input before it reaches the service layer

### Resume Storage Strategy
- Client uploads a PDF as `MultipartFile` — not JSON, so no DTO for the upload itself
- Service extracts text from the PDF using Apache PDFBox and stores it as `String content` in the `Resume` entity
- The raw PDF bytes are never stored — only the extracted text
- `Resume.content` (`TEXT` column) always holds plain text, never binary data

### Multi-LLM support
- The LLM integration sits behind a provider-agnostic `LlmProvider` interface
- `GeminiProvider` is the only implementation so far — uses Gemini 2.5 Flash on the free tier, which limits optimization quality; this is intentional for the demo stage
- Next step: `UserLlmConfig` entity will store provider + API key + preferred model per user so anyone can bring their own key
- No LLM provider is hardcoded in core logic — `OptimizeService` calls `LlmProvider`, not `GeminiProvider` directly

### Runtime fixes (applied during initial testing)
- Removed `@Lob` from `Resume.content` — PostgreSQL's `TEXT` type does not use LOB streaming; keeping `@Lob` caused `Unable to access lob stream` errors on reads outside a transaction
- `GeminiProvider` reads `GOOGLE_API_KEY` (not `GEMINI_API_KEY`) — matches the env var name Google's own tooling uses
- `OptimizeService` builds the response from the saved entity so the DB-generated `id` is returned (was null before)
- Gemini model changed from `gemini-2.0-flash` to `gemini-2.5-flash` — 2.0 Flash is not on the free tier

### Environment variable loading
- Added `spring-dotenv` (`me.paulschwarz:spring-dotenv:4.0.0`) so a `.env` file in the project root is loaded automatically on startup
- Without this, Spring Boot ignores `.env` files entirely and only reads system env vars
