-- Ejecutar desde SQL Editor con una cuenta administradora, después de crear
-- y confirmar la cuenta en Supabase Auth. Nunca introducir contraseñas aquí.
-- Requiere migrations/202609220001_acceso_duena.sql.
begin;
do $$
declare
  cuenta uuid;
begin
  select id into strict cuenta
  from auth.users
  where lower(email) = 'pylyta24@gmail.com'
    and email_confirmed_at is not null;

  insert into privado.duenas(usuario_id) values (cuenta)
  on conflict (usuario_id) do nothing;
exception
  when no_data_found then
    raise exception 'Crea y confirma primero la cuenta de la dueña en Supabase Auth.';
  when too_many_rows then
    raise exception 'Hay más de una cuenta coincidente; revisar manualmente.';
end $$;
commit;

-- No modifica contraseñas ni elimina permisos de otras cuentas.
