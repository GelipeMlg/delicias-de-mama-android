plugins {
    id("com.android.application") version "9.4.1" apply false
    id("org.jetbrains.kotlin.android") version "2.2.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
}

// Usa las salidas estándar de Gradle: android/build y android/app/build.
// Android Studio y su instalador deben leer los mismos artefactos del proyecto.
