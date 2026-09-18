# Organización y trabajo con Git

## Ramas

| Rama | Propósito | Archivos habituales |
| --- | --- | --- |
| `main` | Reunir avances revisados y preparar entregas | Proyecto completo |
| `codex/base-de-datos` | Evolucionar tablas, restricciones, funciones y permisos | `supabase/`, documentación técnica asociada |
| `codex/android` | Desarrollar y probar la aplicación | `android/`, guías y evidencias relacionadas |
| `codex/documentacion` | Mantener informes, requisitos, diagramas y manuales | `docs/`, `scripts/`, índices del repositorio |
| `codex/primer-informe` | Conservar el punto anterior a la reorganización | Historial de la preparación inicial hasta `9985324` |

Las tres ramas de área nacen desde la misma base completa. El SQL sigue dentro de `supabase/` cuando se trabaja en Android, y los documentos siguen disponibles cuando se modifica la base de datos. Esto permite revisar requisitos, código y pruebas juntos.

La separación es por responsabilidad, no una prohibición de tocar otros archivos: un cambio SQL puede necesitar actualizar su diccionario en `docs/`, y un cambio Android debe actualizar su guía cuando corresponda.

## Dónde guardar cada archivo

```text
Delicias de mamá/
├── README.md                   Índice general
├── CONTRIBUTING.md             Guía de ramas y colaboración
├── android/                    Proyecto que se abre en Android Studio
│   └── app/src/
│       ├── main/                Código y recursos de la aplicación
│       └── test/                Pruebas de reglas
├── supabase/
│   ├── migrations/             Cambios versionados de la base
│   └── tests/                  Pruebas SQL
├── docs/
│   ├── informe-01/             Informe fuente, análisis e imágenes
│   ├── entregables/            Word y paquetes preparados para entregar
│   └── *.md                    Diseño, planes, manuales y verificaciones
└── scripts/                    Generadores y utilidades del proyecto
```

Los APK generados por Gradle quedan en `android/app/build/outputs/` y no se versionan. `.tmp/` contiene herramientas o verificaciones temporales y tampoco se incorpora a Git. El ZIP de la demo es una entrega identificada como v0.1, no la fuente de trabajo: el código editable está en `android/`.

## Cambiar a la rama adecuada

Abre la terminal en la carpeta raíz del repositorio. Comprueba primero el estado:

```powershell
git status
git branch
```

Con el directorio limpio, ejecuta uno de estos comandos según el trabajo:

```powershell
git switch codex/base-de-datos
git switch codex/android
git switch codex/documentacion
```

Cada comando cambia la rama activa; no se ejecutan los tres para realizar una sola tarea. Guarda tus cambios en un commit antes de cambiar de área: Git puede arrastrar cambios sin confirmar a otra rama cuando no hay conflictos. No descartes trabajo para forzar un cambio de rama.

## Ejemplo de trabajo SQL

```powershell
git switch codex/base-de-datos
git merge main
```

Agrega una nueva migración en `supabase/migrations/`, sus pruebas y el cambio de documentación correspondiente. Antes de guardar:

```powershell
git diff
git diff --check
git add supabase/migrations/NUEVA_MIGRACION.sql supabase/tests/NUEVA_PRUEBA.sql
git commit -m "feat(db): describir el cambio realizado"
```

Reemplaza los nombres en mayúsculas por archivos que hayas creado. Incluye también los documentos modificados. No ejecutes esos nombres como si fueran archivos ya existentes.

Las migraciones ya aplicadas se conservan. Los cambios posteriores se escriben en una migración nueva, por ejemplo `AAAAMMDDHHMM_descripcion.sql`, con un identificador único. La migración inicial ya se ejecutó manualmente en Supabase: no se debe repetir sobre la base existente.

## Integrar una entrega

Después de revisar el cambio y ejecutar las pruebas apropiadas:

```powershell
git switch main
git merge codex/base-de-datos
```

Para integrar Android o documentación, usa su nombre de rama en lugar de `codex/base-de-datos`. Si Git informa conflictos, revisa y resuelve los archivos afectados antes de continuar. Después de integrar un área, lleva los cambios de `main` a otra rama cuando vayas a trabajar en ella:

```powershell
git switch codex/android
git merge main
```

Mientras no haya un remoto configurado, estas ramas y commits existen únicamente en este equipo. Cuando el equipo conecte un repositorio compartido, se podrá revisar e integrar mediante pull requests.

## Qué comprobar por área

- **Base de datos:** integridad, permisos y transacciones; consulta `supabase/README.md` antes de ejecutar pruebas sobre un servicio remoto.
- **Android:** pruebas de reglas, compilación y revisión en dispositivo según `docs/guia-android.md`. Mantener explícita la distinción entre demo local y app conectada.
- **Documentación:** coherencia con lo implementado, enlaces válidos, fuentes y evidencia. Los Word necesitan revisión visual; la limitación pendiente del documento actual está registrada en `docs/base-de-datos.md`.

## Git y Supabase son independientes

Cambiar de rama Git no cambia la base remota ni crea una rama de base de datos en Supabase. Las tres ramas de trabajo comparten actualmente el mismo proyecto remoto, por lo que no deben aplicar migraciones incompatibles como si fueran entornos aislados. La organización del Schema Visualizer tampoco se guarda automáticamente dentro de Git.
