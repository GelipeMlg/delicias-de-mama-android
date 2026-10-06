-- Incremento 2. Aplicar después de 202609260001. No copia pedidos de demostración.
begin;
alter table public.productos add column if not exists rellenos text[] not null default '{}';
alter table public.productos add column if not exists porciones integer[] not null default '{}';
alter table public.pedidos add column if not exists revision integer not null default 1;
alter table public.pedidos add column if not exists nombre_contacto text;
alter table public.pedidos add column if not exists contacto text;
alter table public.detalle_pedido add column if not exists personas integer check(personas between 1 and 1000);
alter table public.ventas alter column pedido_id drop not null;
alter table public.ventas add column if not exists concepto text;

create table public.app_changes(id integer primary key check(id=1), version bigint not null default 0);
insert into public.app_changes values(1,0);
alter table public.app_changes enable row level security;
revoke all on public.app_changes from public,anon,authenticated;
grant select on public.app_changes to anon,authenticated;
create policy leer_version on public.app_changes for select to anon,authenticated using(true);
create function privado.cambio_app() returns trigger language plpgsql security definer set search_path='' as $$
begin update public.app_changes set version=version+1 where id=1; return null; end; $$;
do $$ declare t text; begin
 foreach t in array array['productos','pedidos','disponibilidad','cotizaciones','ventas','gastos'] loop
 execute format('create trigger avisar_app after insert or update or delete on public.%I for each statement execute function privado.cambio_app()',t);
 end loop;
 if exists(select 1 from pg_publication where pubname='supabase_realtime') then
 alter publication supabase_realtime add table public.app_changes;
 end if;
end $$;

create table public.notificaciones_app(
 id uuid primary key default gen_random_uuid(), usuario_id uuid not null references auth.users(id) on delete cascade,
 pedido_id uuid not null references public.pedidos(id), titulo text not null, cuerpo text not null,
 creado_en timestamptz not null default now()
);
alter table public.notificaciones_app enable row level security;
revoke all on public.notificaciones_app from public,anon,authenticated;
create index notificaciones_usuario_fecha on public.notificaciones_app(usuario_id,creado_en);
create table public.dispositivos_app(token text primary key, usuario_id uuid not null references auth.users(id) on delete cascade, expira_en timestamptz not null);
alter table public.dispositivos_app enable row level security;
revoke all on public.dispositivos_app from public,anon,authenticated;
create index dispositivos_usuario on public.dispositivos_app(usuario_id);
create table public.envios_push_app(
 id uuid primary key default gen_random_uuid(), notificacion_id uuid not null references public.notificaciones_app(id) on delete cascade,
 token text not null references public.dispositivos_app(token) on delete cascade,
 enviado boolean not null default false, intentos integer not null default 0,
 disponible_en timestamptz not null default now(), lease uuid, unique(notificacion_id,token)
);
alter table public.envios_push_app enable row level security;
revoke all on public.envios_push_app from public,anon,authenticated;
grant all on public.envios_push_app,public.dispositivos_app,public.notificaciones_app to service_role;

create function privado.producto_json(p public.productos) returns jsonb language sql immutable set search_path='' as $$
 select jsonb_build_object('id',p.id,'name',p.nombre,'description',coalesce(p.descripcion,''),'price',coalesce(p.precio_base,0),'active',p.activo,
 'flavorsEnabled',p.sabores_habilitados,'flavors',p.sabores,'fillings',p.rellenos,'servings',p.porciones);
$$;
create function privado.pedido_json(identificador uuid) returns jsonb language sql stable set search_path='' as $$
 select jsonb_build_object('id',p.id,'customer',coalesce(p.nombre_contacto,c.nombre),'contact',coalesce(p.contacto,c.telefono,c.email,''),
 'customerUserId',c.usuario_id,'productId',d.producto_id,'productName',d.descripcion,'quantity',d.cantidad,'date',p.fecha_entrega,
 'time',to_char(p.hora_entrega,'HH24:MI'),'flavor',coalesce(d.sabores,''),'filling',coalesce(d.rellenos,''),'theme',coalesce(d.tematica,''),
 'restrictions',coalesce(d.restricciones_alimentarias,''),'servings',d.personas,'revision',p.revision,
 'status',case p.estado when 'solicitado' then 'REQUESTED' when 'cotizado' then 'QUOTED' when 'confirmado' then 'CONFIRMED'
 when 'en_preparacion' then 'CONFIRMED' when 'entregado' then 'DELIVERED' else 'CANCELLED' end,
 'quote',q.monto,'saleDate',v.fecha_venta::date)
 from public.pedidos p join public.clientes c on c.id=p.cliente_id
 join lateral(select * from public.detalle_pedido where pedido_id=p.id order by creado_en,id limit 1) d on true
 left join lateral(select monto from public.cotizaciones where pedido_id=p.id order by version desc limit 1) q on true
 left join public.ventas v on v.pedido_id=p.id where p.id=identificador;
$$;
create function privado.venta_json(v public.ventas) returns jsonb language sql immutable set search_path='' as $$
 select jsonb_build_object('id',v.id,'concept',coalesce(v.concepto,'Pedido entregado'),'amount',v.total,'date',v.fecha_venta::date,'orderId',v.pedido_id);
$$;
create function privado.gasto_json(g public.gastos) returns jsonb language sql immutable set search_path='' as $$
 select jsonb_build_object('id',g.id,'concept',g.concepto,'amount',g.monto,'date',g.fecha_gasto,'category',g.categoria);
$$;
create function public.estado_app() returns jsonb language sql stable security definer set search_path='' as $$
 select jsonb_build_object(
 'products',(select coalesce(jsonb_agg(privado.producto_json(p) order by p.nombre,p.id),'[]') from public.productos p where p.activo or public.es_duena()),
 'orders',(select coalesce(jsonb_agg(privado.pedido_json(p.id) order by p.creado_en,p.id) filter(where privado.pedido_json(p.id) is not null),'[]') from public.pedidos p join public.clientes c on c.id=p.cliente_id where auth.uid() is not null and (c.usuario_id=auth.uid() or public.es_duena())),
 'days',(select coalesce(jsonb_agg(jsonb_build_object('date',fecha,'capacity',capacidad_pedidos,'blocked',bloqueada) order by fecha),'[]') from public.disponibilidad),
 'occupancy',(select coalesce(jsonb_object_agg(fecha_entrega,cantidad),'{}') from (select fecha_entrega,count(*) cantidad from public.pedidos where estado not in('cancelado','rechazado') group by fecha_entrega) x),
 'expenses',(select coalesce(jsonb_agg(privado.gasto_json(g) order by g.fecha_gasto,g.id),'[]') from public.gastos g where public.es_duena()),
 'sales',(select coalesce(jsonb_agg(privado.venta_json(v) order by v.fecha_venta,v.id),'[]') from public.ventas v where public.es_duena() and v.estado='registrada'),
 'notifications',(select coalesce(jsonb_agg(n.dato),'[]') from (select jsonb_build_object('id',id,'userId',usuario_id,'title',titulo,'body',cuerpo) dato from public.notificaciones_app where usuario_id=auth.uid() and creado_en>now()-interval '1 day' order by creado_en desc limit 30) n));
$$;

create function privado.notificar_pedido(pid uuid, titulo text) returns void language plpgsql set search_path='' as $$
begin
 with inserted as (
 insert into public.notificaciones_app(usuario_id,pedido_id,titulo,cuerpo)
 select u,pid,titulo,'Abre la aplicación para ver los detalles de tu pedido.' from (
 select c.usuario_id u from public.pedidos p join public.clientes c on c.id=p.cliente_id where p.id=pid
 union select d.usuario_id from privado.duenas d join auth.users a on a.id=d.usuario_id where a.email_confirmed_at is not null
 ) recipients where u is not null and u is distinct from auth.uid() returning id,usuario_id
 ) insert into public.envios_push_app(notificacion_id,token)
 select i.id,d.token from inserted i join public.dispositivos_app d on d.usuario_id=i.usuario_id and d.expira_en>now();
end; $$;

create function public.guardar_cambio_app(tipo text, anterior jsonb, siguiente jsonb) returns void
language plpgsql security definer set search_path='' as $$
#variable_conflict use_variable
declare
 uid uuid:=auth.uid(); owner boolean:=public.es_duena(); ident uuid; actual jsonb; fecha date;
 prod public.productos; pedido public.pedidos; conf public.disponibilidad;
 cid uuid; cantidad integer; monto numeric; estado text; qid uuid; vers integer;
 fills text[]; portions integer[];
begin
 anterior:=nullif(anterior,'null'::jsonb); siguiente:=nullif(siguiente,'null'::jsonb);
 if uid is null then raise exception 'Inicia sesión' using errcode='42501'; end if;
 if tipo <> 'order' and not owner then raise exception 'Acceso exclusivo de la dueña' using errcode='42501'; end if;
 if tipo='day' then
  fecha:=(siguiente->>'date')::date;
  if fecha is null or jsonb_typeof(siguiente->'capacity') is distinct from 'number' or (siguiente->>'capacity')::numeric <> trunc((siguiente->>'capacity')::numeric)
  or (siguiente->>'capacity')::integer not between 0 and 100 or jsonb_typeof(siguiente->'blocked') is distinct from 'boolean' then raise exception 'Disponibilidad inválida'; end if;
  perform pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtextextended(fecha::text,2));
  select jsonb_build_object('date',d.fecha,'capacity',d.capacidad_pedidos,'blocked',d.bloqueada) into actual from public.disponibilidad d where d.fecha=fecha for update;
  if actual is distinct from anterior then raise exception 'La agenda cambió. Actualiza.' using errcode='40001'; end if;
  select count(*) into cantidad from public.pedidos p where p.fecha_entrega=fecha and p.estado not in('cancelado','rechazado');
  if cantidad>(siguiente->>'capacity')::integer then raise exception 'La capacidad no puede ser menor a los pedidos comprometidos'; end if;
  insert into public.disponibilidad(fecha,capacidad_pedidos,bloqueada) values(fecha,(siguiente->>'capacity')::integer,(siguiente->>'blocked')::boolean)
  on conflict on constraint disponibilidad_pkey do update set capacidad_pedidos=excluded.capacidad_pedidos,bloqueada=excluded.bloqueada;
  return;
 end if;
 ident:=coalesce(siguiente->>'id',anterior->>'id')::uuid;
 if ident is null then raise exception 'Falta identificador'; end if;
 if anterior is not null and (anterior->>'id')::uuid is distinct from ident then raise exception 'Identificador incompatible'; end if;
 perform pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtextextended(ident::text,0));
 if tipo='product' then
  select * into prod from public.productos where id=ident for update;
  actual:=case when found then privado.producto_json(prod) else null end;
  if actual is distinct from anterior then raise exception 'El producto cambió. Actualiza.' using errcode='40001'; end if;
  if siguiente is null then perform public.eliminar_producto_app(anterior-'fillings'-'servings'); return; end if;
  if jsonb_typeof(siguiente->'fillings') is distinct from 'array' or jsonb_typeof(siguiente->'servings') is distinct from 'array' then raise exception 'Faltan opciones del producto'; end if;
  if jsonb_array_length(siguiente->'fillings')>30 or jsonb_array_length(siguiente->'servings')>30
   or exists(select 1 from jsonb_array_elements(siguiente->'fillings') x where jsonb_typeof(x)<>'string' or length(trim(x#>>'{}')) not between 1 and 80 or position(' · ' in(x#>>'{}'))>0)
   or exists(select 1 from jsonb_array_elements(siguiente->'servings') x where jsonb_typeof(x)<>'number' or (x#>>'{}')::numeric not between 1 and 1000 or trunc((x#>>'{}')::numeric)<>(x#>>'{}')::numeric)
   or (not (siguiente->>'flavorsEnabled')::boolean and jsonb_array_length(siguiente->'fillings')>0) then raise exception 'Opciones de producto inválidas'; end if;
  perform public.guardar_producto_app(anterior-'fillings'-'servings',siguiente-'fillings'-'servings');
  select array(select distinct trim(jsonb_array_elements_text(siguiente->'fillings'))) into fills;
  select array(select distinct (jsonb_array_elements_text(siguiente->'servings'))::integer) into portions;
  update public.productos set rellenos=fills,porciones=portions where id=ident;
  return;
 end if;
 if tipo='order' then
  if siguiente is null then raise exception 'Cancela el pedido para conservar el historial'; end if;
  select * into pedido from public.pedidos where id=ident for update;
  if found then
   if not owner and not exists(select 1 from public.clientes where id=pedido.cliente_id and usuario_id=uid) then raise exception 'Pedido ajeno' using errcode='42501'; end if;
   actual:=privado.pedido_json(ident);
   if anterior is null and pedido.estado='solicitado' and (siguiente-'revision')=(actual-'revision'-'productName'-'customerUserId'-'saleDate') then return; end if;
   if anterior is null or (anterior->>'revision')::integer is distinct from pedido.revision then raise exception 'El pedido cambió. Actualiza.' using errcode='40001'; end if;
   actual:=privado.pedido_json(ident);
   if (siguiente-'status'-'quote'-'revision') is distinct from (actual-'status'-'quote'-'revision'-'productName'-'customerUserId'-'saleDate') then raise exception 'Los datos del pedido no pueden cambiar en esta operación'; end if;
   fecha:=pedido.fecha_entrega;
   perform pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtextextended(fecha::text,2));
   estado:=siguiente->>'status';
   if estado='QUOTED' and owner and pedido.estado in('solicitado','cotizado','confirmado') then
    monto:=(siguiente->>'quote')::numeric;
    if monto is null or monto not between 1 and 999999999 or monto<>trunc(monto) then raise exception 'Cotización inválida'; end if;
    select coalesce(max(version),0)+1 into vers from public.cotizaciones where pedido_id=ident;
    update public.cotizaciones set estado='reemplazada' where pedido_id=ident and cotizaciones.estado in('enviada','aceptada');
    insert into public.cotizaciones(pedido_id,version,monto,detalle,estado) values(ident,vers,monto,'Cotización del pedido','enviada');
    update public.pedidos set estado='cotizado',revision=revision+1 where id=ident;
    perform privado.notificar_pedido(ident,'Recibiste una cotización');
   elsif estado='CONFIRMED' and pedido.estado='cotizado' and not owner then
    if siguiente->'quote' is distinct from actual->'quote' then raise exception 'Cotización modificada'; end if;
    select * into conf from public.disponibilidad where disponibilidad.fecha=fecha for update;
    select count(*) into cantidad from public.pedidos p where p.fecha_entrega=fecha and p.estado not in('cancelado','rechazado');
    if fecha<(now() at time zone 'America/Punta_Arenas')::date or conf.bloqueada or cantidad>conf.capacidad_pedidos then raise exception 'Fecha bloqueada o sin cupos'; end if;
    update public.cotizaciones set estado='aceptada' where pedido_id=ident and cotizaciones.estado='enviada';
    update public.pedidos set estado='confirmado',revision=revision+1 where id=ident;
    perform privado.notificar_pedido(ident,'El cliente aceptó la cotización');
   elsif estado='DELIVERED' and owner and pedido.estado in('confirmado','en_preparacion') then
    select c.id,c.monto into qid,monto from public.cotizaciones c where c.pedido_id=ident and c.estado='aceptada';
    if qid is null then raise exception 'Falta cotización aceptada'; end if;
    insert into public.ventas(pedido_id,cotizacion_id,total,concepto) values(ident,qid,monto,'Pedido de '||(actual->>'customer'));
    update public.pedidos set estado='entregado',revision=revision+1 where id=ident;
    perform privado.notificar_pedido(ident,'Pedido entregado');
   elsif estado='CANCELLED' and pedido.estado not in('entregado','cancelado','rechazado') then
    update public.cotizaciones set estado='rechazada' where pedido_id=ident and cotizaciones.estado in('enviada','aceptada');
    update public.pedidos set estado='cancelado',revision=revision+1 where id=ident;
    perform privado.notificar_pedido(ident,'Pedido cancelado');
   else raise exception 'Cambio de estado no permitido' using errcode='42501'; end if;
  else
   if anterior is not null then raise exception 'El pedido ya no existe' using errcode='40001'; end if;
   if coalesce(siguiente->>'status','')<>'REQUESTED' or nullif(siguiente->'quote','null'::jsonb) is not null then raise exception 'Estado inicial inválido'; end if;
   if nullif(trim(siguiente->>'customer'),'') is null or nullif(trim(siguiente->>'contact'),'') is null then raise exception 'Faltan nombre o contacto'; end if;
   fecha:=(siguiente->>'date')::date; cantidad:=(siguiente->>'quantity')::integer;
   if fecha is null or fecha<(now() at time zone 'America/Punta_Arenas')::date or cantidad is null or cantidad not between 1 and 100 then raise exception 'Fecha o cantidad inválida'; end if;
   perform pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtextextended(fecha::text,2));
   insert into public.disponibilidad(fecha,capacidad_pedidos) values(fecha,3) on conflict on constraint disponibilidad_pkey do nothing;
   select * into conf from public.disponibilidad where disponibilidad.fecha=fecha for update;
   if conf.bloqueada or (select count(*) from public.pedidos p where p.fecha_entrega=fecha and p.estado not in('cancelado','rechazado'))>=conf.capacidad_pedidos then raise exception 'La fecha está bloqueada o sin cupos'; end if;
   select * into prod from public.productos where id=(siguiente->>'productId')::uuid and activo for share;
   if not found then raise exception 'Producto no disponible'; end if;
   if (prod.sabores_habilitados and not coalesce((siguiente->>'flavor')=any(prod.sabores),false))
     or (not prod.sabores_habilitados and coalesce(siguiente->>'flavor','')<>'') then raise exception 'Elige un solo sabor disponible'; end if;
   if (prod.sabores_habilitados and cardinality(prod.rellenos)>0 and not coalesce((siguiente->>'filling')=any(prod.rellenos),false))
     or ((not prod.sabores_habilitados or cardinality(prod.rellenos)=0) and coalesce(siguiente->>'filling','')<>'') then raise exception 'Elige un solo relleno disponible'; end if;
   if (cardinality(prod.porciones)>0 and not coalesce((siguiente->>'servings')::integer=any(prod.porciones),false))
     or (cardinality(prod.porciones)=0 and nullif(siguiente->'servings','null'::jsonb) is not null) then raise exception 'Cantidad de personas no disponible'; end if;
   insert into public.clientes(usuario_id,nombre,telefono) values(uid,trim(siguiente->>'customer'),trim(siguiente->>'contact'))
   on conflict(usuario_id) do update set nombre=excluded.nombre,telefono=excluded.telefono returning id into cid;
   insert into public.pedidos(id,cliente_id,fecha_entrega,hora_entrega,tipo_entrega,nombre_contacto,contacto)
   values(ident,cid,fecha,(siguiente->>'time')::time,'retiro',trim(siguiente->>'customer'),trim(siguiente->>'contact'));
   insert into public.detalle_pedido(pedido_id,producto_id,descripcion,cantidad,sabores,rellenos,tematica,restricciones_alimentarias,personas)
   values(ident,prod.id,prod.nombre,cantidad,siguiente->>'flavor',siguiente->>'filling',siguiente->>'theme',siguiente->>'restrictions',(siguiente->>'servings')::integer);
   perform privado.notificar_pedido(ident,'Nuevo pedido recibido');
  end if;
  return;
 end if;
 if tipo='expense' then
  select privado.gasto_json(g) into actual from public.gastos g where id=ident for update;
 elsif tipo='sale' then
  select privado.venta_json(v) into actual from public.ventas v where id=ident and v.estado='registrada' for update;
 else raise exception 'Operación desconocida'; end if;
 if actual is distinct from anterior then raise exception 'El movimiento cambió. Actualiza.' using errcode='40001'; end if;
 if siguiente is null then
  if tipo='expense' then delete from public.gastos where id=ident;
  else update public.ventas set estado='anulada' where id=ident; end if;
  return;
 end if;
 monto:=(siguiente->>'amount')::numeric; fecha:=(siguiente->>'date')::date;
 if nullif(trim(siguiente->>'concept'),'') is null or monto is null or monto not between 1 and 999999999 or monto<>trunc(monto) or fecha is null then raise exception 'Movimiento inválido'; end if;
 if tipo='expense' then
  insert into public.gastos(id,concepto,monto,fecha_gasto,categoria) values(ident,trim(siguiente->>'concept'),monto,fecha,siguiente->>'category')
  on conflict(id) do update set concepto=excluded.concepto,monto=excluded.monto,fecha_gasto=excluded.fecha_gasto,categoria=excluded.categoria;
 else
  if nullif(siguiente->'orderId','null'::jsonb) is distinct from nullif(anterior->'orderId','null'::jsonb) then raise exception 'No puedes cambiar el pedido de una venta'; end if;
  insert into public.ventas(id,concepto,total,fecha_venta) values(ident,trim(siguiente->>'concept'),monto,fecha::timestamptz)
  on conflict(id) do update set concepto=excluded.concepto,total=excluded.total,fecha_venta=excluded.fecha_venta;
 end if;
end; $$;

create function public.registrar_dispositivo_app(token text,persistente boolean) returns void language plpgsql security definer set search_path='' as $$
#variable_conflict use_variable
begin
 if auth.uid() is null then raise exception 'Inicia sesión' using errcode='42501'; end if;
 if length(token) not between 20 and 4096 then raise exception 'Token inválido'; end if;
 -- Cambiar de cuenta elimina envíos pendientes destinados a la anterior.
 delete from public.dispositivos_app d where d.token=registrar_dispositivo_app.token and d.usuario_id<>auth.uid();
 insert into public.dispositivos_app values(token,auth.uid(),now()+case when persistente then interval '30 days' else interval '1 day' end)
 on conflict on constraint dispositivos_app_pkey do update set expira_en=excluded.expira_en;
end; $$;
create function public.retirar_dispositivo_app(token text) returns void language sql security definer set search_path='' as $$
 delete from public.dispositivos_app d where d.token=retirar_dispositivo_app.token and d.usuario_id=auth.uid();
$$;
create function public.reclamar_push_app() returns jsonb language plpgsql security definer set search_path='' as $$
declare result jsonb;
begin
 with jobs as (select e.id from public.envios_push_app e join public.dispositivos_app d on d.token=e.token
 join public.notificaciones_app n on n.id=e.notificacion_id and n.usuario_id=d.usuario_id
 where not e.enviado and e.intentos<8 and e.disponible_en<=now() and d.expira_en>now() and n.creado_en>now()-interval '1 day'
 order by e.disponible_en for update of e skip locked limit 50),
 claimed as (update public.envios_push_app e set intentos=intentos+1,disponible_en=now()+interval '2 minutes',lease=gen_random_uuid() from jobs j where e.id=j.id returning e.*)
 select coalesce(jsonb_agg(jsonb_build_object('jobId',c.id,'lease',c.lease,'token',c.token,'eventId',n.id,'userId',n.usuario_id,'title',n.titulo,'body',n.cuerpo)),'[]') into result
 from claimed c join public.notificaciones_app n on n.id=c.notificacion_id;
 return result;
end; $$;
revoke all on all functions in schema privado from public,anon,authenticated;
revoke all on function public.estado_app(),public.guardar_cambio_app(text,jsonb,jsonb),public.registrar_dispositivo_app(text,boolean),public.retirar_dispositivo_app(text),public.reclamar_push_app() from public,anon,authenticated;
grant execute on function public.estado_app() to anon,authenticated;
grant execute on function public.guardar_cambio_app(text,jsonb,jsonb),public.registrar_dispositivo_app(text,boolean),public.retirar_dispositivo_app(text) to authenticated;
grant execute on function public.reclamar_push_app() to service_role;
notify pgrst,'reload schema';
commit;
