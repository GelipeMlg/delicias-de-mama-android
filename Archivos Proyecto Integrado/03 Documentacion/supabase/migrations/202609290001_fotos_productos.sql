-- Añade únicamente almacenamiento y RPC de fotos del catálogo.
-- Las funciones existentes de pedidos, finanzas y agenda no se modifican.
begin;
insert into storage.buckets(id,name,public,file_size_limit,allowed_mime_types)
values('productos','productos',true,5242880,array['image/jpeg'])
on conflict(id) do update set public=true,file_size_limit=excluded.file_size_limit,allowed_mime_types=excluded.allowed_mime_types;
create policy fotos_productos_insertar_duena on storage.objects
for insert to authenticated with check(bucket_id='productos' and public.es_duena()
and name ~ '^[0-9a-f-]{36}/[0-9a-f-]{36}[.]jpg$');
create policy fotos_productos_leer_duena on storage.objects
for select to authenticated using(bucket_id='productos' and public.es_duena());

create function public.fotos_catalogo_app() returns jsonb
language sql stable security definer set search_path='' as $$
 select coalesce(jsonb_object_agg(id::text,coalesce(imagen_url,'')),'{}'::jsonb)
 from public.productos where activo or public.es_duena();
$$;
revoke all on function public.fotos_catalogo_app() from public,anon,authenticated;
grant execute on function public.fotos_catalogo_app() to anon,authenticated;

-- Una sola transacción para el producto y la referencia de su foto.
create function public.guardar_producto_con_foto_app(anterior jsonb,siguiente jsonb) returns void
language plpgsql security definer set search_path='' as $$
declare ident uuid; foto_actual text;
begin
 if not public.es_duena() then raise exception 'Acceso exclusivo de la dueña' using errcode='42501'; end if;
 anterior:=nullif(anterior,'null'::jsonb); siguiente:=nullif(siguiente,'null'::jsonb);
 ident:=coalesce(siguiente->>'id',anterior->>'id')::uuid;
 if ident is null then raise exception 'Falta identificador'; end if;
 perform pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtextextended(ident::text,0));
 select coalesce(imagen_url,'') into foto_actual from public.productos where id=ident for update;
 if anterior is not null and coalesce(foto_actual,'') is distinct from anterior->>'imageUrl' then
  raise exception 'La foto cambió. Actualiza el catálogo.' using errcode='40001';
 end if;
 if siguiente is not null then
  if jsonb_typeof(siguiente->'imageUrl') is distinct from 'string' then raise exception 'Foto inválida'; end if;
  if siguiente->>'imageUrl' <> '' and not exists(
   select 1 from storage.objects o where o.bucket_id='productos'
   and (storage.foldername(o.name))[1]=ident::text
   and siguiente->>'imageUrl' = 'https://ivxeabovtxetvvxgyywa.supabase.co/storage/v1/object/public/productos/' || o.name
  ) then raise exception 'Primero sube la foto del producto'; end if;
 end if;
 perform public.guardar_cambio_app('product',anterior-'imageUrl',siguiente-'imageUrl');
 if siguiente is not null then
  update public.productos set imagen_url=nullif(siguiente->>'imageUrl','') where id=ident;
 end if;
end;
$$;
revoke all on function public.guardar_producto_con_foto_app(jsonb,jsonb) from public,anon,authenticated;
grant execute on function public.guardar_producto_con_foto_app(jsonb,jsonb) to authenticated;
notify pgrst,'reload schema';
commit;

