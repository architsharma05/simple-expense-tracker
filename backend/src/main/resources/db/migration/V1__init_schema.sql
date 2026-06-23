create extension if not exists pgcrypto;

create table app_users (
    id uuid primary key default gen_random_uuid(),
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    created_at timestamptz not null default now()
);

create table transactions (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references app_users(id) on delete cascade,
    type varchar(20) not null check (type in ('INCOME', 'EXPENSE')),
    category varchar(100) not null,
    amount numeric(12, 2) not null check (amount > 0),
    description text,
    transaction_date date not null,
    created_at timestamptz not null default now()
);

create table budgets (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references app_users(id) on delete cascade,
    category varchar(100) not null,
    monthly_limit numeric(12, 2) not null check (monthly_limit >= 0),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint uq_budgets_user_category unique (user_id, category)
);

create table goals (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references app_users(id) on delete cascade,
    goal_name varchar(150) not null,
    target_amount numeric(12, 2) not null check (target_amount > 0),
    target_date date not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table ai_insights (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references app_users(id) on delete cascade,
    insight_text text not null,
    generated_at timestamptz not null default now()
);

create table receipts (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references app_users(id) on delete cascade,
    file_url text not null,
    uploaded_at timestamptz not null default now()
);

create index idx_transactions_user_date on transactions (user_id, transaction_date desc);
create index idx_transactions_user_category on transactions (user_id, category);
create index idx_transactions_user_type on transactions (user_id, type);
create index idx_ai_insights_user_generated_at on ai_insights (user_id, generated_at desc);
create index idx_receipts_user_uploaded_at on receipts (user_id, uploaded_at desc);

