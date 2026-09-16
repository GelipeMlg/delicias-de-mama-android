# Delicias de Mamá para Android

Primera versión de demostración en Kotlin y Jetpack Compose. Abrir esta carpeta en Android Studio mediante **Open**; no crear otro proyecto ni copiar archivos uno por uno.

## Qué permite probar

- Catálogo de ejemplo y personalización de solicitudes.
- Calendario mensual, bloqueo de fechas y capacidad diaria.
- Solicitar, cotizar, confirmar, cancelar y marcar un pedido como entregado.
- Registrar gastos y consultar ventas de prueba por día, semana o mes.
- Conservar los datos de prueba al cerrar la aplicación, en un archivo privado del dispositivo.

El selector de vista cliente/dueña es para demostrar el flujo en un solo teléfono. **No es autenticación ni control de permisos.** No utilizar datos personales ni financieros reales. El APK se identifica como `cl.deliciasdemama.app.demo` y no tiene permiso de Internet. No realiza solicitudes a Supabase.

Los tres productos y sus precios son ficticios. La capacidad de tres pedidos por día y el registro de venta al marcar entregado son reglas de demostración, pendientes de validar con la dueña. La app permite retiro; el despacho del esquema SQL se implementará después.

## Abrir y ejecutar

Consultar [la guía paso a paso](../docs/guia-android.md).

Con JDK y Android SDK configurados, desde esta carpeta:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

El APK se genera en `app/build/outputs/apk/debug/app-debug.apk`. Este archivo solo existirá después de una compilación satisfactoria.

## Estructura del código

| Archivo | Responsabilidad |
| --- | --- |
| app/src/main/java/cl/deliciasdemama/app/MainActivity.kt | Tema, navegación y pantallas Compose |
| app/src/main/java/cl/deliciasdemama/app/Models.kt | Modelos y reglas del prototipo |
| app/src/main/java/cl/deliciasdemama/app/LocalRepository.kt | Persistencia local mediante escritura atómica |
| app/src/main/java/cl/deliciasdemama/app/ShopViewModel.kt | Estado de pantalla, validación y guardado fuera del hilo principal |
| app/src/test/java/cl/deliciasdemama/app/ShopRulesTest.kt | Pruebas del flujo, cupos y cantidades |

`ShopRepository` separa la interfaz del almacenamiento. La versión conectada necesita operaciones remotas específicas, autenticación, gestión de sesiones, paginación y políticas RLS; no basta con cambiar la URL o subir este archivo JSON a Supabase.

## Versiones fijadas

Estado de verificación: pasaron seis pruebas de reglas en Kotlin/JVM. La compilación Android, el APK y la revisión visual en dispositivo quedan pendientes del SDK. Ver [evidencias y límites](../docs/verificacion-android.md).

AGP 8.9.2, Gradle 8.11.1, Kotlin y compilador Compose 2.1.20, Compose BOM 2025.04.01, compileSdk/targetSdk 35, minSdk 26. Son una base reproducible elegida para el prototipo, no una afirmación de que sean las últimas versiones. Revalidar dependencias y políticas de publicación antes de una entrega en tienda.

El wrapper Gradle procede del repositorio oficial Gradle v8.11.1. Su distribución ZIP tiene SHA-256 fijado. No se incluye ninguna contraseña, clave administrativa ni SDK dentro del proyecto.
