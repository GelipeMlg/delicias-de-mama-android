# Verificación Android

## Actualización del 20 de septiembre de 2026

### Resultados observados

- El código Compose compiló con el wrapper Gradle 8.11.1, JDK 21 y Android SDK 35. Se generó el APK debug en una carpeta temporal de compilación separada, evitando el directorio app/build bloqueado de la instalación previa.
- Las clases compiladas por Gradle de ShopRulesTest se ejecutaron directamente con JUnit 4.13.2: **OK (12 tests)**.
- El APK se instaló correctamente y se inició en el emulador Pixel 9a. Ver la sección de alcance de la revisión visual al final.

### Cobertura de las 12 pruebas

1. La primera solicitud ocupa el último cupo y se rechaza una segunda.
2. Cancelar un pedido confirmado libera capacidad.
3. Bloquear la fecha impide aceptar una cotización.
4. Entregar un pedido dos veces se rechaza.
5. La capacidad no puede reducirse por debajo de pedidos confirmados.
6. Se rechazan gastos negativos y solicitudes con fecha pasada.
7. Corregir una cotización confirmada exige nueva aceptación y mantiene un solo cupo.
8. Cancelar una solicitud pendiente libera su cupo; su capacidad tampoco puede ignorarse.
9. Crear, editar, retirar y reactivar productos conserva nombres históricos y disponibilidad.
10. Editar un gasto reemplaza importe y fecha sin duplicarlo; puede eliminarse.
11. Corregir o eliminar una venta de pedido mantiene la cotización cerrada y la ocupación.
12. Ventas manuales: creación, edición, eliminación y rechazo de importes, vínculos y fechas inválidas.

Estas pruebas verifican reglas locales, no concurrencia entre usuarios ni políticas de Supabase.

### Limitación del ejecutor Gradle en este equipo

testDebugUnitTest compiló las pruebas, pero su ejecutor falló inicialmente con ClassNotFoundException para ShopRulesTest. Un segundo intento encontró un acceso denegado al directorio de resultados binarios dentro de OneDrive. La ejecución directa de las mismas clases con JUnit pasó las 12 pruebas. No se informa testDebugUnitTest como una tarea aprobada.

Para evitar los bloqueos de archivos, conviene probar una copia del proyecto en una carpeta local fuera de OneDrive y sin acentos. No se movió el proyecto del usuario ni se borraron sus directorios de compilación.

### Validaciones que siguen pendientes

- Persistencia completa tras guardar todas las operaciones de CRUD y reiniciar; la lectura y conversión de ventas antiguas sí se comprobó en el emulador.
- Recorrido manual completo del CRUD, teclado, rotación y tamaños de letra grandes.
- Android API 26 y otras resoluciones.
- Autenticación, autorización, sincronización y concurrencia con Supabase.

Seguir el recorrido de prueba en [interfaz, CRUD y agenda](interfaz-crud-agenda.md). El informe del 16 de septiembre cubría seis pruebas aisladas y no incluía un APK; esta actualización agrega las pruebas de edición y la compilación completa.
## Revisión visual

Se revisaron el catálogo y Finanzas en Pixel 9a. El archivo existente tenía versión 1, dos pedidos entregados por un total de $42.000 y gastos por $23.222. La pantalla nueva mostró ambas sumas y su diferencia de $18.778, comprobando la lectura de datos anteriores y la conversión de sus ventas en memoria.

Las capturas se guardaron en docs/diseno/capturas. Durante el arranque hubo lentitud y un aviso de System UI sin respuesta; después se pudo navegar. No se considera una prueba de rendimiento ni un recorrido completo de aceptación.
También se creó un gasto de prueba por $1.000 desde el formulario real. Se comprobó en el archivo guardado: versión 2, tres productos, dos ventas migradas y los dos gastos anteriores más el nuevo gasto. La sesión del emulador se ejecutó con -read-only y -no-snapshot-save, por lo que estas operaciones de prueba no reemplazan los datos persistentes del AVD original.

Tras guardar, se forzó el cierre del proceso y se inició nuevamente: el catálogo cargó sin error de lectura con el archivo versión 2.
