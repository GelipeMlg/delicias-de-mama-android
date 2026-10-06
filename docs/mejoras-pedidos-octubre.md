# Seguimiento y revisión de cotizaciones

Incremento Android del 1 de octubre de 2026.

## Cambios

- Pedidos divididos en Pendientes, Historial y Todos, ordenados por fecha y hora.
- Contadores de pedidos abiertos y cotizaciones pendientes de aceptación.
- Progreso con los estados reales existentes: solicitado, cotizado, confirmado y entregado.
  Los cancelados muestran un mensaje independiente, sin simular avance.
- Mensaje que explica el siguiente paso y contacto visible para la dueña.
- Resumen antes de aceptar: cantidad, personas, fecha, hora, sabor, relleno, temática,
  restricciones y precio total. Se aclara que aceptar no realiza un cobro.
- Si cambia el estado, monto o revisión mientras el cliente revisa la cotización,
  se bloquea la aceptación y se solicita revisar el pedido actualizado.
- Confirmación antes de cancelar para evitar pulsaciones accidentales.
- Acciones deshabilitadas durante guardado, actualización o recuperación de un error.

No requiere migración SQL. Se utiliza el campo revision y la comprobación de concurrencia
que ya implementa guardar_cambio_app en Supabase. No se incorporan estados de preparación
ni pagos hasta desarrollar su persistencia y sus reglas completas.

## Prueba manual entre dispositivos

1. Cliente solicita un pedido; dueña lo cotiza.
2. Cliente abre Revisar y aceptar y comprueba el detalle y total.
3. Con la ventana abierta, la dueña cambia el monto. Tras sincronizar, la ventana debe impedir
   aceptar la versión anterior. Cerrar y volver a abrir muestra el monto vigente.
4. Aceptar la cotización actual debe confirmar el pedido y conservar el cupo.
5. Abrir Cancelar pedido y elegir Conservar pedido no debe cambiar datos.
6. Cancelar definitivamente debe moverlo al historial y liberar el cupo.
7. Entregar un pedido confirmado debe mostrar el último paso y aparecer en el historial.

Las pruebas unitarias OrderPresentationTest verifican la aceptación de la versión revisada,
el rechazo de cambios de monto/revisión/estado/identidad y la separación del historial.
La prueba visual y entre dispositivos debe realizarse instalando el APK actualizado.

## Verificación realizada

- Compilación `:app:assembleDebug` completada correctamente.
- 39 pruebas aprobadas con JUnit 4.13.2 ejecutado directamente sobre las clases
  compiladas, incluidas las seis nuevas pruebas de revisión de cotización.
- Fallo del ejecutor Gradle corregido el 1 de octubre de 2026: el archivo de argumentos
  se escribía en UTF-8 pero el lanzador Java lo leía en Windows-1252. La tilde de
  `Delicias de mamá` producía una ruta incorrecta y ClassNotFoundException.
  Se comprobó reproduciendo el fallo con el mismo archivo y ejecutándolo después
  con una copia codificada en Windows-1252: las seis pruebas nuevas pasaron.
- Ajuste definitivo en `android/gradle.properties`: `-Dfile.encoding=COMPAT` para
  que Gradle escriba los archivos de argumentos con la codificación nativa.
  Se conserva la carpeta del proyecto y las salidas estándar de compilación.
- Verificación final con `gradlew.bat :app:testDebugUnitTest :app:assembleDebug`:
  BUILD SUCCESSFUL, pruebas ejecutadas por Gradle y APK generado correctamente.
- APK generado en `android/app/build/outputs/apk/debug/app-debug.apk`.
- No se realizó todavía la prueba visual ni el flujo en dos teléfonos.
