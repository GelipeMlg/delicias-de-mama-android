-- Autenticación y autorización del MVP. No abre el acceso a las tablas comerciales.
begin;
create schema if not exists privado;
revoke all on schema privado from public, anon, authenticated;
create table if not exists privado.duenas (
    usuario_id uuid primary key references auth.users(id) on delete cascade,
    creado_en timestamptz not null default now()
);
alter table privado.duenas enable row level security;
revoke all on table privado.duenas from public, anon, authenticated;

create or replace function public.es_duena() returns boolean
language sql stable security definer set search_path = ''
as $$
  select exists (
    select 1 from privado.duenas d
    join auth.users u on u.id = d.usuario_id
    where d.usuario_id = auth.uid() and u.email_confirmed_at is not null
  );
$$;
revoke all on function public.es_duena() from public, anon, authenticated;
grant execute on function public.es_duena() to authenticated;
comment on function public.es_duena() is 'Devuelve solamente si la cuenta actual y confirmada es dueña. Asignación exclusivamente administrativa.';
notify pgrst, 'reload schema';
commit;
