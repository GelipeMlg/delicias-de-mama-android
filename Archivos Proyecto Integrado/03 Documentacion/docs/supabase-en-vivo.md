# Primera conexión de Android con Supabase

## Alcance de este incremento

El inicio de sesión utiliza Supabase Auth. El permiso de dueña se consulta en
el servidor; conocer el correo no concede acceso. El catálogo se lee de
`public.productos` y sus altas, ediciones y bajas se guardan mediante una
función que verifica el rol. Retirar un producto lo deja inactivo para
conservar sus referencias. Los productos inactivos solo aparecen a la dueña.

Agenda, pedidos, cotizaciones y finanzas permanecen locales. La aplicación
lo indica en su cabecera. No se suben automáticamente datos ficticios del
emulador. La disponibilidad todavía no representa cupos compartidos entre
teléfonos. Este incremento permite validar conexión y catálogo, no una
operación comercial multiusuario completa.

## Activación del servidor

En Supabase > SQL Editor, ejecutar en este orden:

1. `supabase/migrations/202609220001_acceso_duena.sql`.
2. `supabase/migrations/202609230001_catalogo_conectado.sql`.
3. `supabase/migrations/202609260001_productos_sabores.sql` para añadir
   sabores opcionales y la eliminación protegida.
4. `supabase/admin/asignar_duena.sql`, después de verificar que el correo
   corresponde a la cuenta administrativa confirmada.
5. `supabase/tests/acceso_duena.sql` y
   `supabase/tests/catalogo_conectado.sql`.

No ejecutar nuevamente la migración inicial de tablas en una base existente.
No desactivar RLS ni colocar claves `service_role` en Android.

## Ejecutar desde Android Studio

1. Abrir la carpeta `android` del proyecto y sincronizar Gradle.
2. Elegir el emulador y ejecutar el módulo `app` con Run.
3. Pulsar **Actualizar desde Supabase**. Un catálogo vacío significa que
   todavía no hay productos activos en la base; no se carga un catálogo de
   demostración como sustituto de una conexión fallida.
4. Entrar en **Acceso de la dueña** con la cuenta confirmada y su contraseña
   actual de Supabase. El código no contiene ni cambia esa contraseña.
5. Crear un producto de prueba desde **Productos**. Consultar la fila en
   Supabase > Table Editor > productos para comprobar que se guardó.
6. Editar precio y descripción. Pulsar Actualizar desde Supabase en otra
   instalación para ver el cambio. La actualización es manual, no Realtime.
7. Retirar el producto: debe permanecer en la tabla con `activo=false` y
   dejar de mostrarse en el catálogo público.
8. Cerrar sesión y comprobar que desaparecen los controles administrativos.
   Una cuenta de cliente debe ser rechazada en el acceso de la dueña.

Si dos dispositivos editan la misma versión, el servidor rechaza la segunda
edición. Actualizar antes de reintentar. Si se pierde la conexión después de
enviar una escritura, la app no afirma que falló: obliga a actualizar para
comprobar si se aplicó y evitar duplicados.

## Archivos y decisiones

- `AuthRepository.kt`: cliente Supabase compartido entre login y catálogo.
- `ConnectedRepository.kt`: lectura y escritura remota de productos, con
  almacenamiento local conservado para los módulos pendientes.
- `CatalogChanges.kt`: detecta la operación concreta; evita reemplazar toda
  la tabla por la copia de un dispositivo.
- `ShopViewModel.kt`: actualización, límites de espera, sesión y mensajes.
- SQL versionado: funciones con permisos mínimos y verificación del rol
  dentro del servidor. La tabla privada de dueñas no es accesible a clientes.

El precio nulo del esquema inicial se presenta como cero en este primer
catálogo. La interfaz trabaja en pesos CLP enteros; no ingresar fracciones.

## Productos y sabores

En el editor de la dueña, marca **Permitir selección de sabores** solo para
productos que necesiten esa elección. Escribe un sabor por línea; el cliente
podrá marcar varios. Para empanadas pequeñas, coctelería salada u otro
producto, deja la casilla desmarcada y no se exige sabor.

La tabla `public.productos` guarda `sabores_habilitados` y `sabores` junto con
el producto. Al eliminar, la función intenta borrar el registro si no hay
referencias; si existen pedidos o detalles, realiza una baja lógica
(`activo=false`) para mantener el historial. La pantalla oculta retirados por
defecto y ofrece **Mostrar productos retirados** para revisarlos.

## Siguiente incremento

Conectar clientes y pedidos con políticas de acceso propias; después,
confirmar cupos en una transacción del servidor que bloquee la fecha durante
la reserva. Incorporar cotizaciones versionadas y movimientos financieros
cuando sus reglas y pruebas estén listas. Evitar sincronizar un archivo
local completo: cada operación debe validar identidad, permisos y estado.

## Verificación del 26 de septiembre de 2026

La compilación pasó y las 31 pruebas unitarias terminaron sin fallos.
El diagnóstico SQL confirmó una cuenta administrativa con correo confirmado
y detectó que faltaba `es_duena()`. La llamada HTTP con la clave publicable
alcanzó PostgREST y devolvió `PGRST202` para `catalogo_app` antes de desplegar
las funciones. Después se aplicó la migración del catálogo y una prueba
transaccional confirmó crear, leer, editar, eliminar y conservar como retirado
un producto con historial. La prueba hizo `rollback`, por lo que no dejó
datos sintéticos en la base.
