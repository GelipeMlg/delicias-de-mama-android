# Interfaz, gestión y agenda — 20 de septiembre de 2026

## Qué se implementó

La aplicación es Android nativa con Kotlin y Jetpack Compose. El equivalente al CSS se encuentra en el tema y los componentes de MainActivity.kt: paleta rosada y crema, texto café, acento salvia, tarjetas redondeadas, logo, barra de navegación inferior y formularios con teclado numérico para importes. Las ilustraciones del catálogo son decorativas; falta incorporar fotografías comerciales reales.

### Vista de la dueña

- **Productos:** crear, consultar, editar nombre, descripción, precio y visibilidad, y eliminar. Un producto con pedidos se retira del catálogo sin borrar su historial. Puede reactivarse desde Editar.
- **Pedidos:** emitir y corregir una cotización. Cambiar una cotización confirmada devuelve el pedido a Cotizado: el cliente debe aceptar el nuevo monto y conserva su cupo. Los pedidos entregados y cancelados no se recotizan.
- **Finanzas:** crear, consultar, editar y eliminar ventas y gastos; modificar concepto, monto, fecha y categoría del gasto. Filtros Hoy, Semana, Mes y Todo. El resumen se recalcula con los movimientos guardados.
- **Agenda:** configurar capacidad por fecha, bloquear fechas, ver pedidos del día y distinguir pendientes de confirmados o entregados.

### Regla de cupos aplicada

Una solicitud activa ocupa **un cupo por pedido**, independientemente de cuántas unidades contenga. Solicitado, Cotizado, Confirmado y Entregado cuentan; Cancelado no cuenta. Cotizar, aceptar y entregar no vuelven a descontar el mismo cupo. Crear una solicitud con la fecha llena o bloqueada se rechaza. La capacidad no puede bajarse por debajo de los pedidos activos.

Esta regla se adoptó para reflejar los encargos pendientes desde que se reciben. Debe validarse con la dueña: una solicitud no expira automáticamente y sigue ocupando cupo hasta cancelarse. Si los datos anteriores ya superan la capacidad, se muestran todos los pedidos; la dueña debe ampliar el límite o cancelar solicitudes antes de aceptar más.

Bloquear una fecha impide nuevas solicitudes y confirmaciones; no cancela sus pedidos existentes.

### Ventas y cotizaciones

Entregar un pedido crea una venta una sola vez. Corregir esa venta en Finanzas ajusta el registro financiero, conservando la cotización histórica. Eliminar la venta no cancela el pedido entregado ni libera su cupo. Las cotizaciones pendientes no se suman a las ventas.

Las ventas manuales permiten registrar ventas de mostrador. Evitar volver a ingresar manualmente una venta que la entrega ya registró. La diferencia mostrada es ventas menos gastos del período, no utilidad contable ni saldo bancario.

## Conservación de los datos

El archivo local sigue llamándose demo-v1.json para encontrar los datos anteriores, pero ahora guarda formato versión 2. Al abrir un archivo anterior se conserva su información, se recuperan los nombres del catálogo y se convierten las entregas antiguas en registros de ventas. Una lista de ventas vacía guardada en versión 2 permanece vacía; no se recrean ventas eliminadas.

El guardado usa AtomicFile y la interfaz se actualiza después de escribir correctamente. Si falla la lectura, se bloquean los cambios para no sobrescribir datos. No hace falta desinstalar la app para actualizarla.

## Cómo abrir y probar en Android Studio

1. Abrir la carpeta **android** de este proyecto.
2. Usar el JDK 21 instalado como Gradle JDK y sincronizar Gradle.
3. Elegir el emulador o teléfono y ejecutar **Run app**.
4. Activar **Cambiar vista** para entrar a la vista de la dueña.
5. En Productos, agregar un producto, editarlo y comprobarlo en el catálogo del cliente.
6. En Agenda, seleccionar una fecha futura sin pedidos, fijar capacidad 1 y guardar.
7. Desde el cliente, solicitar un producto para esa fecha. Comprobar en Agenda: 1 ocupado y 0 disponibles.
8. Cotizar como dueña y aceptar como cliente. Debe seguir ocupado solo 1 cupo. Corregir el monto como dueña y verificar que vuelve a requerir aceptación.
9. Cancelar el pedido y comprobar que vuelve a quedar disponible el cupo. Con otro pedido aceptado, marcar Entregado y comprobar su venta automática.
10. En Finanzas, crear, editar y eliminar un gasto y una venta manual. Revisar los totales y usar Todo para encontrar fechas fuera del período actual.
11. Cerrar y abrir la aplicación para comprobar la persistencia. No usar Desinstalar ni Borrar datos.

## Archivos principales

| Archivo dentro de android/app/src | Responsabilidad |
| --- | --- |
| main/java/cl/deliciasdemama/app/MainActivity.kt | Tema, navegación, pantallas y formularios |
| main/java/cl/deliciasdemama/app/Models.kt | Productos, pedidos, movimientos y reglas de cupos/CRUD |
| main/java/cl/deliciasdemama/app/LocalRepository.kt | Lectura, migración y guardado local |
| main/java/cl/deliciasdemama/app/ShopViewModel.kt | Estado de pantalla, guardado y mensajes |
| test/java/cl/deliciasdemama/app/ShopRulesTest.kt | Pruebas de reglas del negocio |

## Alcance para la siguiente etapa

Esta entrega sigue siendo una demostración local. El selector de vistas no es autenticación ni control de acceso real. Supabase aún debe integrarse con inicio de sesión, permisos de la dueña y transacciones que garanticen cupos entre dispositivos.

Antes de conectar estos cambios, alinear el SQL con las ventas manuales (sin pedido), la retirada de productos y el historial de correcciones financieras. Definir con la dueña si se requiere registro de auditoría y cómo gestionar solicitudes pendientes. No se modificaron tablas ni datos de Supabase en esta entrega.