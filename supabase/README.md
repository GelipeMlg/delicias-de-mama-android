# Base de datos y SQL

Rama de trabajo: **`codex/base-de-datos`**.

## Archivos

- [Migración inicial](migrations/202609160001_base_inicial.sql): diez tablas, índices, restricciones e historial. Ya ejecutada manualmente en Supabase.
- [Gastos por pedido](migrations/202609180001_gastos_por_pedido.sql): relación opcional de gastos con pedidos, aplicada manualmente el 18 de septiembre de 2026.
- [Pruebas de gastos por pedido](tests/gastos_por_pedido.sql): gastos generales, vínculo válido, rechazo de pedido inexistente y RLS; revierte los datos ficticios.
- [Pruebas de integridad](tests/base_inicial.sql): datos ficticios dentro de una transacción que termina en `ROLLBACK`.
- [Documentación del esquema](../docs/base-de-datos.md): relaciones, resultados y límites.
- [Datos del proyecto Supabase](../docs/supabase.md): referencia del servicio y estado.

## Reglas de organización

1. Nuevos cambios de estructura o permisos van en archivos nuevos dentro de `migrations/`, con nombre `AAAAMMDDHHMM_descripcion.sql`.
2. Las pruebas SQL van en `tests/` y deben indicar qué validan y cómo manejan sus datos de prueba.
3. Las explicaciones funcionales y diagramas van en `docs/` y se actualizan junto con el SQL.
4. No modificar una migración ya aplicada para representar un cambio posterior. No repetir la migración inicial sobre las tablas existentes.

La prueba inicial usa la fecha `2099-01-01`. Antes de ejecutarla en otro entorno, comprobar que no exista allí; termina con reversión y no es una herramienta para borrar datos. Si se interrumpe una ejecución dentro de una sesión SQL, comprobar el estado transaccional antes de seguir.

Todavía no hay configuración de Supabase CLI ni historial de migraciones CLI sincronizado con la ejecución manual. Cambiar ramas de Git no proporciona aislamiento de la base remota. Las tablas siguen cerradas a la app; las políticas operativas y la confirmación concurrente están pendientes.

Ver [guía de trabajo con ramas](../CONTRIBUTING.md).
