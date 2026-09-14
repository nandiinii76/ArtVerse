# ARTVERSE — Digital Museum & Art Marketplace

ARTVERSE is a Java/Spring Boot + React platform for discovering, cataloguing and eventually trading art. The visual direction is deliberately **vintage museum / archival**, using paper, ink, oxblood, olive and aged-brass tones instead of a generic AI dashboard aesthetic.

## Current stack
- Backend: Java 21, Spring Boot 3.3.4, Spring Security, JWT, Spring Data JPA, PostgreSQL, Flyway, OpenAPI
- Frontend: React 18, TypeScript, Vite, Tailwind CSS, React Router, Zustand, Axios
- Infrastructure: Docker Compose + PostgreSQL

## Implemented foundation
- Registration, login, refresh-token flow and authenticated `/me`
- BCrypt password hashing and stateless JWT security
- Role model: USER, ARTIST, GALLERY, CURATOR, ADMIN, MODERATOR
- Flyway database migrations
- Artwork catalogue entity and REST API
- Artwork search by title/description/style/medium and category filtering
- Pagination and sorting
- Artwork create, update, view and delete operations with artist ownership checks
- Artist profile entity and `/api/v1/artists` API
- Favorites, follows, comments, collections and notifications database model
- Marketplace listings, orders and ownership database model
- Auction and bid database model ready for live-bidding implementation
- Vintage Explore catalogue, artwork detail page and protected Artist Studio
- Museum-style landing page and expanded navigation
- Docker Compose development environment

## API highlights
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `GET /api/v1/auth/me`
- `GET /api/v1/artworks?q=&category=&page=&size=&sort=`
- `GET /api/v1/artworks/{id}`
- `POST /api/v1/artworks`
- `PUT /api/v1/artworks/{id}`
- `DELETE /api/v1/artworks/{id}`
- `GET /api/v1/artists/{userId}`
- `PUT /api/v1/artists/me`
- `GET /api/health`

## Frontend routes
- `/` — museum landing page
- `/explore` — searchable collection
- `/artworks/:id` — artwork catalogue detail
- `/login` and `/register`
- `/dashboard` — protected account area
- `/studio` — protected artist catalogue form

## Run locally

### Docker
```bash
cp .env.example .env
docker compose up --build
```

Frontend: `http://localhost:5173`  
Backend: `http://localhost:8080`  
Swagger: `http://localhost:8080/swagger-ui.html`  
PostgreSQL: `localhost:5432`

### Manual
```bash
cd backend
mvn spring-boot:run
```

In another terminal:
```bash
cd frontend
npm install
npm run dev
```

## Roadmap

The database is intentionally prepared for the larger platform while the application is built incrementally:

1. Artwork and artist catalogue — implemented
2. Explore/search — implemented at relational-search level
3. Image/object storage and provenance documents
4. Favorites, follows, comments, collections and notifications
5. Marketplace checkout/payment integration
6. Auctions and live bids with WebSocket
7. AI curator and semantic/vector search
8. Three.js/React Three Fiber virtual museum
9. Kafka event architecture and real-time activity
10. Role-based admin/moderation console
11. JUnit/Mockito/Testcontainers + Vitest/RTL test suites
12. Actuator/Micrometer observability and GitHub Actions CI/CD

## Important
The project is now a substantially expanded application skeleton, but external payment providers, cloud storage credentials, AI provider credentials, Kafka/OpenSearch infrastructure and production deployment still need environment-specific configuration. Run `mvn test` and `npm run build` locally after pulling the latest changes.
