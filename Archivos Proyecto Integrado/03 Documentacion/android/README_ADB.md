# Instalar el APK con ADB

Trabajar en `D:\dev\Delicias de mamá\android` con JDK 21 y Android SDK 35. Las salidas se guardan dentro del proyecto.

```powershell
.\gradlew.bat :app:assembleDebug
adb devices
adb install -r ".\app\build\outputs\apk\debug\app-debug.apk"
adb shell am start -n cl.deliciasdemama.app.demo/cl.deliciasdemama.app.MainActivity
```

Activar Depuración USB en el teléfono, conectar un cable de datos y aceptar la autorización. Si aparece `unauthorized`, desbloquearlo y aceptar el diálogo. Si `adb` no está en PATH, ejecutar `adb.exe` desde `platform-tools` del SDK configurado en Android Studio.

Para varios dispositivos, añadir `-s SERIAL` después de `adb`, usando el identificador mostrado por `adb devices`.

Ante un fallo de archivos bloqueados, cerrar una compilación en curso y ejecutar ` .\gradlew.bat --stop` antes de reintentar. No eliminar código ni reutilizar las rutas antiguas de compilación en AppData. Si hay conflicto de firma, revisar qué APK estaba instalado antes de desinstalar: desinstalar elimina los datos y sesiones locales.
