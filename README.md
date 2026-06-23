# AI Finance Copilot

AI Finance Copilot is a production-oriented personal finance platform evolved from a simple Java Swing expense tracker. The project is designed as a portfolio-quality full-stack application demonstrating Java backend engineering, API design, PostgreSQL data modeling, secure authentication, modern frontend development, and AI-powered financial insights.

## Project Status

Phase 1 MVP is implemented:

- Original Swing + CSV application preserved in `legacy-swing/`
- Spring Boot backend scaffold added in `backend/`
- Next.js frontend scaffold added in `frontend/`
- PostgreSQL schema migration added
- Architecture, API, database, and roadmap docs added
- Backend auth foundation added: users, registration, login, JWT security, `/api/auth/me`, validation, and standard API responses
- Phase 1 finance features added: transaction CRUD/search/filtering, budget management, dashboard summaries, category summaries, monthly trends, and a Next.js MVP UI

## Target Stack

- Frontend: Next.js, TypeScript, Tailwind CSS, shadcn/ui, Recharts
- Backend: Java 21, Spring Boot, Spring Security, Spring Data JPA, Maven
- Database: PostgreSQL
- Authentication: JWT
- AI: OpenAI API
- Documentation: Swagger/OpenAPI
- Deployment: Vercel frontend, Render or AWS backend, managed PostgreSQL

## Repository Layout

```text
legacy-swing/   Original Java Swing tracker
backend/        Spring Boot REST API
frontend/       Next.js web application
docs/           Architecture and planning docs
```

## Development Roadmap

See `docs/ROADMAP.md`.

