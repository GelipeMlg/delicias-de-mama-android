-- RF-06: gastos generales y costos atribuibles a un pedido.
-- Ejecutar una sola vez, después de la migración inicial.
begin;
alter table public.gastos
  add column pedido_id uuid,
  add constraint gastos_pedido_id_fkey foreign key (pedido_id)
    references public.pedidos(id) on delete restrict;
create index gastos_pedido_idx on public.gastos(pedido_id)
  where pedido_id is not null;
comment on column public.gastos.pedido_id is
  'Pedido al que se atribuye el gasto. NULL para gastos generales; no repartir el mismo importe completo entre varios pedidos.';
commit;
