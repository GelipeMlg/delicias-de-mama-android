-- Incremento 1: catálogo compartido. Mantiene cerradas las tablas comerciales.
begin;
alter table public.productos add column if not exists sabores_habilitados boolean not null default false;
alter table public.productos add column if not exists sabores text[] not null default '{}';
create or replace function public.catalogo_app() returns jsonb
language sql stable security definer set search_path = '' as $$
 select coalesce(jsonb_agg(jsonb_build_object(
   'id',id,'name',nombre,'description',coalesce(descripcion,''),
   'price',coalesce(precio_base,0),'active',activo,'flavorsEnabled',sabores_habilitados,'flavors',to_jsonb(sabores)) order by nombre,id),'[]'::jsonb)
 from public.productos where activo or public.es_duena();
$$;
revoke all on function public.catalogo_app() from public, anon, authenticated;
grant execute on function public.catalogo_app() to anon, authenticated;

create or replace function public.guardar_producto_app(anterior jsonb, siguiente jsonb)
returns void language plpgsql security definer set search_path = '' as $$
declare
 identificador uuid;
 actual jsonb;
begin
 if not public.es_duena() then
   raise exception 'Acceso exclusivo de la dueña' using errcode='42501';
 end if;
 identificador := (siguiente->>'id')::uuid;
 if identificador is null or nullif(trim(siguiente->>'name'),'') is null
    or nullif(trim(siguiente->>'description'),'') is null
    or jsonb_typeof(siguiente->'price') is distinct from 'number'
    or (siguiente->>'price')::numeric not between 0 and 999999999
    or trunc((siguiente->>'price')::numeric) <> (siguiente->>'price')::numeric
    or jsonb_typeof(siguiente->'active') is distinct from 'boolean' then
   raise exception 'Datos de producto inválidos' using errcode='22023';
 end if;
 if jsonb_typeof(siguiente->'flavorsEnabled') is distinct from 'boolean'
    or jsonb_typeof(siguiente->'flavors') is distinct from 'array' then
   raise exception 'Actualiza la aplicación para guardar los sabores' using errcode='22023';
 end if;
 if jsonb_array_length(siguiente->'flavors') > 30 or exists(
   select 1 from jsonb_array_elements(siguiente->'flavors') x
   where jsonb_typeof(x) <> 'string' or length(trim(x #>> '{}')) not between 1 and 80
     or position(' · ' in (x #>> '{}')) > 0
 ) or ((siguiente->>'flavorsEnabled')::boolean and jsonb_array_length(siguiente->'flavors')=0)
   or (not (siguiente->>'flavorsEnabled')::boolean and jsonb_array_length(siguiente->'flavors')<>0) then
   raise exception 'Revisa los sabores disponibles del producto' using errcode='22023';
 end if;
 -- Serializa también dos altas del mismo UUID. No bloquea todo el catálogo.
 perform pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtextextended(identificador::text,0));
 select jsonb_build_object('id',id,'name',nombre,'description',coalesce(descripcion,''),
   'price',coalesce(precio_base,0),'active',activo,'flavorsEnabled',sabores_habilitados,'flavors',to_jsonb(sabores)) into actual
 from public.productos where id=identificador for update;
 if actual is distinct from nullif(anterior,'null'::jsonb) then
   raise exception 'El producto cambió. Actualiza el catálogo antes de guardar.' using errcode='40001';
 end if;
 insert into public.productos(id,nombre,descripcion,precio_base,activo,sabores_habilitados,sabores)
 values(identificador,trim(siguiente->>'name'),trim(siguiente->>'description'),
   (siguiente->>'price')::numeric,(siguiente->>'active')::boolean,(siguiente->>'flavorsEnabled')::boolean,array(select jsonb_array_elements_text(siguiente->'flavors')))
 on conflict(id) do update set nombre=excluded.nombre,descripcion=excluded.descripcion,
   precio_base=excluded.precio_base,activo=excluded.activo,sabores_habilitados=excluded.sabores_habilitados,sabores=excluded.sabores;
end;
$$;
revoke all on function public.guardar_producto_app(jsonb,jsonb) from public, anon, authenticated;
grant execute on function public.guardar_producto_app(jsonb,jsonb) to authenticated;
comment on function public.guardar_producto_app(jsonb,jsonb) is
 'CRUD de productos con comprobación de dueña y concurrencia optimista. Retirar significa activo=false.';
create or replace function public.eliminar_producto_app(anterior jsonb) returns void
language plpgsql security definer set search_path='' as $$
declare identificador uuid; actual jsonb;
begin
 if not public.es_duena() then raise exception 'Acceso exclusivo de la dueña' using errcode='42501'; end if;
 identificador := (anterior->>'id')::uuid;
 if identificador is null then raise exception 'Falta el identificador' using errcode='22023'; end if;
 perform pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtextextended(identificador::text,0));
 select jsonb_build_object('id',id,'name',nombre,'description',coalesce(descripcion,''),
 'price',coalesce(precio_base,0),'active',activo,'flavorsEnabled',sabores_habilitados,'flavors',to_jsonb(sabores))
 into actual from public.productos where id=identificador for update;
 if actual is null then return; end if;
 if actual is distinct from anterior then raise exception 'El producto cambió. Actualiza antes de eliminar.' using errcode='40001'; end if;
 begin
   delete from public.productos where id=identificador;
 exception when foreign_key_violation then
   update public.productos set activo=false where id=identificador;
 end;
end;
$$;
revoke all on function public.eliminar_producto_app(jsonb) from public,anon,authenticated;
grant execute on function public.eliminar_producto_app(jsonb) to authenticated;
notify pgrst,'reload schema';
commit;
