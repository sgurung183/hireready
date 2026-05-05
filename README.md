# HireReady

> **Demo / Early Stage** — This is an initial proof-of-concept. Only the Gemini 2.5 Flash provider is implemented, using the free tier API. Optimization quality is limited by the free tier's constraints and the early state of the project. Results should be treated as a starting point, not a production-ready output.

A resume optimizer REST API built with Spring Boot. Upload your resume (PDF) and paste a job description — HireReady sends both to an LLM and returns a tailored resume with a match score.

## Tech Stack

- **Java 21** / **Spring Boot 3.5**
- **Spring Security** + **JWT** (JJWT 0.11.5) — stateless auth
- **Spring Data JPA** + **PostgreSQL 15**
- **Apache PDFBox** — PDF text extraction
- **Google Gemini 2.5 Flash** — LLM provider (free tier)
- **Lombok**

## Prerequisites

- Java 21+
- Maven
- Docker (for PostgreSQL)
- A [Google AI Studio](https://aistudio.google.com) API key

## Setup

**1. Clone and enter the project**
```bash
git clone <repo-url>
cd hireready
```

**2. Start the database**
```bash
docker-compose up -d
```

**3. Set environment variables**

On Windows (PowerShell):
```powershell
$env:GOOGLE_API_KEY = "your_google_api_key"
$env:JWT_SECRET = "your_jwt_secret"        # optional, has a dev default
```

On macOS/Linux:
```bash
export GOOGLE_API_KEY="your_google_api_key"
export JWT_SECRET="your_jwt_secret"
```

Or create a `.env` file in the project root (loaded automatically via spring-dotenv):
```
GOOGLE_API_KEY=your_google_api_key
JWT_SECRET=your_jwt_secret
```

**4. Run the app**
```bash
# Windows
mvnw.cmd spring-boot:run

# macOS/Linux
./mvnw spring-boot:run
```

The API is available at `http://localhost:8080`.

## API Endpoints

All endpoints except `/api/auth/**` require a `Authorization: Bearer <token>` header.

### Auth

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user, returns JWT |
| POST | `/api/auth/login` | Login, returns JWT |

**Register body:**
```json
{
  "fullName": "Jane Doe",
  "email": "jane@example.com",
  "password": "secret123",
  "visaStatus": "OPT"
}
```
Visa status options: `F1`, `OPT`, `CPT`, `H1B`, `CITIZEN`, `OTHER`

**Login body:**
```json
{
  "email": "jane@example.com",
  "password": "secret123"
}
```

### Resumes

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/resumes/upload` | Upload a PDF resume |
| GET | `/api/resumes/{id}` | Get a resume by ID |
| GET | `/api/resumes/findAll` | List all resumes for the authenticated user |
| DELETE | `/api/resumes/{id}` | Delete a resume |

Upload uses `multipart/form-data` with a `file` field (PDF only). Optionally include `isMain=true` to mark it as your primary resume.

### Optimize

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/optimize/{resumeId}` | Optimize a resume against a job description |
| GET | `/api/optimize/{resumeId}/history` | Get optimization history for a resume |

**Optimize body:**
```json
{
  "jobDescription": "We are looking for a software engineer with 3+ years of experience..."
}
```

**Optimize response:**
```json
{
  "id": 1,
  "optimizedText": "...",
  "matchScore": 87.5,
  "createdAt": "2026-05-04T12:00:00"
}
```

## Project Structure

```
src/main/java/com/hireready/hireready/
├── config/         # CORS config
├── controller/     # AuthController, ResumeController, OptimizeController
├── dto/
│   ├── request/    # RegisterRequest, LoginRequest, OptimizeRequest
│   └── response/   # AuthResponse, ResumeResponse, OptimizeResponse
├── entity/         # User, Resume, OptimizationResult, Role, VisaStatus
├── exception/      # GlobalExceptionHandler, ResourceNotFoundException, InvalidCredentialsException
├── repository/     # UserRepository, ResumeRepository, OptimizationResultRepository
├── security/       # JwtUtil, JwtFilter, CustomUserDetailsService, SecurityConfig
└── service/
    ├── llm/        # LlmProvider interface, GeminiProvider
    ├── AuthService.java
    ├── ResumeService.java
    └── OptimizeService.java
```

## Data Model

```
User ──< Resume ──< OptimizationResult
```

- **User** — stores credentials, visa status, and role
- **Resume** — stores extracted plain text (not the raw PDF binary), linked to one user
- **OptimizationResult** — stores the optimized resume text and match score, linked to one resume

## Adding a New LLM Provider

The LLM layer is provider-agnostic. Implement the `LlmProvider` interface:

```java
public interface LlmProvider {
    OptimizeResponse optimize(String resumeContent, String jobDescription);
}
```

Then register your implementation as a `@Service` and swap it in via Spring's dependency injection or a configuration flag.

## Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `GOOGLE_API_KEY` | Yes | — | Google AI Studio API key for Gemini |
| `JWT_SECRET` | No | `hireready-super-secret-jwt-key-dev-only-2024` | JWT signing secret |
| `JWT_EXPIRATION` | No | `86400000` (24h) | Token expiry in milliseconds |
