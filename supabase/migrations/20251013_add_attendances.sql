-- Attendance records created by QR validation
create extension if not exists pgcrypto;

create table if not exists public.attendances (
    id uuid primary key default gen_random_uuid(),
    assignment_id bigint not null,
    student_id text not null,
    marked_at timestamptz not null default now(),
    method text not null default 'qr',
    -- optional metadata
    meta jsonb default '{}'::jsonb
);

alter table public.attendances enable row level security;

-- Unique per assignment/student per day to prevent duplicate scans the same day
create unique index if not exists uq_attendance_assignment_student_day
on public.attendances (
  assignment_id,
  student_id,
  (date_trunc('day', marked_at))
);

-- Service role policy for server-side inserts
drop policy if exists attendances_service_insert on public.attendances;
create policy attendances_service_insert on public.attendances
  for insert
  to authenticated, anon
  with check (auth.role() = 'service_role');

-- Allow users to read only their own records (if desired). Adjust as needed.
drop policy if exists attendances_self_select on public.attendances;
create policy attendances_self_select on public.attendances
  for select
  to authenticated
  using (student_id = auth.uid()::text);


