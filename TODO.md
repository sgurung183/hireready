# HireReady — TODO

## Completed
- [x] `User`, `Resume`, `OptimizationResult` entities
- [x] `UserRepository`, `ResumeRepository`, `OptimizationResultRepository`
- [x] DTOs: `RegisterRequest`, `LoginRequest`, `AuthResponse`, `OptimizeRequest`, `OptimizeResponse`, `ResumeResponse`
- [x] `JwtUtil` — generate, extract, validate tokens
- [x] `CustomUserDetailsService` — load user from DB by email
- [x] `JwtFilter` — intercepts every request, validates JWT, sets user in SecurityContext
- [x] `User implements UserDetails` — Spring Security integration

---

## In Progress — Authentication

- [ ] `SecurityConfig` — define public vs protected endpoints, plug in `JwtFilter`, disable sessions
- [ ] `AuthService` — register (hash password, save user, return token) + login (verify password, return token)
- [ ] `AuthController` — `POST /api/auth/register` and `POST /api/auth/login`
- [ ] Test auth with Postman: register → get token → use token on a protected endpoint

---

## Up Next — Resume

- [ ] Add Apache PDFBox dependency to `pom.xml` for PDF text extraction
- [ ] `ResumeService` — upload PDF, extract text, save `Resume` entity, return `ResumeResponse`
- [ ] `ResumeController` — `POST /api/resumes/upload` (multipart), `GET /api/resumes` (list user's resumes)

---

## Up Next — Optimization

- [ ] Design `LlmProvider` interface (provider-agnostic: Claude, OpenAI, Gemini)
- [ ] Implement at least one `LlmProvider` (e.g. OpenAI or Claude)
- [ ] `OptimizationService` — fetch resume text from DB + job description → send to LLM → save `OptimizationResult` → return `OptimizeResponse`
- [ ] `OptimizationController` — `POST /api/optimize`, `GET /api/optimize/{id}`

---

## Later / Nice to Have

- [ ] Set `JWT_SECRET` and `JWT_EXPIRATION` as real environment variables on the machine
- [ ] Error handling — global `@ControllerAdvice` for clean error responses
- [ ] Input validation error messages (currently Jakarta validation errors return ugly 400s)
- [ ] Swagger/OpenAPI docs (`springdoc-openapi`) so frontend knows the API contract
- [ ] Resume "set as main" endpoint — mark one resume as the default for optimization
- [ ] Decide on PDF storage strategy (text only vs binary BYTEA vs S3 path)
