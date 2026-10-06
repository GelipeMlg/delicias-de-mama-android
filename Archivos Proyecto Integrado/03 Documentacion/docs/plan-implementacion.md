# Plan de implementación de Delicias de Mamá

## Punto de partida

Tenemos diez tablas creadas en Supabase y un prototipo Android local para revisar pantallas y flujo. El 16 de septiembre de 2026 se reorganizó el Schema Visualizer mediante distribución automática y ajustes manuales. Las diez tablas de negocio quedan separadas y visibles; se verificó que la distribución se conserva al recargar. La referencia externa a auth.users puede requerir desplazar el lienzo. No se cambiaron tablas ni relaciones para mejorar el dibujo.

La entrega final debe conectar la app con Supabase y cubrir RF-01 a RF-07 y RNF-01 a RNF-04. La demo no representa ese cumplimiento final.

## Orden de trabajo y entregables

| Etapa | Trabajo | Criterio de término | Documentación |
| --- | --- | --- | --- |
| 1 | Validar reglas y pantallas con la dueña | Reglas de cupos, confirmación, abonos, entrega y cancelación acordadas | Acta breve de validación y requisitos actualizados |
| 2 | Autenticación y permisos Supabase | Dos clientes aislados entre sí; permisos de dueña verificables | Matriz de permisos, migración y pruebas RLS |
| 3 | Catálogo y disponibilidad conectados | Datos compartidos visibles en dos dispositivos; estados de carga/error | Pantallas, diccionario de datos y evidencias RF-01/RF-02 |
| 4 | Solicitudes y cotizaciones | Pedido con detalles guardado de manera atómica; respuesta y aceptación trazables | Casos de uso, secuencia de estados y pruebas RF-03/RF-04 |
| 5 | Confirmación, ventas, cobros y gastos | Sin sobreventa de cupos ni registros de venta duplicados; saldos y devoluciones consistentes | Reglas financieras, pruebas y evidencia RF-05/RF-06 |
| 6 | Calidad y entrega | Pruebas en Android 8 y actual; restauración y tiempos medidos | Informe de pruebas, manual de usuario y manual técnico |

La estimación original de ocho semanas deberá ajustarse según disponibilidad del equipo y validaciones. Cada etapa termina con una evidencia demostrable; no asignamos fechas ni responsables sin acordarlos.

## Matriz de requisitos y brechas

| ID | Estado en la demo local | Trabajo para la app conectada |
| --- | --- | --- |
| RF-01 | Catálogo ficticio y solicitudes de retiro con fecha/hora | Catálogo real, despacho si corresponde, persistencia transaccional pedido/detalles |
| RF-02 | Calendario, capacidad configurable y bloqueo local | Consulta compartida, protección de observaciones internas y confirmación concurrente en servidor |
| RF-03 | Sabores, rellenos, temática y restricciones | Enviar y recuperar campos desde detalle_pedido; validar tamaños y contenido |
| RF-04 | Cotización simple emitida y aceptada | Versiones, vigencia, rechazo y cambios autorizados con trazabilidad |
| RF-05 | Venta de prueba al entregar; filtro diario, semanal y mensual | Regla comercial validada, ventas separadas de pagos, anulaciones y devoluciones |
| RF-06 | Gasto de hoy y diferencia simple | Categorías completas, períodos y fórmula financiera acordada |
| RF-07 | Archivo privado del dispositivo; base remota creada por separado | Persistencia Supabase, respaldo, restauración y estrategia de trabajo sin conexión |
| RNF-01 | minSdk 26 configurado | Compilar y ejecutar en Android 8.0 y otra versión reciente |
| RNF-02 | Sin red, solo demo; tablas remotas cerradas | Auth, permisos efectivos, privacidad y restauración de respaldo |
| RNF-03 | Pantallas Compose con textos en español | Pruebas con dueña, tamaños de pantalla, texto ampliado y TalkBack |
| RNF-04 | Escritura local fuera del hilo principal | Medir consultas y guardados reales menores a 2 s con red, volumen y dispositivo definidos |

## Reglas que debemos validar antes de programar el servidor

- ¿Un cupo corresponde a un pedido, una torta o una cantidad de trabajo? ¿Depende del tipo de producto?
- ¿Qué anticipación exige cada producto y qué horarios de entrega están disponibles?
- ¿Se confirma al aceptar la cotización, pagar un abono o recibir aprobación de la dueña?
- ¿Se permite retiro, despacho o ambos? ¿Quién calcula el costo de despacho?
- ¿Cuánto dura una cotización y quién puede modificarla?
- ¿Qué abonos, cancelaciones y devoluciones maneja el negocio?
- ¿Cuándo se reconoce la venta y qué indicadores necesita la dueña?
- ¿Se registrarán ventas directas sin pedido previo? El esquema inicial exige un pedido.

## Diseño del acceso a datos

La app dibuja pantallas con Compose. Los ViewModels mantienen el estado y coordinan repositorios. El prototipo tiene `ShopRepository` y una implementación local; la versión conectada deberá definir operaciones de red independientes para cada flujo, con paginación y respuestas de error.

El servidor será la autoridad para permisos, estados, precios aceptados y cupos. La validación local ayuda al usuario, pero no sustituye la validación remota. Las solicitudes y detalles se guardarán juntos o no se guardará ninguno. La confirmación del último cupo deberá bloquear o actualizar de forma atómica la disponibilidad y rechazar una segunda confirmación concurrente.

## Cambios pendientes en la base

1. Roles de cliente y dueña sin asignación libre desde el teléfono.
2. Políticas RLS de lectura y escritura, con pruebas por operación y actor.
3. Una vista o función que exponga disponibilidad al cliente sin revelar observacion_interna.
4. Funciones para crear pedido con detalles y aceptar cotización con control de cupo.
5. Transiciones de estado autorizadas e historial compatible con los permisos del servidor. El disparador actual no eleva privilegios; revisar su interacción con las nuevas políticas.
6. Reglas que vinculen el total de venta a la cotización aceptada y limiten cobros y devoluciones.
7. Plan de respaldo y restauración ensayado. Una migración describe estructura; no respalda los datos del negocio.

## Evidencias para Proyecto Integrado TI

Conservar en cada entrega: necesidad obtenida en entrevista, requisito asociado, diseño de pantalla y datos, código/migración, prueba con resultado y validación de la contraparte. Registrar los cambios de alcance. El catálogo de prueba y las reglas de demostración no se presentarán como información real de la pyme.

La demostración final debería mostrar dos usuarios en dispositivos separados: un cliente solicita, la dueña cotiza, el cliente confirma, la agenda actualiza cupos y la dueña registra la operación financiera. Añadir un intento de acceso a datos ajenos, una reserva simultánea del último cupo y una restauración de respaldo como evidencias técnicas.
