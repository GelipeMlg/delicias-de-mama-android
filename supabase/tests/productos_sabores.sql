-- Prueba transaccional: todos los registros sintéticos se revierten al finalizar.
begin;
select set_config('request.jwt.claims', jsonb_build_object('sub',(
 select d.usuario_id from privado.duenas d join auth.users u on u.id=d.usuario_id
 where u.email_confirmed_at is not null limit 1),'role','authenticated')::text,true);
set local role authenticated;
do $$
declare p jsonb; q jsonb; session_claims text;
begin
 if not public.es_duena() then raise exception 'Se necesita una cuenta de dueña confirmada para esta prueba'; end if;
 session_claims := current_setting('request.jwt.claims');
 p := jsonb_build_object('id',gen_random_uuid(),'name','PRUEBA TRANSACCIONAL','description','Se revierte al terminar',
 'price',1000,'active',true,'flavorsEnabled',true,'flavors',jsonb_build_array('Chocolate','Vainilla'));
 perform public.guardar_producto_app(null,p);
 if not public.catalogo_app() @> jsonb_build_array(p) then raise exception 'Alta/lectura incorrecta'; end if;
 q := p || '{"price":2000}'::jsonb;
 perform public.guardar_producto_app(p,q);
 if not public.catalogo_app() @> jsonb_build_array(q) then raise exception 'Edición incorrecta'; end if;
 begin
   perform public.guardar_producto_app(p,q);
   raise exception 'No detectó versión desactualizada';
 exception when serialization_failure then null;
 end;
 perform set_config('request.jwt.claims','{"sub":"00000000-0000-0000-0000-000000000001","role":"authenticated"}',true);
 begin
   perform public.eliminar_producto_app(q);
   raise exception 'Un cliente pudo eliminar';
 exception when insufficient_privilege then null;
 end;
 perform set_config('request.jwt.claims',session_claims,true);
 perform public.eliminar_producto_app(q);
 if public.catalogo_app() @> jsonb_build_array(jsonb_build_object('id',q->'id')) then raise exception 'No eliminó el producto'; end if;
end $$;
reset role;
rollback;
select 'OK: crear, leer, editar, eliminar, sabores, conflicto y permisos; datos de prueba revertidos' as resultado;
