# Ejecución desde Android Studio — 29/09/2026

Proyecto: D:\dev\Delicias de mamá\android.

El error «Error loading build artifacts» se produjo después de una compilación correcta del IDE. Se comprobó la existencia de redirect.txt, sus metadatos JSON y el APK. También se ejecutó el lector GenericBuiltArtifactsLoader de la instalación local de Android Studio sobre ese mismo redirect: devolvió el APK y sus metadatos correctamente. Por ello, regenerar el APK desde la terminal no demuestra que el despliegue del IDE esté resuelto.

Se añadió la configuración compartida android/.run/Delicias_de_Mama.run.xml, llamada «Delicias de Mama», con el módulo DeliciasDeMama.app, despliegue APK, actividad predeterminada y compilación Gradle antes de ejecutar. No borra los datos de la aplicación. Las configuraciones antiguas no se eliminaron.

Seleccionar «Delicias de Mama» junto a Run. Si no aparece, cerrar y abrir el proyecto para cargar la configuración compartida. Está pendiente confirmar el resultado del botón Run con esta configuración; no se atribuye una causa definitiva al archivo redirect ni se editan manualmente sus rutas generadas.

Verificación en dispositivo: instalación mediante adb install -r -t terminada con Success en el Samsung conectado. La opción -t es necesaria para el APK generado por el IDE, marcado como testOnly. El lanzamiento de MainActivity mediante am start -W devolvió Status: ok, LaunchState: COLD. Esto verifica instalación e inicio, no el despliegue desde el botón Run.
