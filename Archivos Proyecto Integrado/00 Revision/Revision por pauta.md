# Revisión documental frente a las guías U1 U2 y U3

Revisión del 6 de octubre de 2026. Alcance: lectura de guías, párrafos y tablas de los Word, documentación técnica y reportes disponibles. No se certificó el formato visual de todas las páginas Word, ni se repitieron pruebas de teléfonos o del servidor. No se asigna una nota: falta el instrumento de evaluación con ponderaciones, además de las guías.

## Conclusión

La aplicación ofrece un alcance apropiado para el proyecto integrado. El paquete documental aún requiere consolidación y evidencias de cierre. La guía U3 permite priorizar los casos críticos y pruebas manuales básicas; no exige agregar funcionalidades sin relación con las necesidades. Un resultado automatizado local no demuestra por sí solo autorización remota, integración completa o aceptación de la dueña.

## Unidad 1 Diagnóstico

| Criterio | Evidencia encontrada | Estado y acción |
|---|---|---|
| 1.1.1 Necesidades TI | Informe de diagnóstico: antecedentes, necesidades, cinco porqués y prioridades; informe original | Documentado. Confirmar vigencia y vinculación con entrevista; no inventar respuestas adicionales. |
| 1.1.2 Historias de usuario | Tabla de historias, aceptación y prioridad en diagnóstico | Documentado. Revisar las 5 o 6 historias clave contra el alcance final. |
| 1.1.3 Requisitos funcionales y no funcionales | Tablas RF/RNF y plan de implementación | Parcialmente actualizado. El plan describe la demo local antigua; crear matriz vigente requisito → función → prueba → resultado. |
| 1.1.4 Metodología | Diagnóstico menciona Scrum; informe posterior desarrolla incremental | Inconsistencia. Unificar la entrega final en incremental y justificarlo; conservar versiones anteriores como históricas. |
| 1.1.5 Seguridad y ética | Informes y docs/autenticacion.md, SQL de permisos | Documentado a nivel de propuesta e implementación. Agregar evidencia de permisos efectivos y verificar referencias normativas antes de entregar. |

## Unidad 2 Diseño

| Criterio | Evidencia encontrada | Estado y acción |
|---|---|---|
| 2.1.1 Cronograma | Incrementos y estimación orientativa de ocho semanas | Falta acreditar cronograma fechado, dependencias, hitos, recursos y responsables reales. No convertir estimaciones en fechas ejecutadas. |
| 2.1.2 Arquitectura 4+1 | Secciones UML/casos de uso y despliegue en informe incremental | Cobertura completa no acreditada. Revisar/actualizar clases, componentes, procesos BPMN y caso de uso del sistema. El esquema de tablas no sustituye estos diagramas. |
| 2.1.3 Interfaces | Wireframes conceptuales y capturas de catálogo/finanzas | Parcial. Añadir mapa de navegación, perfiles, pantallas actuales y resultado de evaluación UX. |
| 2.1.4 Modelo de datos | SQL, documentación inicial y migraciones posteriores | Existe. Actualizar modelo/diccionario con pedidos compartidos, fotos y notificaciones; evitar usar solo el esquema inicial de diez tablas. |
| 2.1.5 Programación segura | Auth, rol de dueña, RLS/RPC, manejo de sesiones y documentación | Vincular controles concretos con roles, validaciones y manejo de errores; bcrypt es hash de contraseñas, no cifrado reversible. |
| 2.1.6 Plan de pruebas | Guías de recorridos y archivos de pruebas | Parcial. Formalizar casos, precondiciones, datos, responsables y resultados esperados; incluir prueba funcional y de seguridad o rendimiento. |

La U2 admite metodología justificada y recomienda Gantt/PERT para enfoque tradicional; condiciona sprint/backlog/Kanban al enfoque ágil. La U3 menciona Scrum o Kanban al hablar de colaboración. Mantener el modelo incremental elegido, describir la colaboración real y confirmar con el docente cómo documentarla; no inventar ceremonias Scrum.

## Unidad 3 Implementación

| Criterio | Evidencia encontrada | Estado y acción |
|---|---|---|
| 3.1.1 Interfaces construidas | Código Compose, capturas históricas y APK | Implementación disponible. Faltan capturas actuales del flujo completo y comprobación de usabilidad/accesibilidad. |
| 3.1.2 Base de datos en nube | PostgreSQL/Supabase y migraciones | Implementación disponible. El detalle de las guías permite elegir relacional o no relacional; no añadir otra base solo por la redacción del criterio. |
| 3.1.3 Programación segura y equipo | Código, controles, pruebas y GitHub | Parcial en evidencia. Documentar patrones que realmente usa el código, aportes y revisiones reales por integrante. Los roles del informe son propuestos, no acreditados. |
| 3.1.4 Instalación y configuración | Manual Android/ADB, Supabase y Firebase, APK | Documentado. Registrar versión, dispositivo, instalación, monitoreo y limitaciones de operación. |
| 3.1.5 Ejecutar plan de pruebas | 39 casos JUnit aprobados, recorridos manuales pendientes | Parcial. Ejecutar y registrar flujo entre dos teléfonos, permisos remotos, sincronización y errores. |
| 3.1.6 Comparar esperado y obtenido | XML con cero fallos; notas técnicas | Falta tabla por caso con resultado esperado, observado, fecha, evidencia, defecto y corrección/reprueba. |

## Evidencias disponibles y su alcance

- XML/HTML JUnit: 39 casos, cero fallos y cero errores. Cinco suites: acceso (8), catálogo (5), presentación de pedidos (6), sabores (8), reglas del negocio (12). Evidencia local; no prueba todo Supabase/FCM.
- Catálogo y finanzas: capturas históricas asociadas a la revisión del 20 de septiembre. No acreditan por sí solas el estado conectado final.
- Wireframes: imagen generada como propuesta; sus datos son ilustrativos. No es evidencia de ejecución.
- Notificaciones en segundo plano e imágenes en vivo: confirmadas por el usuario en conversación. Falta un registro formal reproducible del recorrido completo.
- SQL de tests: evidencia de casos preparados; su presencia no demuestra que se ejecutaron ahora.
- GitHub: repositorio privado con main e historial. Respalda archivos, no datos ni objetos de Storage.

## Correcciones de los informes existentes

1. Informe de diagnóstico: revisar Scrum y afirmaciones de conexión pendiente frente a la metodología/estado vigente.
2. Informe incremental: conserva fecha y estado del 23 de septiembre, veinte pruebas y operación compartida pendiente. Actualizar una nueva edición final con Supabase, FCM, fotos y 39 pruebas, manteniendo límites reales.
3. Resumen de base de datos: describe la creación inicial. Conservar como antecedente y complementarlo con el modelo actual.
4. Plan de implementación y verificación Android: contienen limitaciones históricas de Gradle/OneDrive ya superadas. No usarlas como estado actual.
5. La viabilidad está fundamentada de forma preliminar; faltan presupuesto/beneficios medidos y validación operativa antes de afirmar viabilidad productiva comprobada.

## Orden de cierre recomendado

1. Confirmar fechas, sección, grupo y funciones efectivamente realizadas por Felipe Alvarado, Jafet Lavados y Matias Otero.
2. Consolidar requisitos y alcance; validar con la dueña sin atribuir conformidad inexistente.
3. Completar cronograma, diagramas y mapa de navegación.
4. Ejecutar pruebas manuales usando la plantilla; capturar pantallas actuales sin exponer datos personales.
5. Redactar edición final U3 con equipo, objetivos, desafío, justificación, enfoque técnico, gestión, entregables y anexos, incorporando U1/U2.
6. Revisar formato: texto 12 puntos, márgenes mínimos 2,54 cm, sangría, números inferiores centrados; figuras tituladas y referidas. Más de 10 páginas: índice y listas aplicables.
7. Preparar defensa individual. La infografía se aplica solo a la presentación final: hasta 10 minutos, hasta 20 diapositivas, fuente mínima 30 puntos según el archivo recibido.
