# Propuesta técnica para Delicias de Mamá

Fecha de consulta: 16 de septiembre de 2026. Estado: recomendación inicial basada en el primer informe. El equipo confirma que no existe una tecnología impuesta por el profesor y que el objetivo es innovar tecnológicamente en la pyme según los requerimientos de la entrevista. La rúbrica específica no ha sido revisada.

## Evaluación académica

El proyecto es un candidato razonable para Proyecto Integrado TI: aborda un negocio real e integra levantamiento de requisitos, modelado de datos, desarrollo móvil, autenticación, permisos, pruebas y puesta en funcionamiento. Esta es una evaluación técnica, no una aprobación académica. La búsqueda no permitió verificar la rúbrica específica de la asignatura.

Para sostener la defensa, preparar requisitos validados con la dueña, trazabilidad entre requisitos y pruebas, DER, arquitectura, evidencia de funcionamiento entre dos dispositivos, manuales y prueba de restauración del respaldo. La complejidad útil está en resolver reservas, cotizaciones y control financiero con consistencia.

## Recomendación

Usar Android Studio, Kotlin y Jetpack Compose para Android nativo, con Android 8.0 como mínimo objetivo del informe. Elegir dependencias compatibles con ese mínimo y comprobarlo al compilar y ejecutar. Google recomienda Compose para nuevas interfaces y una arquitectura separada en interfaz y datos, con ViewModels y repositorios. [Arquitectura Android](https://developer.android.com/topic/architecture/recommendations).

Usar Supabase como servicio compartido: PostgreSQL para datos relacionales, Auth para sesiones y Storage para imágenes cuando sea necesario. PostgreSQL facilita representar pedidos, detalles, cotizaciones, ventas y gastos con relaciones y restricciones. Esta preferencia es una valoración de adecuación al proyecto. Existe una [guía Android con Kotlin](https://supabase.com/docs/guides/getting-started/quickstarts/kotlin); el [cliente Kotlin es comunitario, no oficial](https://supabase.com/docs/reference/kotlin/introduction), por lo que debemos comprobar sus versiones y compatibilidad mediante un pequeño flujo de prueba antes de adoptarlo definitivamente.

## Alternativas comparadas

| Alternativa | Ventaja para este proyecto | Costo o limitación | Evaluación |
| --- | --- | --- | --- |
| Kotlin y Compose + Supabase/PostgreSQL | Android nativo y datos relacionales adecuados para DER, restricciones y reportes | Configurar SQL, permisos y comportamiento ante fallos de red; cliente Kotlin comunitario | Recomendada para el alcance documentado |
| Kotlin y Compose + Firebase Auth/Firestore | Integración móvil y caché con sincronización sin conexión | Firestore requiere un modelo documental; las relaciones y reportes deben diseñarse para él | Alternativa si priorizamos sincronización móvil y no se exige SQL |
| Flutter + Supabase | Posibilidad de compartir código entre plataformas | Cambia la implementación Kotlin nativa prevista en el informe | Considerarla si se amplía el alcance a iOS y se acuerda el cambio |
| Kotlin y Compose + API propia y PostgreSQL | Mayor control sobre lógica y evidencia de desarrollo del servidor | Añade construcción, pruebas y despliegue de otro componente | Preferible si la rúbrica exige una API propia |

Referencias: [arquitectura Flutter](https://docs.flutter.dev/resources/architectural-overview), [persistencia sin conexión de Firestore](https://firebase.google.com/docs/firestore/manage-data/enable-offline). El caché sincronizado no es un respaldo ni garantiza que un cupo siga disponible.

## Arquitectura propuesta

Una aplicación con pantallas según rol. Compose dibuja la interfaz; ViewModels mantienen su estado; repositorios separan las pantallas del servicio de datos. Empezar con una estructura pequeña y agregar capas solo cuando haya lógica que lo justifique.

El teléfono accede a Supabase por sus APIs autenticadas; no se conecta con credenciales administrativas a PostgreSQL. Las políticas RLS deben permitir al cliente acceder solo a sus pedidos y a la dueña gestionar la operación. El cliente no puede asignarse privilegios. No incluir claves secretas o service_role en el APK. [Seguridad por fila](https://supabase.com/docs/guides/database/postgres/row-level-security).

Confirmar pedidos mediante una operación transaccional en el servidor que valide permisos, estado y cupo. Si dos personas intentan reservar el último cupo, solo una confirmación debe prosperar. La app puede guardar un borrador sin conexión; la confirmación requiere respuesta del servidor. No prometer sincronización automática con Supabase: cualquier caché y resolución de conflictos se implementará explícitamente.

Room puede añadirse si se necesita conservar catálogo o borradores localmente; no sustituye la base compartida. No introducir dos bases desde la primera pantalla sin una necesidad definida.

## Funciones y entregas propuestas

### Primera entrega funcional

- Catálogo con fotos, descripción y precio orientativo.
- Inicio de sesión y permisos de cliente y administradora.
- Calendario con disponibilidad, días bloqueados y cupos acordados con la dueña.
- Solicitud personalizada con fecha, hora, sabores, relleno, temática y restricciones alimentarias.
- Respuesta manual de cotización por la dueña y seguimiento del pedido.

### Segunda entrega funcional

- Aprobación, rechazo y cancelación con reglas de cupos y registro de cambios.
- Registro de ventas, pagos y gastos; filtros por fecha.
- Resumen financiero con reglas explícitas para diferenciar ingresos cobrados, ventas y gastos.
- Respaldo, restauración, pruebas de permisos y medición de tiempos.

Estas dos entregas cubren el núcleo de RF-01 a RF-07. Son una propuesta de reorganización del plan original, no un cambio ya validado con el equipo.

### Ampliaciones posteriores

Notificaciones de cambios de estado, exportación de reportes, fotos de referencia y contacto por WhatsApp. Evaluar pagos en línea, fidelización y control detallado de insumos después del núcleo funcional. Evitar convertir esas ampliaciones en compromisos dentro de las ocho semanas estimadas sin revisar el esfuerzo.

## Costos y operación

El plan gratuito de Supabase puede servir para un prototipo pequeño sujeto a sus límites; no se ha estimado consumo real. Los proyectos gratuitos con baja actividad pueden pausarse tras un período de siete días y necesitan un plan explícito de exportación y recuperación. Verificar disponibilidad antes de una demostración y conservar migraciones y datos ficticios reproducibles. [Precios](https://supabase.com/pricing), [pausas](https://supabase.com/docs/guides/platform/free-project-pausing), [respaldos](https://supabase.com/docs/guides/platform/backups).

Firebase también tiene cuotas gratuitas y servicios sujetos a facturación; una arquitectura con imágenes, funciones o autenticación telefónica debe presupuestarse por componentes. [Precios Firebase](https://firebase.google.com/pricing). Ninguna opción debe describirse como gratuita sin límites para producción.

La demostración académica puede comenzar con un APK instalado en dispositivos de prueba y una base de datos de demostración. La publicación en tienda será una decisión posterior.

## Verificación y próximos pasos

1. Revisar las respuestas de la entrevista y vincular cada función con una necesidad de la dueña; el informe disponible solo resume sus resultados. El equipo confirma libertad de elección tecnológica.
2. Acordar capacidad diaria, estados del pedido, aceptación de cotizaciones y momento de registro de ventas.
3. Diseñar DER y wireframes de cliente y dueña.
4. Preparar Android Studio, SDK y emulador o dispositivo físico.
5. Crear el esqueleto Android y comprobar un flujo de autenticación, lectura y escritura con permisos antes de construir todos los módulos.
6. Probar reserva simultánea del último cupo, acceso a pedidos ajenos, exactitud de totales, reinicio de la aplicación, restauración y consultas en menos de dos segundos bajo condiciones definidas.

En esta revisión no se encontraron Android Studio ni Android SDK en sus rutas habituales de este equipo, ni java, adb o gradle en PATH. Esto no descarta una instalación en otra ubicación. Aún no se ha compilado una app, creado una cuenta de servicios ni desplegado infraestructura.
