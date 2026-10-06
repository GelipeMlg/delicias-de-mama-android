# Proyecto Supabase

## Actualización de implementación

El 16 de septiembre de 2026 se crearon diez tablas con RLS y se ejecutaron pruebas de integridad con reversión de datos. Consultar [Base de datos inicial](base-de-datos.md) para esquema, evidencias y límites. El estado descrito a continuación corresponde a la revisión previa a esa creación.

## Estado verificado

El 16 de septiembre de 2026 se revisó el proyecto desde el panel abierto por el usuario.

- Nombre: Delicias de mamá.
- Referencia: `ivxeabovtxetvvxgyywa`.
- Panel: https://supabase.com/dashboard/project/ivxeabovtxetvvxgyywa
- URL del servicio: https://ivxeabovtxetvvxgyywa.supabase.co
- Plan de la organización mostrado: Free.
- Estado mostrado: Healthy.
- Región: East US (North Virginia), `us-east-1`.
- El esquema `public` no contiene tablas ni vistas.
- El panel no muestra migraciones, respaldos ni repositorio GitHub conectado.

La revisión fue de lectura. No se modificaron ajustes, permisos ni datos remotos. La cuenta y el proyecto ya existen, pero la aplicación todavía no está conectada. No se han recopilado claves ni contraseñas.

## Diseño inicial para preparar la base

Esta es una propuesta de estructura, no un esquema desplegado ni una confirmación de reglas que no aparecen en la entrevista disponible.

| Entidad propuesta | Propósito | Requisito |
| --- | --- | --- |
| Perfiles | Identificar al cliente y relacionarlo con su usuario de autenticación | Actores y RNF-02 |
| Productos | Catálogo de pasteles y coctelería | RF-01 |
| Disponibilidad | Fechas bloqueadas y capacidad de recepción de pedidos | RF-02 |
| Pedidos y detalles | Fecha y hora solicitadas, productos y personalización | RF-01, RF-03 |
| Cotizaciones | Importe propuesto por la dueña y respuesta del cliente | RF-04 |
| Ventas y pagos | Registro de venta y cobros separados para evitar confundir abonos con ventas nuevas | RF-05 |
| Gastos | Compras de insumos y otros gastos | RF-06 |

RF-07 aplica al conjunto. La asignación del rol administrador debe quedar fuera de los campos que el cliente puede editar.

## Preparación de la conexión Android

La app usará la URL del servicio y una clave publicable del proyecto. La clave publicable identifica el proyecto; los permisos efectivos deben limitarse mediante autenticación y políticas RLS. Las credenciales administrativas y la contraseña de PostgreSQL no corresponden a la configuración del APK.

Preparar primero una migración SQL versionada con restricciones, RLS y pruebas de permisos. Diseñar las operaciones de reserva para comprobar el cupo de manera transaccional en el servidor. La app solo debe mostrar una reserva como confirmada después de una respuesta satisfactoria del servidor.

Antes de fijar el esquema, resolver capacidad diaria, momento de confirmación, vigencia de cotizaciones y tratamiento de abonos. No asignar valores supuestamente reales a estas reglas sin validarlas con la dueña.

## Siguiente entrega

DER y migración inicial revisables, seguidos de una prueba de conexión Android. La existencia de este documento no implica que esas tareas ya se hayan ejecutado.
