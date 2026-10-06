# Android: Delicias de Mamá

Aplicación conectada a Supabase y Firebase, escrita en Kotlin con Jetpack Compose y Material 3.

## Abrir y ejecutar

1. Abrir `D:\dev\Delicias de mamá\android` en Android Studio (o la carpeta `android` de una nueva clonación).
2. Usar JDK 21 para Gradle e instalar Android SDK 35. La configuración de compilación genera bytecode Java 17.
3. Dejar que Android Studio configure `local.properties` con el SDK local. Este archivo no se sube a Git.
4. Recuperar `app/google-services.json` desde Firebase, proyecto `deliciasdemama-da46b`, aplicación `cl.deliciasdemama.app.demo`. En este PC ya existe; se mantiene fuera del repositorio. No usar un JSON de cuenta de servicio en su lugar.
5. Sincronizar Gradle, seleccionar el módulo `app` y un teléfono o emulador Android 8.0/API 26 o superior, y ejecutar Run.
6. Iniciar sesión con una cuenta real de Supabase. La cuenta de dueña necesita el rol del servidor; registrar una cuenta normal no concede ese rol.

La conexión usa la URL y clave publicable del cliente en `AuthRepository.kt`. Esta clave no concede permisos administrativos: la autorización depende de Auth, RLS y las RPC. Las contraseñas se verifican en Supabase Auth. Nunca introducir una clave `service_role` en Android.

## Pruebas y APK

Desde PowerShell, dentro de `android` y con Java 21 disponible:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.
Informes: `app/build/reports/tests/testDebugUnitTest/index.html`.

Conservar `-Dfile.encoding=COMPAT` en `gradle.properties`: corrige el lanzamiento del ejecutor de pruebas en Windows con la tilde de la ruta. No utilizar scripts antiguos que redirigen las salidas de compilación a AppData.

Sin `google-services.json`, Gradle permite compilar pero la integración FCM no queda configurada. Para probar notificaciones, instalar con ese archivo, conceder permiso y activar mantener sesión. Ver [Firebase](../docs/firebase-notificaciones.md).

Ver [instalación ADB](README_ADB.md), [estado actual](../docs/estado-actual.md) y [pruebas del último incremento](../docs/mejoras-pedidos-octubre.md).
