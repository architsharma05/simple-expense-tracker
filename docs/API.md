# API Design

Base path: `/api`

## Auth

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`

## Transactions

- `GET /api/transactions`
- `POST /api/transactions`
- `GET /api/transactions/{id}`
- `PUT /api/transactions/{id}`
- `DELETE /api/transactions/{id}`

Supported filters:

- `type`
- `category`
- `from`
- `to`
- `search`

## Dashboard

- `GET /api/dashboard/summary?month=YYYY-MM`
- `GET /api/dashboard/category-summary?from=YYYY-MM-DD&to=YYYY-MM-DD`
- `GET /api/dashboard/monthly-trends?year=YYYY`

## Budgets

- `GET /api/budgets`
- `POST /api/budgets`
- `PUT /api/budgets/{id}`
- `DELETE /api/budgets/{id}`

## Goals

- `GET /api/goals`
- `POST /api/goals`
- `PUT /api/goals/{id}`
- `DELETE /api/goals/{id}`

## AI, Phase 2

- `POST /api/ai/spending-summary`
- `POST /api/ai/budget-coach`
- `POST /api/ai/chat`
- `GET /api/ai/insights`
- `POST /api/ai/goal-coach`
- `POST /api/ai/categorize-transaction`

## Analytical Insights

- `GET /api/insights/recurring-expenses`
- `GET /api/insights/anomalies`


## Forecasting

- `GET /api/forecast/month-end`
- `GET /api/forecast/categories`

## Receipts

- `GET /api/receipts`
- `POST /api/receipts/upload`
- `POST /api/receipts/{id}/extract`
- `DELETE /api/receipts/{id}`
