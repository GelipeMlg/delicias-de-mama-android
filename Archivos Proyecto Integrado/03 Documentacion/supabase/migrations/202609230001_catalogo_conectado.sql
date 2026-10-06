-- Incremento 1: catálogo compartido. Mantiene cerradas las tablas comerciales.
begin;
create or replace function public.catalogo_app() returns jsonb
language sql stable security definer set search_path = '' as $$
 select coalesce(jsonb_agg(jsonb_build_object(
   'id',id,'name',nombre,'description',coalesce(descripcion,''),
   'price',coalesce(precio_base,0),'active',activo) order by nombre,id),'[]'::jsonb)
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
 -- Serializa también dos altas del mismo UUID. No bloquea todo el catálogo.
 perform pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtextextended(identificador::text,0));
 select jsonb_build_object('id',id,'name',nombre,'description',coalesce(descripcion,''),
   'price',coalesce(precio_base,0),'active',activo) into actual
 from public.productos where id=identificador for update;
 if actual is distinct from nullif(anterior,'null'::jsonb) then
   raise exception 'El producto cambió. Actualiza el catálogo antes de guardar.' using errcode='40001';
 end if;
 insert into public.productos(id,nombre,descripcion,precio_base,activo)
 values(identificador,trim(siguiente->>'name'),trim(siguiente->>'description'),
   (siguiente->>'price')::numeric,(siguiente->>'active')::boolean)
 on conflict(id) do update set nombre=excluded.nombre,descripcion=excluded.descripcion,
   precio_base=excluded.precio_base,activo=excluded.activo;
end;
$$;
revoke all on function public.guardar_producto_app(jsonb,jsonb) from public, anon, authenticated;
grant execute on function public.guardar_producto_app(jsonb,jsonb) to authenticated;
comment on function public.guardar_producto_app(jsonb,jsonb) is
 'CRUD de productos con comprobación de dueña y concurrencia optimista. Retirar significa activo=false.';
notify pgrst,'reload schema';
commit;
