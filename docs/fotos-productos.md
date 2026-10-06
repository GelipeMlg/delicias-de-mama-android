# Fotografías de productos

Implementado el 29 de septiembre de 2026 en el proyecto de D:\dev.

## Uso de la dueña

1. Instalar la nueva compilación e iniciar sesión como dueña.
2. Abrir Productos y elegir Agregar producto o Editar.
3. Pulsar Elegir foto y seleccionar una imagen con el selector del sistema.
4. Revisar la vista previa y guardar. Cambiar foto permite reemplazarla; Quitar foto deja el producto sin imagen al guardar.
5. Desde otro dispositivo, actualizar el catálogo para comprobar la fotografía publicada.

La foto es opcional para pasteles, coctelería salada y cualquier otro producto. Cancelar el selector conserva la selección anterior. La app no solicita acceso general a toda la galería ni permiso de cámara. Los productos sin foto muestran un espacio con el texto Foto por agregar.

## Implementación

- Selector Android PickVisualMedia; imágenes mostradas con Coil y caché de imágenes.
- Reducción a un máximo de 1600 píxeles por lado y conversión a JPEG con calidad 85. Se corrige orientación EXIF y no se conserva la ubicación en el JPEG publicado.
- Bucket público productos en Supabase Storage, límite 5 MiB, tipo image/jpeg. Solo la dueña autenticada puede insertar archivos. Cada nueva foto usa una ruta única producto/archivo.jpg para evitar sobrescribir y mostrar versiones antiguas en caché.
- La tabla productos conserva la URL en imagen_url. fotos_catalogo_app devuelve las fotos visibles; guardar_producto_con_foto_app verifica el rol, la pertenencia del archivo al producto y cambios concurrentes. El guardado del producto y la referencia de foto ocurre en una transacción, reutilizando guardar_cambio_app sin modificarla.
- La subida del archivo precede a la transacción. Si falla el guardado posterior, puede quedar un archivo sin referencia. Reemplazar/quitar una foto no borra el archivo de Storage: se conservan las versiones anteriores; no hay limpieza automática en este incremento.
- Las versiones anteriores de Android conservan su flujo y no muestran las nuevas fotos. Hay que actualizar la aplicación para seleccionar y ver fotografías.

## Despliegue y verificación

La migración supabase/migrations/202609290001_fotos_productos.sql se aplicó al proyecto ivxeabovtxetvvxgyywa. No repetirla: contiene políticas y funciones ya creadas.

La prueba supabase/tests/fotos_productos.sql pasó en Supabase: alta, edición, quitar foto, concurrencia, rechazo de URL ajena, rechazo de usuario sin rol y eliminación. Todo dato de prueba se revierte con ROLLBACK. Esta prueba comprueba referencias y permisos RPC, no la transferencia de bytes desde un teléfono.

Gradle completó testDebugUnitTest (33 pruebas, sin fallos) y assembleDebug. La primera verificación usó .tmp/photos-build.init.gradle; después se corrigió android/build.gradle.kts para que Android Studio y la terminal usen una carpeta estable por checkout, sin scripts adicionales. Se verificó nuevamente la compilación normal. El APK actual está en C:\Users\mlgge\AppData\Local\DeliciasDeMama\android-build\7be90f1952e2\app\outputs\apk\debug\app-debug.apk. La carpeta antigua no se borró.

Tras este cambio hay que sincronizar el proyecto con Gradle en Android Studio para que el IDE reconozca la nueva salida y luego ejecutar Run. La fuente sigue en D:\dev\Delicias de mamá\android.

Pendiente en dispositivo: seleccionar una foto real, guardar, verla desde otra cuenta/celular, reemplazarla y quitarla. También probar cancelar el selector y subir sin conexión. En un error ambiguo de guardado, actualizar antes de reintentar.

Referencias técnicas: https://supabase.com/docs/reference/kotlin/storage-from-upload y https://coil-kt.github.io/coil/compose/.
