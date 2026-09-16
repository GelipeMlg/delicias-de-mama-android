-- Pruebas de integridad sin conservar datos ficticios.
begin;
do $$
declare c uuid; p uuid; q uuid; v uuid; t text; r text; op text;
begin
 insert into public.clientes(nombre) values ('PRUEBA TEMPORAL') returning id into c;
 insert into public.disponibilidad(fecha,capacidad_pedidos) values ('2099-01-01',2);
 insert into public.pedidos(cliente_id,fecha_entrega,hora_entrega,tipo_entrega)
 values(c,'2099-01-01','12:00','retiro') returning id into p;
 if (select count(*) from public.historial_pedido where pedido_id=p) <> 1 then raise exception 'Fallo historial de alta'; end if;
 update public.pedidos set estado='cotizado' where id=p;
 if (select count(*) from public.historial_pedido where pedido_id=p) <> 2 then raise exception 'Fallo historial de cambio'; end if;
 insert into public.detalle_pedido(pedido_id,descripcion,cantidad) values(p,'Torta personalizada',1);
 insert into public.cotizaciones(pedido_id,version,monto,detalle,estado)
 values(p,1,20000,'Cotizacion de prueba','aceptada') returning id into q;
 insert into public.ventas(pedido_id,cotizacion_id,total) values(p,q,20000) returning id into v;
 insert into public.pagos(venta_id,monto,metodo) values(v,5000,'transferencia');
 begin
  insert into public.detalle_pedido(pedido_id,descripcion,cantidad) values(p,'Cantidad invalida',0);
  raise exception 'Acepto cantidad cero';
 exception when check_violation then null; end;
 begin
  insert into public.gastos(concepto,categoria,monto,fecha_gasto) values('Prueba','insumos',-1,'2099-01-01');
  raise exception 'Acepto gasto negativo';
 exception when check_violation then null; end;
 begin
  insert into public.pedidos(cliente_id,fecha_entrega,hora_entrega,tipo_entrega) values(c,'2099-01-01','12:00','despacho');
  raise exception 'Acepto despacho sin direccion';
 exception when check_violation then null; end;
 begin
  insert into public.ventas(pedido_id,total) values(p,20000);
  raise exception 'Acepto segunda venta del pedido';
 exception when unique_violation then null; end;
 begin
  insert into public.cotizaciones(pedido_id,version,monto,detalle,estado) values(p,2,21000,'Duplicada','aceptada');
  raise exception 'Acepto dos cotizaciones aceptadas';
 exception when unique_violation then null; end;
 begin
  insert into public.pagos(venta_id,monto,metodo) values(gen_random_uuid(),100,'efectivo');
  raise exception 'Acepto pago sin venta';
 exception when foreign_key_violation then null; end;
 foreach t in array array['clientes','productos','disponibilidad','pedidos','detalle_pedido','cotizaciones','ventas','pagos','gastos','historial_pedido'] loop
  if not (select rowsecurity from pg_tables where schemaname='public' and tablename=t) then raise exception 'Falta RLS en %',t; end if;
  foreach r in array array['anon','authenticated'] loop
   foreach op in array array['SELECT','INSERT','UPDATE','DELETE'] loop
    if has_table_privilege(r,'public.'||t,op) then raise exception 'Permiso inesperado % % %',r,t,op; end if;
   end loop;
  end loop;
 end loop;
end;
$$;
rollback;
select 'OK: flujo, historial, restricciones y permisos; datos de prueba revertidos' as resultado;
