# Database Design

The database is PostgreSQL and uses UUID primary keys for public-facing records.

## Core Tables

- `app_users`: application accounts
- `transactions`: income and expense records
- `budgets`: monthly category budget limits
- `goals`: financial goals
- `ai_insights`: stored AI-generated insight history
- `receipts`: uploaded receipt metadata

## Design Decisions

- `numeric(12, 2)` is used for money to avoid floating-point precision errors.
- `app_users` avoids naming friction with PostgreSQL user-related keywords.
- Most tables include `user_id` and cascade deletes so account cleanup is straightforward.
- Query indexes favor dashboard and filtering use cases.

See `backend/src/main/resources/db/migration/V1__init_schema.sql`.

