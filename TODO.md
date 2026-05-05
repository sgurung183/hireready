# HireReady — TODO

## Completed
- [x] `User`, `Resume`, `OptimizationResult` entities
- [x] `UserRepository`, `ResumeRepository`, `OptimizationResultRepository`
- [x] DTOs: `RegisterRequest`, `LoginRequest`, `AuthResponse`, `OptimizeRequest`, `OptimizeResponse`, `ResumeResponse`
- [x] `JwtUtil` — generate, extract, validate tokens
- [x] `CustomUserDetailsService` — load user from DB by email
- [x] `JwtFilter` — intercepts every request, validates JWT, sets user in SecurityContext
- [x] `User implements UserDetails` — Spring Security integration
- [x] `SecurityConfig` — public vs protected endpoints, JwtFilter wired in, sessions disabled
- [x] `AuthService` + `AuthController` — register and login, both return JWT
- [x] Apache PDFBox — PDF upload, text extraction, save to DB
- [x] `ResumeController` — upload, get by id, list all, delete
- [x] `LlmProvider` interface — provider-agnostic abstraction
- [x] `GeminiProvider` — Gemini 2.5 Flash (free tier), returns `optimizedText` + `matchScore`
- [x] `OptimizeService` + `OptimizeController` — optimize endpoint, history endpoint
- [x] `GlobalExceptionHandler` — 400/401/404 error responses
- [x] README

---

## Up Next — Multi-LLM Integration

The `LlmProvider` interface is already in place. The goal is to let each user bring their own API key and choose their preferred provider.

- [ ] `UserLlmConfig` entity — stores `provider` (enum: GEMINI, OPENAI, CLAUDE, OLLAMA), `apiKey`, `model` per user; one-to-one with `User`
- [ ] `UserLlmConfigRepository` + endpoints to save/update a user's LLM config (`POST /api/user/llm-config`)
- [ ] `OpenAiProvider` — implement `LlmProvider` using the OpenAI API
- [ ] `ClaudeProvider` — implement `LlmProvider` using the Anthropic API
- [ ] `OllamaProvider` — implement `LlmProvider` for local Ollama models (no API key needed)
- [ ] Provider factory / selector in `OptimizeService` — read the user's config and instantiate the right `LlmProvider` at runtime
- [ ] Update `OptimizeRequest` or user config to accept a preferred model name per call

---

## Later / Nice to Have

- [ ] Swagger/OpenAPI docs (`springdoc-openapi`) so a frontend knows the API contract
- [ ] Resume "set as main" endpoint — mark one resume as the default for optimization
- [ ] Input validation error messages (400 responses currently return the full Jakarta error object)
- [ ] Store match score history and surface trends over multiple optimization runs
