# Delicias de Mamá

Proyecto Integrado TI de INACAP: aplicación Android para gestionar catálogo, pedidos, cotizaciones, agenda y finanzas de la PYME Delicias de Mamá.

**Equipo:** Felipe Alvarado, Jafet Lavados y Matias Otero. Metodología incremental.

## Estado actual (6 de octubre de 2026)

La aplicación Kotlin/Jetpack Compose está conectada a Supabase Auth, PostgreSQL, Realtime y Storage. Firebase Cloud Messaging permite avisos de pedidos y cotizaciones. El usuario confirmó notificaciones en segundo plano e imágenes en vivo; eso no sustituye una prueba completa de regresión en dos teléfonos.

- Acceso de cliente y dueña, verificación del rol en servidor y sesión persistente opcional.
- Catálogo con CRUD, fotos, modo de pastel y opciones de sabores, rellenos y porciones. El cliente elige un sabor y un relleno.
- Pedidos compartidos, cotizaciones, confirmación, cancelación, agenda y finanzas.
- Filtros de pedidos, seguimiento y validación de la cotización revisada antes de aceptarla.
- Estados actuales: solicitado, cotizado, confirmado, entregado y cancelado. Preparación, listo para retiro y pagos/abonos no están implementados.

## Organización

| Carpeta | Contenido |
| --- | --- |
| [android/](android/README.md) | Código, recursos y pruebas; abrir esta carpeta en Android Studio |
| [supabase/](supabase/README.md) | Migraciones, pruebas SQL y función de notificaciones |
| [docs/](docs/README.md) | Requisitos, informes, diseños, guías y evidencias |
| [scripts/](scripts/README.md) | Generadores de documentación |

La copia activa en este PC es `D:\dev\Delicias de mamá`. No trabajar con copias antiguas de OneDrive. `main` contiene la versión integrada; las ramas de área son puntos de trabajo históricos y deben actualizarse desde `main` antes de reutilizarlas.

## Empezar

1. Leer [configuración Android](android/README.md).
2. Revisar [respaldo y recuperación](docs/respaldo-github.md) y [colaboración](CONTRIBUTING.md).
3. Consultar [último incremento](docs/mejoras-pedidos-octubre.md) y [estado del proyecto](docs/estado-actual.md).

Los informes y ZIP de entregas anteriores conservan su fecha y no describen necesariamente el código actual. GitHub guarda código y documentación; no respalda automáticamente datos, usuarios, imágenes ni secretos de Supabase/Firebase.
