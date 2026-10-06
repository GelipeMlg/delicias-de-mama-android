-- Delicias de Mama: estructura inicial. Ejecutar una sola vez.
-- No crea usuarios, datos de negocio ni acceso desde la app.
begin;

create table public.clientes (
 id uuid primary key default gen_random_uuid(),
 usuario_id uuid unique references auth.users(id) on delete set null,
 nombre text not null check (length(trim(nombre)) > 0),
 telefono text, email text, direccion text,
 creado_en timestamptz not null default now()
);
create table public.productos (
 id uuid primary key default gen_random_uuid(),
 nombre text not null check (length(trim(nombre)) > 0),
 descripcion text, categoria text, imagen_url text,
 precio_base numeric(12,2) check (precio_base >= 0),
 activo boolean not null default true,
 creado_en timestamptz not null default now()
);
create table public.disponibilidad (
 fecha date primary key,
 bloqueada boolean not null default false,
 capacidad_pedidos integer not null check (capacidad_pedidos >= 0),
 observacion_interna text,
 creado_en timestamptz not null default now()
);
create table public.pedidos (
 id uuid primary key default gen_random_uuid(),
 cliente_id uuid not null references public.clientes(id),
 fecha_entrega date not null references public.disponibilidad(fecha),
 hora_entrega time not null,
 tipo_entrega text not null check (tipo_entrega in ('retiro','despacho')),
 direccion_entrega text,
 especificaciones text,
 estado text not null default 'solicitado' check (estado in ('solicitado','cotizado','confirmado','en_preparacion','entregado','rechazado','cancelado')),
 creado_en timestamptz not null default now(),
 check (tipo_entrega <> 'despacho' or nullif(trim(direccion_entrega),'') is not null)
);
create table public.detalle_pedido (
 id uuid primary key default gen_random_uuid(),
 pedido_id uuid not null references public.pedidos(id),
 producto_id uuid references public.productos(id),
 descripcion text not null check (length(trim(descripcion)) > 0),
 cantidad integer not null check (cantidad > 0),
 precio_unitario numeric(12,2) check (precio_unitario >= 0),
 sabores text, rellenos text, tematica text, restricciones_alimentarias text,
 creado_en timestamptz not null default now()
);
create table public.cotizaciones (
 id uuid primary key default gen_random_uuid(),
 pedido_id uuid not null references public.pedidos(id),
 version integer not null check (version > 0),
 monto numeric(12,2) not null check (monto >= 0),
 detalle text not null check (length(trim(detalle)) > 0),
 valida_hasta timestamptz,
 estado text not null default 'borrador' check (estado in ('borrador','enviada','aceptada','rechazada','vencida','reemplazada')),
 creado_en timestamptz not null default now(),
 unique (pedido_id,version), unique (id,pedido_id),
 check (valida_hasta is null or valida_hasta >= creado_en)
);
create unique index una_cotizacion_aceptada_por_pedido on public.cotizaciones(pedido_id) where estado='aceptada';
create table public.ventas (
 id uuid primary key default gen_random_uuid(),
 pedido_id uuid not null unique references public.pedidos(id),
 cotizacion_id uuid,
 fecha_venta timestamptz not null default now(),
 total numeric(12,2) not null check (total >= 0),
 estado text not null default 'registrada' check (estado in ('registrada','anulada')),
 creado_en timestamptz not null default now(),
 foreign key (cotizacion_id,pedido_id) references public.cotizaciones(id,pedido_id)
);
create table public.pagos (
 id uuid primary key default gen_random_uuid(),
 venta_id uuid not null references public.ventas(id),
 monto numeric(12,2) not null check (monto > 0),
 tipo text not null default 'cobro' check (tipo in ('cobro','devolucion')),
 metodo text not null check (metodo in ('efectivo','transferencia','otro')),
 referencia text,
 fecha_pago timestamptz not null default now(),
 creado_en timestamptz not null default now()
);
create table public.gastos (
 id uuid primary key default gen_random_uuid(),
 concepto text not null check (length(trim(concepto)) > 0),
 categoria text not null check (categoria in ('insumos','costo_fijo','costo_variable','otro')),
 monto numeric(12,2) not null check (monto > 0),
 fecha_gasto date not null,
 observaciones text,
 creado_en timestamptz not null default now()
);
create table public.historial_pedido (
 id uuid primary key default gen_random_uuid(),
 pedido_id uuid not null references public.pedidos(id),
 estado_anterior text,
 estado_nuevo text not null,
 actor_usuario_id uuid references auth.users(id) on delete set null,
 ocurrido_en timestamptz not null default now()
);

create index pedidos_cliente_idx on public.pedidos(cliente_id);
create index pedidos_fecha_estado_idx on public.pedidos(fecha_entrega,estado);
create index detalle_pedido_pedido_idx on public.detalle_pedido(pedido_id);
create index detalle_pedido_producto_idx on public.detalle_pedido(producto_id);
create index ventas_fecha_idx on public.ventas(fecha_venta);
create index ventas_cotizacion_idx on public.ventas(cotizacion_id,pedido_id);
create index pagos_venta_idx on public.pagos(venta_id);
create index gastos_fecha_idx on public.gastos(fecha_gasto);
create index historial_pedido_fecha_idx on public.historial_pedido(pedido_id,ocurrido_en);
create index historial_actor_idx on public.historial_pedido(actor_usuario_id);

create function public.registrar_historial_pedido() returns trigger
language plpgsql security invoker set search_path = '' as $$
begin
 if TG_OP = 'INSERT' then
  insert into public.historial_pedido(pedido_id,estado_nuevo,actor_usuario_id)
  values (NEW.id,NEW.estado,auth.uid());
 elsif NEW.estado is distinct from OLD.estado then
  insert into public.historial_pedido(pedido_id,estado_anterior,estado_nuevo,actor_usuario_id)
  values (NEW.id,OLD.estado,NEW.estado,auth.uid());
 end if;
 return NEW;
end;
$$;
revoke all on function public.registrar_historial_pedido() from public, anon, authenticated;
create trigger pedidos_historial after insert or update of estado on public.pedidos
for each row execute function public.registrar_historial_pedido();

-- Sin politicas de acceso hasta implementar y probar los roles de la app.
do $$
declare t text;
begin
 foreach t in array array['clientes','productos','disponibilidad','pedidos','detalle_pedido','cotizaciones','ventas','pagos','gastos','historial_pedido'] loop
  execute format('alter table public.%I enable row level security',t);
  execute format('revoke all on table public.%I from public, anon, authenticated',t);
 end loop;
end;
$$;
comment on table public.disponibilidad is 'Capacidad diaria configurable; no confirma cupos por si sola. Requiere operacion transaccional futura.';
comment on table public.pagos is 'Cobros y devoluciones separados de ventas. Reglas de abonos pendientes de validacion.';
comment on table public.historial_pedido is 'Registro automatico de alta y cambios de estado. No sustituye auditoria completa.';
comment on table public.productos is 'Precio base opcional para productos que requieren cotizacion. Importes expresados en CLP en esta version.';
commit;

select tablename, rowsecurity from pg_tables
where schemaname='public'
order by tablename;
