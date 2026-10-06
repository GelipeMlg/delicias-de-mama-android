# Respaldo en GitHub y recuperación

El repositorio debe ser privado. Contiene código Android, migraciones, funciones e informes del Proyecto Integrado TI. No contiene APK, cachés, SDK, contraseñas, archivos de cuenta de servicio ni configuración local de Firebase.

## Guardar avances

Desde la raíz del proyecto:

```powershell
git status
git diff
# Agregar únicamente los archivos revisados, por ejemplo:
git add README.md docs/estado-actual.md
git diff --cached --check
git commit -m "docs: actualizar estado del proyecto"
git push
```

Un commit guarda en este PC; `git push` envía al remoto. No hay sincronización automática por guardar un archivo. Verificar que GitHub muestre el último commit. No subir secretos aunque el repositorio sea privado.

## Recuperar en otro equipo

1. Iniciar sesión con una cuenta con acceso al repositorio privado y clonarlo con Git/Android Studio.
2. Abrir su carpeta `android`; instalar SDK 35 y usar JDK 21.
3. Configurar SDK local y descargar `google-services.json` del proyecto Firebase autorizado a `android/app/`.
4. Ejecutar pruebas y generar APK según `android/README.md`.
5. El proyecto Supabase existente sigue siendo remoto; clonar no crea una nueva base.

## Qué no respalda GitHub

Los registros y usuarios de Supabase, archivos del bucket Storage, secretos Vault/Edge Functions, trabajos Cron, configuración desplegada y claves de firma necesitan su propio respaldo y control de acceso. Las migraciones describen estructura, no constituyen una copia de datos reales. No exportar información de clientes al repositorio.

## Colaboración

Cada integrante trabaja en una rama de cambio creada desde `main` actualizado, sube la rama y abre un pull request. Las ramas antiguas de área deben actualizarse antes de reutilizarse. No cambiar de rama con trabajo sin guardar y no forzar un push para resolver divergencias.

Repositorio privado: https://github.com/GelipeMlg/delicias-de-mama-android

Para recuperar una copia (con autenticación GitHub autorizada):

```powershell
git clone https://github.com/GelipeMlg/delicias-de-mama-android.git
```
