# Architecture

AI Finance Copilot uses a layered architecture with a decoupled frontend and backend.

## Runtime View

```text
Next.js frontend
  -> Spring Boot REST API
    -> Controller layer
    -> Service layer
    -> Repository layer
    -> PostgreSQL
```

Phase 2 introduces OpenAI behind an application service boundary. AI features should receive scoped, validated financial summaries rather than direct database access.

## Backend Layers

- Controller layer: HTTP endpoints, request validation, response DTOs
- Service layer: business rules, authorization checks, orchestration
- Repository layer: Spring Data JPA persistence
- Database: PostgreSQL with Flyway-managed migrations

## Frontend Layers

- `app/`: routed pages
- `components/`: reusable UI and product components
- `lib/`: API client, auth helpers, validators
- `types/`: shared TypeScript types

## Security Principles

- Passwords are hashed with BCrypt.
- JWTs identify authenticated users.
- Every financial query is scoped to the authenticated user.
- AI prompts use scoped summaries and redact unnecessary data.

