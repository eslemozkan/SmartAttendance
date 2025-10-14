-- QR Codes table for time-bound attendance QR generation
create extension if not exists pgcrypto;

create table if not exists public.qr_codes (
    id uuid primary key default gen_random_uuid(),
    assignment_id bigint not null,
    created_at timestamptz not null default now(),
    expire_after_minutes integer not null check (expire_after_minutes > 0),
    is_active boolean not null default true
);

alter table public.qr_codes enable row level security;

-- Only the service role can insert/select/update/delete by default.
drop policy if exists qr_codes_service_all on public.qr_codes;
create policy qr_codes_service_all on public.qr_codes
    for all
    to authenticated, anon
    using (auth.role() = 'service_role')
    with check (auth.role() = 'service_role');

-- Helpful index for lookups during validation
create index if not exists idx_qr_codes_assignment_created_at
    on public.qr_codes (assignment_id, created_at);


