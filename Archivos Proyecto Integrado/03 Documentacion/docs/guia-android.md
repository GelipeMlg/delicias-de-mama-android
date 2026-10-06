# Guía para abrir y desarrollar la aplicación

## Resultado de esta entrega

Se entrega el proyecto fuente `android/`, una demostración local para revisar el flujo con el equipo y la dueña. La base Supabase ya existe, pero esta aplicación todavía no se conecta a ella. El desarrollo restante está detallado en [Plan de implementación](plan-implementacion.md).

## 1 Programa que debes utilizar

Usa **Android Studio**, el entorno oficial de desarrollo Android. Desde ahí se edita Kotlin, se ejecutan las pruebas y se genera el APK. Supabase se administra desde el navegador y almacena los datos compartidos de la versión conectada.

Descarga Android Studio desde [la página oficial](https://developer.android.com/studio). La [guía de instalación](https://developer.android.com/studio/install) describe el asistente y los requisitos del equipo. Un teléfono físico puede sustituir al emulador si tu computador tiene pocos recursos.

## 2 Instalar y preparar Android Studio

1. Ejecuta el instalador oficial de Android Studio para Windows.
2. Completa el asistente de instalación y acepta personalmente las licencias que correspondan.
3. En el primer inicio, usa la configuración estándar y deja que descargue el SDK.
4. Abre **SDK Manager** desde More Actions en la bienvenida, o desde Tools si tienes un proyecto abierto.
5. En **SDK Platforms**, instala **Android 15 / API 35** para compilar esta versión.
6. En **SDK Tools**, comprueba Android SDK Platform-Tools y Android SDK Build-Tools 35.0.0. Instala Android Emulator si usarás un dispositivo virtual.
7. Usa el JDK incluido con Android Studio si es compatible, o un JDK 17. En Settings busca **Gradle JDK**. Gradle 8.11.1 puede ejecutarse con JDK 17 o 21; este proyecto compila el código con objetivo Java 17. No sirve un JRE para sustituir todas las herramientas del JDK.

Los nombres o la ubicación de algunos menús pueden variar con la versión de Android Studio.

## 3 Abrir el código entregado

1. En Android Studio pulsa **Open**.
2. Selecciona la carpeta:

   `C:\Users\mlgge\OneDrive\Documentos\ChatGPT\Delicias de mamá\android`

3. Si aparece una confirmación de confianza del proyecto, verifica que sea esta carpeta y ábrelo.
4. Espera la sincronización de Gradle. La primera vez descargará las dependencias desde Google, Maven Central y Gradle.
5. Si aparece **SDK location not found**, selecciona el SDK instalado desde Android Studio. La ruta se guarda localmente en `local.properties`, que no se incorpora a Git.
6. Conserva inicialmente las versiones fijadas en los archivos Gradle para que todos los integrantes prueben la misma base.

Si OneDrive bloquea archivos durante la compilación, copia el repositorio completo a una ruta de desarrollo local, por ejemplo `C:\Proyectos\DeliciasMama`, y abre su carpeta `android`. Conserva una sola copia de trabajo para evitar cambios divergentes.

## 4 Ejecutar en un emulador

1. Abre **Tools → Device Manager**.
2. Crea un dispositivo virtual de teléfono y elige una imagen Android API 35.
3. Inicia el emulador y espera a que muestre la pantalla principal de Android.
4. Selecciona el módulo **app** y el dispositivo en la barra superior.
5. Pulsa **Run ▶**. Debería abrirse **Delicias de Mamá · Demo** después de compilar.
6. Para validar RNF-01, repite luego las pruebas en un emulador o teléfono con Android 8.0/API 26; ejecutar solo en API 35 no demuestra compatibilidad con API 26.

## 5 Ejecutar en un teléfono Android

1. Habilita las opciones de desarrollador y la depuración USB en tu teléfono.
2. Conéctalo al computador mediante un cable de datos.
3. Autoriza la depuración desde el teléfono cuando aparezca el aviso.
4. Selecciona ese dispositivo en Android Studio y pulsa **Run ▶**.

## 6 Probar el recorrido completo de la demostración

1. En **Catálogo**, elige un producto y pulsa Personalizar pedido.
2. Ve a **Agenda**, selecciona una fecha futura y pulsa Solicitar para esta fecha.
3. Completa nombre y contacto ficticios, cantidad, hora, sabor, relleno, temática y restricciones. Envía la solicitud.
4. Activa **Vista de la dueña**. En Pedidos, ingresa una cotización total sin separadores, por ejemplo `25000`.
5. Vuelve a la vista del cliente y acepta la cotización. La demo verifica los cupos del día.
6. En la vista de la dueña, marca el pedido como entregado. Esta regla de demostración registra una venta por ese importe.
7. En **Finanzas**, registra un gasto ficticio y cambia entre Hoy, Semana y Mes.
8. Cierra y vuelve a abrir la app. Los pedidos y gastos deben conservarse en ese dispositivo.
9. Prueba un día bloqueado y una fecha con capacidad de un pedido; intenta confirmar una segunda solicitud para comprobar el rechazo por falta de cupo.

Las solicitudes pendientes no consumen cupo. La cancelación libera un cupo confirmado. Bloquear una fecha impide nuevas confirmaciones y no cancela compromisos anteriores. Son reglas de prueba, no acuerdos ya obtenidos en la entrevista.

## 7 Dónde editar el código

En el panel Project, abre `app/src/main/java/cl/deliciasdemama/app`:

- `MainActivity.kt`: textos, colores, pantallas, formularios y navegación.
- `Models.kt`: modelos y reglas de negocio. Aquí está el catálogo ficticio.
- `LocalRepository.kt`: lectura y guardado local en `demo-v1.json` usando AtomicFile.
- `ShopViewModel.kt`: carga, mensajes, guardado y manejo de errores.
- `app/src/test/.../ShopRulesTest.kt`: pruebas automáticas de reglas.

El guardado se ejecuta fuera del hilo de pantalla y el estado solo se actualiza después de guardarlo. Si el archivo local no puede leerse, la app no lo sobrescribe con datos vacíos.

## 8 Pruebas y generación de APK

Abre la terminal de Android Studio, sitúate en `android/` y ejecuta:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

Después de una compilación satisfactoria encontrarás el APK en:

`android/app/build/outputs/apk/debug/app-debug.apk`

El informe de pruebas de Gradle estará en `android/app/build/reports/tests/testDebugUnitTest/index.html`. Conserva evidencias de la ejecución real para el proyecto integrado. Las pruebas de reglas no reemplazan la revisión visual ni las pruebas en dispositivos.

## 9 Conectar Supabase en la siguiente etapa

1. Definir la tabla privada o mecanismo de roles y la asignación controlada de la dueña.
2. Crear y probar políticas RLS: cliente ve sus pedidos; dueña gestiona la operación; ningún cliente se convierte en administrador.
3. Crear una operación transaccional para pedido y detalle, y otra para confirmar cotización y reservar el cupo.
4. Implementar un repositorio remoto con el cliente Kotlin de Supabase, autenticación, cierre de sesión, renovación de sesión y manejo de errores.
5. Configurar la URL `https://ivxeabovtxetvvxgyywa.supabase.co` y una clave publicable. Las claves administrativas y la contraseña de PostgreSQL nunca van en el APK.
6. Sustituir el selector de vistas de la demo por el rol autorizado por el servidor.
7. Probar con dos cuentas y dos dispositivos, incluyendo acceso denegado a pedidos ajenos y confirmaciones simultáneas.

No desactives RLS para que una pantalla consiga leer datos. Ahora mismo las tablas están cerradas a los clientes de la API de forma intencional. Tampoco debe copiarse el archivo local como mecanismo de sincronización.

## Referencias técnicas

- [Android Studio e instalación](https://developer.android.com/studio/install).
- [Compatibilidad AGP 8.9 y Gradle](https://developer.android.com/build/releases/agp-8-9-0-release-notes).
- [Java compatible con Gradle](https://docs.gradle.org/8.11.1/userguide/compatibility.html).
- [Arquitectura Android](https://developer.android.com/topic/architecture/recommendations).
- [Supabase con Kotlin](https://supabase.com/docs/guides/getting-started/quickstarts/kotlin).
- [Seguridad RLS](https://supabase.com/docs/guides/database/postgres/row-level-security).

Consulta: 16 de septiembre de 2026.
