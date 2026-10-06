# Autenticación y acceso de la dueña

## Estado

El código Android incorpora inicio de sesión con Supabase Auth y comprobación
del rol mediante `public.es_duena()`. No contiene contraseñas ni claves de
servicio. La compilación y 25 pruebas unitarias finalizaron correctamente.
Quedan pendientes la activación SQL, el alta de la cuenta y las pruebas reales
de acceso con una cuenta de cliente y otra de dueña.

## Configuración administrativa

1. Ejecutar `supabase/migrations/202609220001_acceso_duena.sql` en SQL Editor.
2. En Authentication > Users, crear la cuenta solicitada para la dueña.
   Introducir personalmente la contraseña en Supabase; no guardarla en este
   repositorio. El panel exige como mínimo 6 caracteres; el registro Android
   exige 12 y admite hasta 72 bytes UTF-8. Usar 12 o más para ambos recorridos.
3. Confirmar la cuenta y revisar el correo en
   `supabase/admin/asignar_duena.sql` antes de ejecutarlo como administrador.
   Este script concede el rol a la cuenta indicada; no cambia su contraseña.
4. Ejecutar `supabase/tests/acceso_duena.sql` y verificar el inicio de sesión
   en Android. Un cliente debe ser rechazado en «Acceso de la dueña».
5. Comprobar que cerrar sesión elimina la vista administrativa y que sin
   conexión no se permiten modificaciones que requieren validación remota.

No basta con escribir el correo de la dueña: se exige una contraseña válida,
una cuenta confirmada y una asignación administrativa asociada a su UUID.
El registro de clientes no puede conceder ese rol.

## Contraseñas y sesiones

Supabase Auth calcula y verifica hashes bcrypt con sal en el servidor.
El hash es unidireccional; no es cifrado reversible. Android envía las
credenciales mediante HTTPS y no vuelve a aplicar bcrypt localmente.
Los tokens permanecen en memoria y la app cierra la sesión a los 15 minutos.
Cada modificación revalida la identidad y el rol con el servidor.

## Alcance del MVP

El código conecta productos con Supabase; su activación SQL está pendiente.
Pedidos, agenda y finanzas continúan en almacenamiento local.
El control de interfaz y las reglas de acceso del MVP no sustituyen las
políticas RLS de una futura persistencia comercial en Supabase. Antes de
producción faltan sincronización y autorización de los módulos pendientes, y pruebas
de concurrencia y recuperación. La revocación del rol se detecta al verificar
una modificación o al iniciar una sesión nueva; las lecturas de una sesión
ya abierta pueden conservar el rol hasta su cierre.

Referencia: https://supabase.com/docs/guides/auth/password-security

La guía `supabase-en-vivo.md` describe las migraciones adicionales y la prueba
del catálogo conectado desde Android Studio.
