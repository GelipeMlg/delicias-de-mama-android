# Propuesta visual de Delicias de Mamá

La lámina [wireframes-delicias-mama.png](wireframes-delicias-mama.png) contiene seis maquetas conceptuales generadas con la herramienta integrada de imágenes, tomando como referencia el logo proporcionado por el equipo. No representa capturas de una aplicación ya implementada.

## Pantallas para revisar con la dueña

1. **Catálogo:** productos y acceso a personalización.
2. **Tu pedido:** fecha, sabor, relleno, porciones y retiro; envío de solicitud de cotización.
3. **Mi pedido:** monto propuesto, aceptación y seguimiento del estado.
4. **Agenda:** capacidad diaria, fechas bloqueadas y encargos.
5. **Cotizaciones:** solicitudes pendientes y preparación de respuestas.
6. **Finanzas:** ventas, gastos y resultado; costos por pedido y gastos generales.

Los productos, precios, nombres, ubicación y fechas de la ilustración son ejemplos ficticios. La dirección que aparece no es una dirección confirmada del negocio. El calendario y los importes son ilustrativos. Los textos secundarios y navegación se ajustarán al validar el diseño; las fotos reales y la información comercial deben ser proporcionadas o aprobadas por la dueña.

## Estilo propuesto

Rosado suave y crema en fondos, café oscuro para textos y verde salvia como acento. Tarjetas redondeadas, formularios simples y logo visible sin competir con las acciones principales. En implementación se comprobarán contraste, tamaño de letra y áreas táctiles.

## Preguntas de validación

- ¿La paleta y el tono representan al negocio?
- ¿Qué información falta para cotizar una torta personalizada?
- ¿Cómo se define la capacidad diaria y cuándo se confirma un cupo?
- ¿Qué estados necesita ver el cliente?
- ¿Qué resumen financiero utiliza la dueña para tomar decisiones?

## Icono Android

La imagen del encabezado se encuentra en `android/app/src/main/res/drawable/logo_delicias_mama.jpg`. Los recursos actuales del icono usan `drawable/ic_launcher_foreground.jpg` y los XML adaptativos en `mipmap-anydpi-v26`, según los ajustes del equipo. El logo conserva sus letras; en tamaños pequeños su legibilidad será menor que la de la ilustración central.

Para verlo, abrir la carpeta `android` en Android Studio y ejecutar **Run app** sobre el emulador. Volver al lanzador del emulador para revisar el icono. Si el lanzador conserva el anterior en caché, reiniciar el emulador. No es necesario desinstalar la app ni perder sus datos.

La interfaz implementada el 20 de septiembre de 2026 retoma la paleta, las tarjetas y la navegación de esta lámina. Incluye gestión de productos, ventas, gastos y cotizaciones; consultar [interfaz, CRUD y agenda](../interfaz-crud-agenda.md). Los ajustes previos de Gradle, dependencias y manifiesto del equipo se conservan.

## Verificación actualizada — 20 de septiembre de 2026

El código de la interfaz compiló y generó un APK debug con JDK 21. Se instaló e inició en Pixel 9a y se revisaron visualmente el catálogo y el resumen financiero con datos existentes de la versión anterior.

Capturas reales de la app:

- [Catálogo](capturas/catalogo.png).
- [Finanzas de la dueña](capturas/finanzas.png).

El emulador tuvo un aviso de que System UI no respondía durante el arranque; después permitió revisar estas pantallas. Esto no sustituye el recorrido completo de aceptación en un teléfono. Consultar [verificación Android](../verificacion-android.md) para las pruebas de negocio y las limitaciones de Gradle/OneDrive.