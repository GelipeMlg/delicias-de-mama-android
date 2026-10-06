-- Pruebas de permisos sin modificar cuentas ni datos comerciales.
begin;
do $$
begin
 if not has_function_privilege('anon','public.catalogo_app()','execute') then
   raise exception 'Falta consulta anónima del catálogo';
 end if;
 if has_function_privilege('anon','public.guardar_producto_app(jsonb,jsonb)','execute') then
   raise exception 'Anónimo puede ejecutar una escritura';
 end if;
 if has_table_privilege('authenticated','public.productos','insert') then
   raise exception 'Escritura directa no autorizada';
 end if;
 if not exists(select 1 from privado.duenas d join auth.users u on u.id=d.usuario_id
   where lower(u.email)='pylyta24@gmail.com' and u.email_confirmed_at is not null) then
   raise exception 'La dueña aún no está asignada';
 end if;
end $$;
set local role authenticated;
select set_config('request.jwt.claims','{"sub":"00000000-0000-0000-0000-000000000001","role":"authenticated"}',true);
do $$
begin
 if public.es_duena() then raise exception 'Una cuenta ajena obtuvo rol de dueña'; end if;
 begin
   perform public.guardar_producto_app(null,'{}'::jsonb);
   raise exception 'Se admitió escritura sin rol';
 exception when insufficient_privilege then null;
 end;
end $$;
reset role;
rollback;
select 'Permisos del catálogo verificados' as resultado;
