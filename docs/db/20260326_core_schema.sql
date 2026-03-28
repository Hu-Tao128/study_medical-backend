create extension if not exists vector with schema extensions;

create or replace function public.set_updated_at()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

create or replace function public.try_uuid(input text)
returns uuid
language plpgsql
immutable
set search_path = ''
as $$
begin
  return input::uuid;
exception
  when others then
    return null;
end;
$$;

create or replace function public.request_auth_uuid()
returns uuid
language sql
stable
set search_path = ''
as $$
  select coalesce(auth.uid(), public.try_uuid(auth.jwt() ->> 'sub'));
$$;

create or replace function public.current_user_id()
returns uuid
language sql
stable
set search_path = ''
as $$
  select u.id
  from public.users u
  where public.try_uuid(u.auth_id::text) = public.request_auth_uuid()
  limit 1;
$$;

create table if not exists public.institutions (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  domain text,
  plan text not null default 'FREE',
  created_at timestamptz not null default now(),
  constraint chk_institutions_plan check (plan in ('FREE', 'PRO', 'ENTERPRISE'))
);

create table if not exists public.groups (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  type text not null default 'STUDY',
  description text,
  institution_id uuid,
  created_by uuid,
  created_at timestamptz not null default now(),
  constraint chk_groups_type check (type in ('STUDY', 'CLASS'))
);

create table if not exists public.memberships (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null,
  group_id uuid not null,
  role text not null default 'STUDENT',
  status text not null default 'ACTIVE',
  joined_at timestamptz not null default now(),
  constraint chk_memberships_role check (role in ('STUDENT', 'TEACHER')),
  constraint chk_memberships_status check (status in ('ACTIVE', 'PENDING', 'BANNED')),
  constraint uk_memberships_user_group unique (user_id, group_id)
);

create table if not exists public.topics (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  parent_id uuid,
  description text
);

create table if not exists public.user_progress (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null,
  topic_id uuid not null,
  accuracy double precision,
  attempts integer not null default 0,
  streak_days integer not null default 0,
  time_spent_minutes integer not null default 0,
  last_score double precision,
  last_studied_at timestamptz,
  constraint uk_user_progress_user_topic unique (user_id, topic_id)
);

create table if not exists public.study_sessions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null,
  topic_id uuid not null,
  mode text not null,
  total_questions integer,
  correct_answers integer,
  accuracy double precision,
  started_at timestamptz not null default now(),
  ended_at timestamptz,
  constraint chk_study_sessions_mode check (mode in ('flashcards', 'quiz'))
);

create table if not exists public.embeddings (
  id uuid primary key default gen_random_uuid(),
  content_type text not null,
  content_id uuid not null,
  topic_id uuid,
  embedding extensions.vector(1536) not null,
  created_at timestamptz not null default now()
);

drop policy if exists "Usuario lee su propio perfil" on public.users;
drop policy if exists "Usuario crea su propio perfil" on public.users;
drop policy if exists "Usuario edita su propio perfil" on public.users;

alter table public.users
  alter column auth_id type uuid using auth_id::uuid,
  alter column auth_id set not null,
  alter column email set not null,
  alter column display_name drop not null,
  alter column role type text using role::text,
  alter column role set not null,
  alter column role set default 'STUDENT',
  alter column preferred_language set default 'es',
  alter column preferred_language set not null,
  alter column theme set default 'system',
  alter column theme set not null,
  alter column created_at set default now(),
  alter column created_at set not null,
  alter column updated_at set default now(),
  alter column updated_at set not null;

alter table public.users
  add column if not exists career text,
  add column if not exists last_active_at timestamptz,
  add column if not exists is_active boolean default true;

alter table public.users
  alter column is_active set default true,
  alter column is_active set not null;

alter table public.groups
  add column if not exists type text default 'STUDY';

alter table public.groups
  alter column type set default 'STUDY',
  alter column type set not null;

alter table public.memberships
  add column if not exists status text default 'ACTIVE';

alter table public.memberships
  alter column status set default 'ACTIVE',
  alter column status set not null;

alter table public.user_progress
  add column if not exists streak_days integer default 0,
  add column if not exists time_spent_minutes integer default 0;

alter table public.user_progress
  alter column streak_days set default 0,
  alter column streak_days set not null,
  alter column time_spent_minutes set default 0,
  alter column time_spent_minutes set not null;

alter table public.embeddings
  add column if not exists topic_id uuid;

alter table public.study_sessions
  add column if not exists accuracy double precision;

do $$
begin
  if not exists (
    select 1 from pg_constraint where conname = 'chk_users_role'
  ) then
    alter table public.users
      add constraint chk_users_role check (role in ('STUDENT', 'TEACHER', 'ADMIN'));
  end if;
end;
$$;

do $$
begin
  if not exists (
    select 1 from pg_constraint where conname = 'fk_users_institution'
  ) then
    alter table public.users
      add constraint fk_users_institution
      foreign key (institution_id)
      references public.institutions(id)
      on delete set null;
  end if;
end;
$$;

do $$
begin
  if not exists (
    select 1 from pg_constraint where conname = 'chk_groups_type'
  ) then
    alter table public.groups
      add constraint chk_groups_type check (type in ('STUDY', 'CLASS'));
  end if;

  if not exists (
    select 1 from pg_constraint where conname = 'fk_groups_institution'
  ) then
    alter table public.groups
      add constraint fk_groups_institution
      foreign key (institution_id)
      references public.institutions(id)
      on delete set null;
  end if;

  if not exists (
    select 1 from pg_constraint where conname = 'fk_groups_created_by'
  ) then
    alter table public.groups
      add constraint fk_groups_created_by
      foreign key (created_by)
      references public.users(id)
      on delete set null;
  end if;
end;
$$;

do $$
begin
  if not exists (
    select 1 from pg_constraint where conname = 'chk_memberships_status'
  ) then
    alter table public.memberships
      add constraint chk_memberships_status check (status in ('ACTIVE', 'PENDING', 'BANNED'));
  end if;

  if not exists (
    select 1 from pg_constraint where conname = 'fk_memberships_user'
  ) then
    alter table public.memberships
      add constraint fk_memberships_user
      foreign key (user_id)
      references public.users(id)
      on delete cascade;
  end if;

  if not exists (
    select 1 from pg_constraint where conname = 'fk_memberships_group'
  ) then
    alter table public.memberships
      add constraint fk_memberships_group
      foreign key (group_id)
      references public.groups(id)
      on delete cascade;
  end if;
end;
$$;

do $$
begin
  if not exists (
    select 1 from pg_constraint where conname = 'fk_topics_parent'
  ) then
    alter table public.topics
      add constraint fk_topics_parent
      foreign key (parent_id)
      references public.topics(id)
      on delete set null;
  end if;
end;
$$;

do $$
begin
  if not exists (
    select 1 from pg_constraint where conname = 'fk_study_sessions_user'
  ) then
    alter table public.study_sessions
      add constraint fk_study_sessions_user
      foreign key (user_id)
      references public.users(id)
      on delete cascade;
  end if;

  if not exists (
    select 1 from pg_constraint where conname = 'fk_study_sessions_topic'
  ) then
    alter table public.study_sessions
      add constraint fk_study_sessions_topic
      foreign key (topic_id)
      references public.topics(id)
      on delete cascade;
  end if;

  if not exists (
    select 1 from pg_constraint where conname = 'fk_embeddings_topic'
  ) then
    alter table public.embeddings
      add constraint fk_embeddings_topic
      foreign key (topic_id)
      references public.topics(id)
      on delete set null;
  end if;
end;
$$;

do $$
begin
  if not exists (
    select 1 from pg_constraint where conname = 'fk_user_progress_user'
  ) then
    alter table public.user_progress
      add constraint fk_user_progress_user
      foreign key (user_id)
      references public.users(id)
      on delete cascade;
  end if;

  if not exists (
    select 1 from pg_constraint where conname = 'fk_user_progress_topic'
  ) then
    alter table public.user_progress
      add constraint fk_user_progress_topic
      foreign key (topic_id)
      references public.topics(id)
      on delete cascade;
  end if;
end;
$$;

create index if not exists idx_memberships_user_id on public.memberships(user_id);
create index if not exists idx_memberships_group_id on public.memberships(group_id);
create index if not exists idx_progress_user on public.user_progress(user_id);
create index if not exists idx_progress_topic on public.user_progress(topic_id);
create index if not exists idx_study_sessions_user on public.study_sessions(user_id);
create index if not exists idx_study_sessions_topic on public.study_sessions(topic_id);
create index if not exists idx_study_sessions_mode on public.study_sessions(mode);
create index if not exists idx_groups_institution on public.groups(institution_id);
create index if not exists idx_groups_created_by on public.groups(created_by);
create index if not exists idx_groups_type on public.groups(type);
create index if not exists idx_topics_parent on public.topics(parent_id);
create index if not exists idx_embeddings_content on public.embeddings(content_type, content_id);
create index if not exists idx_embeddings_topic on public.embeddings(topic_id);

drop trigger if exists trg_users_set_updated_at on public.users;
create trigger trg_users_set_updated_at
before update on public.users
for each row execute function public.set_updated_at();

alter table public.users enable row level security;
alter table public.institutions enable row level security;
alter table public.groups enable row level security;
alter table public.memberships enable row level security;
alter table public.topics enable row level security;
alter table public.user_progress enable row level security;
alter table public.study_sessions enable row level security;
alter table public.embeddings enable row level security;

create policy "users_select_own_profile"
on public.users
for select
using (auth_id = public.request_auth_uuid());

create policy "users_insert_own_profile"
on public.users
for insert
with check (auth_id = public.request_auth_uuid());

create policy "users_update_own_profile"
on public.users
for update
using (auth_id = public.request_auth_uuid())
with check (auth_id = public.request_auth_uuid());

create policy "institutions_select_authenticated"
on public.institutions
for select
using (public.request_auth_uuid() is not null);

create policy "institutions_admin_manage"
on public.institutions
for all
using (
  exists (
    select 1 from public.users u
    where u.auth_id = public.request_auth_uuid()
      and u.role = 'ADMIN'
  )
)
with check (
  exists (
    select 1 from public.users u
    where u.auth_id = public.request_auth_uuid()
      and u.role = 'ADMIN'
  )
);

create policy "groups_select_member_or_owner"
on public.groups
for select
using (
  public.current_user_id() is not null
  and (
    created_by = public.current_user_id()
    or exists (
      select 1
      from public.memberships m
      where m.group_id = groups.id
        and m.user_id = public.current_user_id()
    )
    or exists (
      select 1 from public.users u
      where u.auth_id = public.request_auth_uuid()
        and u.role = 'ADMIN'
    )
  )
);

create policy "groups_teacher_or_admin_insert"
on public.groups
for insert
with check (
  created_by = public.current_user_id()
  and exists (
    select 1 from public.users u
    where u.auth_id = public.request_auth_uuid()
      and u.role in ('TEACHER', 'ADMIN')
  )
);

create policy "groups_owner_or_admin_update_delete"
on public.groups
for all
using (
  created_by = public.current_user_id()
  or exists (
    select 1 from public.users u
    where u.auth_id = public.request_auth_uuid()
      and u.role = 'ADMIN'
  )
)
with check (
  created_by = public.current_user_id()
  or exists (
    select 1 from public.users u
    where u.auth_id = public.request_auth_uuid()
      and u.role = 'ADMIN'
  )
);

create policy "memberships_select_user_or_group_owner"
on public.memberships
for select
using (
  user_id = public.current_user_id()
  or exists (
    select 1
    from public.groups g
    where g.id = memberships.group_id
      and g.created_by = public.current_user_id()
  )
);

create policy "memberships_insert_group_owner"
on public.memberships
for insert
with check (
  exists (
    select 1
    from public.groups g
    where g.id = memberships.group_id
      and g.created_by = public.current_user_id()
  )
);

create policy "memberships_delete_group_owner"
on public.memberships
for delete
using (
  exists (
    select 1
    from public.groups g
    where g.id = memberships.group_id
      and g.created_by = public.current_user_id()
  )
);

create policy "user_progress_owner_all"
on public.user_progress
for all
using (user_id = public.current_user_id())
with check (user_id = public.current_user_id());

create policy "study_sessions_owner_all"
on public.study_sessions
for all
using (user_id = public.current_user_id())
with check (user_id = public.current_user_id());

create policy "topics_select_authenticated"
on public.topics
for select
using (public.request_auth_uuid() is not null);

create policy "topics_teacher_or_admin_manage"
on public.topics
for all
using (
  exists (
    select 1 from public.users u
    where u.auth_id = public.request_auth_uuid()
      and u.role in ('TEACHER', 'ADMIN')
  )
)
with check (
  exists (
    select 1 from public.users u
    where u.auth_id = public.request_auth_uuid()
      and u.role in ('TEACHER', 'ADMIN')
  )
);

create policy "embeddings_teacher_or_admin_manage"
on public.embeddings
for all
using (
  exists (
    select 1 from public.users u
    where u.auth_id = public.request_auth_uuid()
      and u.role in ('TEACHER', 'ADMIN')
  )
)
with check (
  exists (
    select 1 from public.users u
    where u.auth_id = public.request_auth_uuid()
      and u.role in ('TEACHER', 'ADMIN')
  )
);
