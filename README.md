# ARTVERSE — Foundation

Phase 0 of the ARTVERSE build: a working, end-to-end skeleton with a Java/Spring Boot backend and
a React/TypeScript frontend, wired together through a real authentication flow (register, login,
JWT access + refresh tokens, protected routes). Visual language follows the vintage/museum design
tokens from the spec (paper, ink, oxblood, aged brass; serif display type).

## Stack

- **Backend:** Java 21, Spring Boot 3.3, Spring Security, JWT (jjwt), Spring Data JPA, PostgreSQL, Flyway, springdoc-openapi
- **Frontend:** React 18, TypeScript, Vite, Tailwind CSS, React Router, Zustand, Axios

## Project layout

```
artverse/
├── backend/                  Spring Boot API (com.artverse package)
│   ├── src/main/java/com/artverse/
│   │   ├── auth/              register / login / refresh / me
│   │   ├── security/           JWT filter, JWT service, UserDetailsService
│   │   ├── user/                User entity, Role enum, repository
│   │   ├── config/              Spring Security + CORS config
│   │   ├── common/               ApiResponse envelope, ApiException, global error handler
│   │   └── health/                GET /api/health
│   └── src/main/resources/
│       ├── application.yml
│       └── db/migration/V1__init_schema.sql   (Flyway)
├── frontend/                 React app
│   └── src/
│       ├── pages/              Home, Login, Register, Dashboard
│       ├── components/layout/  Header
│       ├── routes/              ProtectedRoute
│       ├── store/                Zustand auth store (persisted to localStorage)
│       ├── lib/                   axios client with auto token-refresh, auth API calls
│       └── types/                 shared TS types
├── docker-compose.yml
└── .env.example
```

## Run it locally

### Option A — Docker Compose (recommended)

```bash
cp .env.example .env      # edit JWT_SECRET and DB_PASSWORD before any real use
docker compose up --build
```

- Backend: http://localhost:8080 (Swagger UI at `/swagger-ui.html`)
- Frontend: http://localhost:5173
- Postgres: localhost:5432

### Option B — Run each piece by hand

**Postgres** (or point `DB_URL` at any Postgres instance):
```bash
docker run -d --name artverse-db -e POSTGRES_DB=artverse \
  -e POSTGRES_USER=artverse -e POSTGRES_PASSWORD=artverse \
  -p 5432:5432 postgres:16-alpine
```

**Backend:**
```bash
cd backend
export JWT_SECRET=$(openssl rand -base64 48)
mvn spring-boot:run
```
Flyway runs the migration automatically on startup — no manual schema step needed.

**Frontend:**
```bash
cd frontend
npm install
npm run dev
```

> Note: this sandbox's network allowlist doesn't include Maven Central, so the Java side could not
> be `mvn compile`-verified here. It was written and reviewed carefully, but run a build on your
> machine before relying on it. The frontend **was** installed, type-checked (`tsc -b`), and
> production-built successfully in this environment.

## What's implemented

- `POST /api/v1/auth/register` — creates a user (BCrypt-hashed password), returns access + refresh tokens
- `POST /api/v1/auth/login` — authenticates, returns access + refresh tokens
- `POST /api/v1/auth/refresh` — exchanges a valid refresh token for a new token pair
- `GET  /api/v1/auth/me` — returns the authenticated user (requires `Authorization: Bearer <token>`)
- `GET  /api/health` — liveness check
- Stateless JWT security filter chain; BCrypt password hashing; role model (`USER`, `ARTIST`,
  `GALLERY`, `CURATOR`, `ADMIN`, `MODERATOR`) via an `@ElementCollection` on `User`
- Consistent `ApiResponse<T>` envelope on every response, with a global exception handler for
  validation errors, auth failures, conflicts, and unhandled exceptions
- Frontend: axios interceptor that attaches the access token and transparently retries a request
  once on 401 by refreshing the token; Zustand store persists the session to `localStorage`;
  `ProtectedRoute` guards `/dashboard` and redirects to `/login`

## What's deliberately out of scope for this phase

Everything else in the master spec — artworks, artists, marketplace, auctions, the 3D virtual
museum, AI curator, admin panel, search, messaging, etc. Those are separate build phases on top of
this foundation. See the roadmap in the vintage spec (section 26) for the suggested order.

## Next steps

1. Add the `artworks` / `artists` domains (entities, migrations, DTOs, endpoints) and an Explore
   page in the frontend.
2. Add `/studio` and `/admin` route shells guarded by role, not just authentication.
3. Add Vitest + JUnit test suites for what's here before layering on more domains.
