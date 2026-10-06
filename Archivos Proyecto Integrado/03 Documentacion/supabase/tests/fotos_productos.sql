-- Prueba transaccional: todos los productos y objetos creados aquí se revierten.
begin;
do $$
declare
 uid uuid; pid uuid:=gen_random_uuid(); fileid uuid:=gen_random_uuid();
 path text; url text; p jsonb; updated jsonb; snapshot jsonb;
begin
 select d.usuario_id into uid from privado.duenas d join auth.users u on u.id=d.usuario_id
 where u.email_confirmed_at is not null limit 1;
 if uid is null then raise exception 'Se necesita una dueña confirmada para esta prueba'; end if;
 perform set_config('request.jwt.claim.sub',uid::text,true);
 path:=pid::text || '/' || fileid::text || '.jpg';
 url:='https://ivxeabovtxetvvxgyywa.supabase.co/storage/v1/object/public/productos/' || path;
 -- Solo metadatos temporales para probar la referencia y el CRUD, no un archivo real.
 insert into storage.objects(bucket_id,name) values('productos',path);
 p:=jsonb_build_object('id',pid,'name','Prueba foto (rollback)','description','Temporal',
 'price',1000,'active',true,'flavorsEnabled',false,'flavors','[]'::jsonb,
 'fillings','[]'::jsonb,'servings','[]'::jsonb,'imageUrl',url);
 perform public.guardar_producto_con_foto_app(null,p);
 if public.fotos_catalogo_app()->>pid::text is distinct from url then raise exception 'No guardó la foto'; end if;
 select privado.producto_json(t) || jsonb_build_object('imageUrl',coalesce(t.imagen_url,'')) into snapshot
 from public.productos t where id=pid;
 updated:=snapshot || jsonb_build_object('name','Prueba editada','imageUrl','');
 perform public.guardar_producto_con_foto_app(snapshot,updated);
 if public.fotos_catalogo_app()->>pid::text is distinct from '' then raise exception 'No quitó la foto'; end if;
 begin
  perform public.guardar_producto_con_foto_app(snapshot,p);
  raise exception 'No detectó edición concurrente';
 exception when serialization_failure then null; end;
 begin
  perform public.guardar_producto_con_foto_app(updated,updated || jsonb_build_object('imageUrl','https://example.com/ajena.jpg'));
  raise exception 'Aceptó una foto ajena';
 exception when raise_exception then
  if SQLERRM <> 'Primero sube la foto del producto' then raise; end if;
 end;
 perform set_config('request.jwt.claim.sub','00000000-0000-0000-0000-000000000001',true);
 begin
  perform public.guardar_producto_con_foto_app(updated,updated);
  raise exception 'Aceptó usuario sin rol';
 exception when insufficient_privilege then null; end;
 perform set_config('request.jwt.claim.sub',uid::text,true);
 perform public.guardar_producto_con_foto_app(updated,null);
 if exists(select 1 from public.productos where id=pid) then raise exception 'No eliminó producto temporal'; end if;
end $$;
rollback;
select 'OK: alta, edición, quitar foto, concurrencia, URL ajena, permisos y eliminación; todo revertido' as resultado;

