# Estado actual y continuidad

Revisión: 6 de octubre de 2026. Equipo: Felipe Alvarado, Jafet Lavados y Matias Otero.

Fuente activa: `D:\dev\Delicias de mamá`. Abrir `android` en Android Studio. No usar OneDrive. El proyecto integrado comprende Android, Supabase, Firebase y documentación académica; otras aplicaciones de asignaturas están fuera del alcance.

## Implementación comprobada en archivos

- Auth y autorización de dueña mediante `es_duena`, sesión opcional persistente y almacenamiento de sesión protegido.
- RPC de catálogo, pedidos, disponibilidad y finanzas; escucha de cambios remotos.
- Productos con fotografías, modo de pastel, sabores, rellenos y porciones.
- FCM y función `enviar-notificaciones`; el usuario confirmó funcionamiento en segundo plano.
- Último incremento: filtros, progreso y revisión de cotización con control de revisión antes de aceptar.
- Corrección del ejecutor Gradle con `file.encoding=COMPAT` para Windows.

## Evidencia y límites

Los informes guardados del 1 de octubre registran 39 pruebas sin fallos y un APK debug. La revisión de archivos por sí sola no acredita el estado remoto. Las pruebas unitarias no comprueban la entrega FCM ni todo el circuito real en dos dispositivos. Registrar resultados de una prueba de cliente/dueña para cada entrega.

No están implementados los estados preparación/listo ni los abonos. No declarar completas funciones solo propuestas. Los documentos antiguos y entregables se conservan como evidencia histórica.

## Continuar

1. Mantener código y documentación juntos en Git; revisar cambios antes de cada commit.
2. Probar cliente y dueña con cuentas diferentes: crear pedido, cotizar, confirmar, cancelar y entregar; comprobar cupos y permisos.
3. Probar fotos y notificaciones sin duplicados, con app en segundo plano y cambio/cierre de sesión.
4. Usar nuevas migraciones para cambios de base. No reaplicar a ciegas las migraciones a la base activa.

## Verificación de respaldo (6 de octubre de 2026)

Se ejecutó `:app:testDebugUnitTest :app:assembleDebug`: BUILD SUCCESSFUL, 43 tareas UP-TO-DATE. Gradle reutilizó los resultados existentes: 39 pruebas, cero fallos y cero errores. No se ejecutó una prueba nueva en dispositivos ni se modificó la base remota. Persisten advertencias de opciones obsoletas de AGP; no impiden esta compilación.
