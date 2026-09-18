# Delicias de Mamá

Proyecto académico de Ingeniería de Software para una aplicación Android de gestión de una repostería.

## Organización

| Área | Carpeta | Rama de trabajo |
| --- | --- | --- |
| SQL, migraciones y pruebas de base de datos | [supabase/](supabase/README.md) | `codex/base-de-datos` |
| Código Kotlin, pantallas y pruebas Android | [android/](android/README.md) | `codex/android` |
| Informes, requisitos, diseño y guías | [docs/](docs/README.md) | `codex/documentacion` |
| Generadores de documentación | [scripts/](scripts/README.md) | `codex/documentacion` |

`main` reúne los avances revisados de las tres áreas. `codex/primer-informe` conserva el punto de partida anterior a esta organización, incluyendo el informe, las tablas y el prototipo que ya habíamos preparado.

Las carpetas indican dónde guardar cada archivo. Las ramas separan los cambios en desarrollo y parten de una base completa común. Al crearlas tienen el mismo contenido; no son tres copias aisladas del proyecto.

Consulta [cómo cambiar de rama y unir avances](CONTRIBUTING.md).

## Base del proyecto

El proyecto reúne el primer informe, la base de datos inicial y el código de un prototipo Android local. La integración Android con Supabase sigue pendiente. La rama `main` representa la versión integrada actual, no una aplicación terminada para producción.

- [Análisis del primer informe](docs/informe-01/analisis.md): objetivos, requisitos, dudas y propuesta de inicio.
- [Propuesta técnica](docs/propuesta-tecnica.md): evaluación académica, comparación de tecnologías y alcance recomendado.
- [Proyecto Supabase](docs/supabase.md): estado verificado y preparación de la base compartida.
- [Base de datos inicial](docs/base-de-datos.md): tablas creadas, relaciones, pruebas y límites de implementación.
- [Resumen Word](docs/entregables/Resumen_base_de_datos_Delicias_de_Mama.docx): documentación académica de la base de datos.
- [Código Android](android/README.md): aplicación de demostración local en Kotlin y Compose.
- [Guía para Android Studio](docs/guia-android.md): instalación, apertura, ejecución y recorrido de prueba.
- [Plan de implementación](docs/plan-implementacion.md): etapas, requisitos y documentación de la versión conectada.
- [Verificación Android](docs/verificacion-android.md): pruebas ejecutadas y comprobaciones pendientes.
- [Informe original](docs/informe-01/original/InformeTrabajoIngSoft(V2).docx): copia sin modificaciones del documento recibido.
- [Texto extraído](docs/informe-01/texto-extraido.txt): apoyo para búsquedas; el Word conserva la estructura original de tablas e imágenes.
- [Imágenes del documento](docs/informe-01/diagramas/): recursos originales extraídos del Word.

El informe plantea Android nativo, Kotlin, Android Studio y compatibilidad desde Android 8.0. La arquitectura de almacenamiento y sincronización sigue pendiente de definición.

## Próximo paso

Abrir el prototipo en Android Studio, validar las reglas y el flujo con la dueña y avanzar con autenticación, políticas RLS y operaciones transaccionales de Supabase.
