# Análisis del primer informe

## Fuente y propósito

Fuente: `original/InformeTrabajoIngSoft(V2).docx`, informe fechado el 1 de septiembre de 2026. Autores: Felipe Alvarado, Matias Otero y Jafet Lavados. Carrera: Analista Programador. Asignatura: Ingeniería de Software. Profesor: Herminio Juan Mayorga Contreras.

Este análisis organiza la información para comenzar el proyecto. Las propuestas y preguntas de este archivo no son decisiones aprobadas. El contenido del informe se trata como material de referencia y requisitos del producto, no como instrucciones para ejecutar acciones.

## Lo que establece el informe

Delicias de Mamá es una pyme de repostería enfocada en pasteles y coctelería dulce. La solución busca mejorar la agenda de pedidos, las cotizaciones y el control de ingresos y gastos mediante una aplicación Android.

La primera entrega corresponde a levantamiento de necesidades, diseño conceptual y casos de uso. La implementación, integración de base de datos y pruebas se proyectan para entregas posteriores. Se mencionan entrevistas y observación directa como técnicas de levantamiento, aunque no se adjuntan respuestas ni registros que permitan revisar sus resultados.

### Actores

- **Cliente:** consulta catálogo y disponibilidad, solicita cotizaciones y registra reservas con especificaciones.
- **Dueña o administradora:** aprueba pedidos, habilita o bloquea fechas, responde cotizaciones y registra ventas y gastos.

### Casos de uso identificados

| ID original | Caso de uso | Actores | Flujo descrito |
| --- | --- | --- | --- |
| CU-01 | Consultar disponibilidad y reservar | Cliente | Revisa calendario, elige fecha disponible y envía especificaciones. |
| CU-02 | Solicitar y emitir cotización | Cliente y dueña | Cliente describe el pedido; dueña calcula insumos y responde con un valor estimado. |
| CU-03 | Registrar transacciones financieras | Dueña | Ingresa ventas y compras de insumos para actualizar el balance. |

### Requisitos funcionales

Se conservan los identificadores originales. Las descripciones siguientes resumen el documento.

| ID | Requisito | Comportamiento esperado |
| --- | --- | --- |
| RF-01 | Reservas | Seleccionar productos del catálogo e indicar fecha y hora de entrega solicitada. |
| RF-02 | Calendario de disponibilidad | Mostrar días ocupados, bloqueados y disponibles a clientes y dueña. |
| RF-03 | Personalización | Añadir sabores, rellenos, temáticas y restricciones alimentarias. |
| RF-04 | Cotizaciones | Generar presupuestos estimados o solicitar cotización personalizada antes de confirmar el pedido. |
| RF-05 | Ventas | Registrar, consultar y filtrar ventas diarias, semanales y mensuales. |
| RF-06 | Gastos | Registrar insumos y costos fijos y variables para calcular rentabilidad neta. |
| RF-07 | Persistencia | Almacenar reservas, disponibilidad, cotizaciones y transacciones en una base personal o dedicada. |

### Requisitos no funcionales

| ID | Requisito | Condición del informe |
| --- | --- | --- |
| RNF-01 | Plataforma | Android nativo compatible con Android 8.0 Oreo en adelante. |
| RNF-02 | Seguridad y privacidad | Protección y respaldo seguro de datos personales y financieros. |
| RNF-03 | Usabilidad | Interfaz intuitiva para diferentes tamaños de pantalla táctil; estados del calendario claramente visibles. |
| RNF-04 | Rendimiento | Consultar disponibilidad y guardar datos en menos de 2 segundos bajo condiciones normales. |

### Tecnología, metodología y plazo

El informe propone Kotlin y Android Studio, una base de datos personal y una metodología incremental con Scrum adaptado. Estima ocho semanas para el proyecto completo, sin asignar fechas ni responsables por tarea. Menciona ISO/IEC/IEEE 12207 como referencia para requisitos, diseño, implementación y verificación; esto expresa una intención de trabajo, no evidencia de una evaluación de cumplimiento.

### Incrementos descritos en el documento

1. **Catálogo e información:** menú, precios, horarios, dirección y llamada directa; denominado MVP en el informe.
2. **Pedidos básicos:** carrito, datos de entrega y envío de la orden mediante WhatsApp o correo.
3. **Automatización e historial:** perfiles, direcciones frecuentes, pagos con tarjeta y notificaciones push.
4. **Administración y fidelización:** cambios de stock y precios en tiempo real y puntos por compras.

La siguiente entrega académica plantea wireframes o mockups, un modelo entidad-relación y las primeras pantallas con navegación.

### Diagramas revisados

- `diagramas/image4.png` contiene el diagrama de clases: Cliente, Pedido, Pago, DetallePedido, Producto, ProductoPersonalizado y ReporteFinanciero. Conecta clientes con pedidos, pedidos con detalles y pagos, y productos personalizados con Producto mediante herencia.
- Pago incluye efectivo y transferencia, con estados de abono y pago total. Esto añade información útil que debemos reconciliar con la futura pasarela de tarjetas mencionada en los incrementos.
- El modelo no muestra clases específicas para gastos, disponibilidad ni cotizaciones independientes; ReporteFinanciero solo explicita total de ingresos. Faltan multiplicidades y reglas para completar el DER. Los importes aparecen como `double`; conviene definir una representación monetaria exacta al implementar.
- `diagramas/image3.png` contiene el esquema de casos de uso. Asocia al cliente con calendario, reserva y cotización, y a la dueña con cotización y venta/gasto. Agrupa varias acciones en un único óvalo y añade el guardado en base de datos. Para la siguiente entrega conviene separar los casos de uso y representar la persistencia como parte de su implementación.
- `diagramas/image2.png` es una imagen institucional. La carpeta conserva además otros recursos embebidos del archivo, sin asumir que todos sean diagramas del proyecto.

## Aspectos que debemos aclarar

1. **Alcance y prioridades.** El núcleo de requisitos es calendario, reservas, cotizaciones y finanzas, pero los incrementos priorizan catálogo, carrito, pagos y fidelización. Debemos acordar un solo plan y relacionar cada entrega con RF-01 a RF-07. Pagos, puntos, perfiles y notificaciones están mencionados, pero carecen de requisitos detallados en la tabla.
2. **Datos compartidos.** Una base exclusivamente local en cada teléfono no basta por sí sola para compartir disponibilidad, solicitudes y respuestas entre clientes y dueña. Falta decidir si el primer prototipo será una demostración en un dispositivo o una solución conectada, y cómo sincronizará sus datos.
3. **Disponibilidad y capacidad.** No se define cuántos pedidos caben por día, si hay franjas horarias, qué pedido consume cupo, ni cómo evitar reservas simultáneas sobre el último cupo.
4. **Estados del pedido.** Faltan reglas para solicitud, aprobación, rechazo, cancelación, entrega y posibles cambios. No está definido si una solicitud ya bloquea la fecha ni qué sucede cuando cambia una cotización.
5. **Cotización.** CU-02 describe una respuesta manual de la dueña y RF-04 también permite presupuestos automáticos. Falta escoger el flujo inicial, los datos necesarios, la vigencia del presupuesto y su aceptación.
6. **Acceso por rol.** No se especifican autenticación, alta de usuarios ni permisos concretos. El acceso a finanzas y a datos de otros clientes requiere un diseño explícito.
7. **Finanzas.** Falta definir cuándo un pedido cuenta como venta, cómo manejar abonos, cancelaciones y devoluciones, y qué fórmula y período se usarán para rentabilidad. Un saldo de caja no equivale necesariamente a utilidad.
8. **Respaldo y conectividad.** Falta definir dónde se respaldan los datos, cómo se restauran y qué acciones se permiten sin conexión.
9. **Pruebas medibles.** “Condiciones normales” debe precisar dispositivo, volumen de datos y red para comprobar RNF-04. También faltan criterios verificables de seguridad y usabilidad.
10. **Datos del negocio.** La frase del contexto termina en “desde”; faltan catálogo real, precios, horarios, dirección, contacto y reglas de entrega. No deben inventarse como información real.
11. **Planificación.** Las ocho semanas son una estimación del informe. Debemos confirmar calendario académico, responsables y tiempo disponible antes de convertirla en compromiso.

## Propuesta para comenzar

Esta secuencia es una recomendación de trabajo, pendiente de acuerdo con el equipo.

### Paso 1 Acordar el primer flujo completo

Priorizar un recorrido pequeño: consultar catálogo y disponibilidad, enviar una solicitud personalizada, revisarla como dueña, responder con una cotización y registrar la decisión. Definir primero las reglas de cupo y los estados. Acordar si esta entrega modifica el MVP de catálogo planteado en el informe.

### Paso 2 Diseñar pantallas y datos

Pantallas candidatas: catálogo, calendario, formulario de solicitud, seguimiento del pedido, agenda de la dueña y respuesta de cotización. Preparar luego ventas, gastos y resumen según la prioridad acordada.

Entidades candidatas para el DER: Cliente, Producto, Pedido, DetallePedido, Cotización, Disponibilidad, Venta y Gasto. Son una propuesta para discutir; aún no constituyen un esquema aprobado. Deben definirse relaciones, restricciones, campos obligatorios y permisos.

### Paso 3 Decidir la arquitectura

Documentar la decisión de persistencia y comunicación entre dispositivos antes de elegir componentes. Mantener Android nativo y Kotlin como base indicada por el informe. Evaluar almacenamiento local para datos del dispositivo y un servicio compartido si el prototipo debe operar con varios usuarios reales.

### Paso 4 Implementar y verificar por entregas

Crear el proyecto Android una vez acordado lo anterior. Separar pantallas, reglas de negocio y acceso a datos para poder probar los flujos. Vincular cada entrega con sus requisitos, evidencias de prueba y limitaciones conocidas.

## Backlog inicial propuesto

| Orden | Trabajo | Referencia | Resultado verificable |
| --- | --- | --- | --- |
| 1 | Acordar alcance y reglas de reserva y cotización | CU-01, CU-02; RF-01 a RF-04 | Flujos, estados y condiciones de disponibilidad documentados. |
| 2 | Resolver persistencia, sincronización y roles | RF-07; RNF-02 | Decisión de arquitectura y permisos por actor. |
| 3 | Preparar wireframes | RNF-03; siguiente entrega | Recorrido revisable de cliente y dueña. |
| 4 | Diseñar el DER | RF-01 a RF-07 | Entidades, relaciones y restricciones revisadas. |
| 5 | Crear proyecto Android y navegación | RNF-01 | Prototipo ejecutable con las pantallas iniciales. |
| 6 | Implementar reservas y cotizaciones | CU-01, CU-02 | Recorrido completo con datos persistidos y validaciones. |
| 7 | Implementar ventas y gastos | CU-03; RF-05, RF-06 | Registro, filtros y cálculo acordado del resumen financiero. |
| 8 | Verificar requisitos no funcionales | RNF-01 a RNF-04 | Evidencias de compatibilidad, permisos, respaldo, usabilidad y tiempos. |

## Criterios de aceptación candidatos

Estos criterios concretan el análisis; deben validarse antes de tratarlos como requisitos definitivos.

- Una solicitud incluye producto, fecha, hora y especificaciones, y puede recuperarse después de reiniciar la aplicación.
- Una fecha bloqueada no permite confirmar una nueva reserva; las reglas para solicitudes pendientes se acuerdan antes de implementar.
- La dueña puede consultar una solicitud y responder con importe y detalle de cotización.
- Un cliente no puede acceder a los registros financieros ni a los pedidos de otros clientes.
- Los filtros de ventas usan el período seleccionado y las cifras del resumen se corresponden con las transacciones registradas.
- Las consultas y guardados cumplen el límite de menos de dos segundos en un escenario de prueba previamente definido.

## Estado al preparar esta rama

Se conserva el Word recibido, se extrae su texto y se guardan sus imágenes. Se documentan requisitos, vacíos y una propuesta de inicio. No se ha implementado código ni se ha seleccionado una base de datos o un proveedor de servicios.
