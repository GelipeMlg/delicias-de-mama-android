-- Ejecutar como postgres. Solo comprueba permisos; no crea usuarios ni asigna dueñas.
begin;
do $$
begin
  assert not has_schema_privilege('anon', 'privado', 'USAGE');
  assert not has_schema_privilege('authenticated', 'privado', 'USAGE');
  assert not has_table_privilege('authenticated', 'privado.duenas', 'INSERT');
  assert not has_table_privilege('authenticated', 'privado.duenas', 'UPDATE');
  assert not has_table_privilege('authenticated', 'privado.duenas', 'SELECT');
  assert not has_function_privilege('anon', 'public.es_duena()', 'EXECUTE');
  assert has_function_privilege('authenticated', 'public.es_duena()', 'EXECUTE');
end $$;
set local role authenticated;
select set_config('request.jwt.claims', '{"sub":"00000000-0000-0000-0000-000000000001","role":"authenticated"}', true);
do $$ begin assert public.es_duena() = false; end $$;
rollback;
