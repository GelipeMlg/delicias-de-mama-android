# Firebase y notificaciones: estado de integración

Actualizado: 28 de septiembre de 2026.

## Proyecto activo

La fuente de trabajo está en `D:\dev\Delicias de mamá`. Abrir su carpeta `android` en Android Studio.

- Firebase: `deliciasdemama-da46b`.
- Aplicación Android: `cl.deliciasdemama.app.demo`.
- Configuración local: `android/app/google-services.json`, comprobada contra el paquete.
- Cloud Messaging HTTP V1 habilitado en Firebase.
- El plugin Google Services está condicionado a la presencia del archivo; la tarea `processDebugGoogleServices` se ejecutó correctamente el 27/09/2026.

## Flujo implementado

1. Android autentica al usuario con Supabase y registra el token FCM mediante `registrar_dispositivo_app`.
2. Las operaciones de pedidos se guardan por RPC en Supabase y generan notificaciones y trabajos de envío.
3. La función `supabase/functions/enviar-notificaciones/index.ts` reclama trabajos y envía mensajes de datos mediante FCM HTTP V1.
4. Android verifica el destinatario, evita duplicados y muestra una notificación en el canal Pedidos y cotizaciones. Al pulsarla abre los pedidos.

La función está desplegada y dispone de FCM_SERVICE_ACCOUNT y PUSH_CRON_SECRET. El 28/09 se detectó que Cron no tenía trabajos: había cuatro dispositivos y cuatro avisos pendientes, sin intentos de envío. Se creó enviar-notificaciones-cada-minuto para ejecutar la función cada minuto.

## Configuración del servidor

- Cuenta supabase-notificaciones con el rol personalizado deliciasFcmSender y el permiso cloudmessaging.messages.create.
- JSON privado en el secreto FCM_SERVICE_ACCOUNT de Supabase.
- PUSH_CRON_SECRET coincide con push_cron_secret de Vault; la función valida la cabecera x-push-secret.
- La función mantiene Verify JWT with legacy secret activado. Cron envía Authorization: Bearer usando el JWT público anon guardado como push_anon_jwt en Vault. Una clave sb_publishable_ no sustituye ese JWT.
- Cron ejecuta enviar-notificaciones-cada-minuto con pg_net y espera hasta 10 segundos por respuesta. Consultar net._http_response para verificar el HTTP y el cuerpo: el estado succeeded de Cron por sí solo no confirma el envío FCM.

Las claves privadas de cuentas de servicio no deben incorporarse a Git, al APK ni a documentos del proyecto. No confundirlas con google-services.json, que configura el cliente Android.

## Resultado comprobado del servidor

El 28/09/2026, la primera ejecución automática obtuvo HTTP 200 y el cuerpo `{"sent":2,"failed":0}` en `net._http_response`. Firebase aceptó dos mensajes. Esto confirma que el servidor ejecuta el envío sin depender de tener la aplicación abierta; no demuestra por sí solo que los teléfonos mostraron las notificaciones.

Esta corrección modifica la configuración del servidor. No requiere sincronizar Gradle ni reinstalar una aplicación que ya incluya la integración FCM.

## Validación final pendiente en dos dispositivos

Instalar el APK actualizado en ambos teléfonos y conceder el permiso de notificaciones. Usar cuentas distintas de cliente y dueña. Seleccionar mantener sesión para recibir avisos después de cerrar la app.

1. Cliente crea un pedido; comprobarlo en Supabase y en la cuenta de la dueña.
2. Dueña envía una cotización; cliente recibe un aviso con la app en segundo plano.
3. Cliente acepta; dueña recibe el cambio y su aviso.
4. Abrir una notificación y comprobar que lleva a pedidos y que no aparece duplicada.
5. Cerrar sesión y comprobar que no se muestran avisos de la cuenta anterior.

Cerrar la app normalmente y forzar su detención desde Ajustes son casos diferentes; documentar ambos resultados en la prueba real. Aún no hay evidencia de una prueba completa de entrega FCM entre dos teléfonos.

## Confirmación posterior del usuario

El usuario confirmó después de esta revisión que las notificaciones en segundo plano funcionan. Conservar la lista de pruebas anterior como recorrido de regresión: esa confirmación no documenta por sí sola todos los casos de dos dispositivos, duplicados y cierre de sesión.
