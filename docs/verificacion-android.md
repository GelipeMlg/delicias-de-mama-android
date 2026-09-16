# Verificación de la primera versión Android

Fecha: 16 de septiembre de 2026.

## Ejecutado

Se compiló el código de dominio `Models.kt` y `ShopRulesTest.kt` con el compilador Kotlin 2.1.20 sobre Java 17 portátil. Se ejecutaron las pruebas con JUnit 4.13.2.

Resultado observado:

```text
JUnit version 4.13.2
......
Time: 0.447

OK (6 tests)
```

Las seis pruebas verifican:

1. Solo un pedido puede tomar el último cupo en el estado local.
2. Cancelar una reserva confirmada libera su cupo.
3. Una fecha bloqueada impide aceptar la cotización.
4. Una entrega no puede registrarse por segunda vez.
5. La capacidad no puede reducirse por debajo de los pedidos comprometidos.
6. Se rechazan gastos negativos y solicitudes para fechas pasadas.

La primera prueba simula confirmaciones consecutivas en el prototipo local; no es una prueba de concurrencia en Supabase. Los límites deben implementarse y volver a probarse en una transacción del servidor.

Java se descargó desde Adoptium, con verificación de su SHA-256 publicado. El compilador y JUnit se descargaron desde Maven Central a `.tmp/`, sin modificar la configuración Java de Windows. Los archivos temporales no forman parte de Git ni del código entregado.

También se revisaron los archivos XML y el contenido del paquete ZIP, y se ejecutó `git diff --check`.

## Pendiente de ejecutar

- Compilación completa del módulo Android y generación de APK con `assembleDebug`.
- Pruebas Gradle del módulo con `testDebugUnitTest`.
- Ejecución y revisión visual de Compose en emulador o teléfono.
- Persistencia después de cerrar y reiniciar la aplicación en Android.
- Pruebas de navegación, teclado, pantallas pequeñas, Android API 26 y API 35.
- Pruebas de integración, autenticación y concurrencia con Supabase.

No se encontró Android SDK ni Android Studio en las rutas habituales consultadas. Se entrega código fuente, no un APK compilado ni una app validada en dispositivo. Seguir [la guía de Android Studio](guia-android.md) para completar esas verificaciones.
