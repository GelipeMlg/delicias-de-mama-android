-- Ejecutar después de ambas migraciones. Todos los datos se revierten.
begin;
do $$
declare
  cliente uuid;
  pedido uuid;
  gasto uuid;
  fecha_prueba date;
begin
  select d::date into fecha_prueba
  from generate_series(date '2098-01-01', date '2098-12-31', interval '1 day') d
  where not exists (select 1 from public.disponibilidad where fecha = d::date)
  limit 1;
  if fecha_prueba is null then raise exception 'No hay fecha libre de prueba'; end if;
  insert into public.clientes(nombre) values ('PRUEBA gasto pedido') returning id into cliente;
  insert into public.disponibilidad(fecha,capacidad_pedidos) values (fecha_prueba,1);
  insert into public.pedidos(cliente_id,fecha_entrega,hora_entrega,tipo_entrega)
    values(cliente,fecha_prueba,'12:00','retiro') returning id into pedido;
  insert into public.gastos(concepto,categoria,monto,fecha_gasto,pedido_id)
    values('PRUEBA costo encargo','insumos',1000,fecha_prueba,pedido) returning id into gasto;
  insert into public.gastos(concepto,categoria,monto,fecha_gasto)
    values('PRUEBA gasto general','costo_fijo',2000,fecha_prueba);
  begin
    update public.gastos set pedido_id=gen_random_uuid() where id=gasto;
    raise exception 'Se aceptó un pedido inexistente';
  exception when foreign_key_violation then null;
  end;
  if not exists(select 1 from public.gastos where id=gasto and pedido_id=pedido) then
    raise exception 'Se perdió el vínculo válido';
  end if;
  if not exists(select 1 from pg_constraint where conrelid='public.gastos'::regclass
      and conname='gastos_pedido_id_fkey' and confdeltype='r' and convalidated) then
    raise exception 'Falta restricción validada de borrado';
  end if;
  if not (select relrowsecurity from pg_class where oid='public.gastos'::regclass) then
    raise exception 'RLS desactivada';
  end if;
end $$;
rollback;
select 'OK: gasto general y por pedido; FK y RLS verificadas; datos revertidos' as resultado;
