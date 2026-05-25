plugins {
    // Solo declaración de plugins globales sin aplicarlos directamente aquí
    id("org.jetbrains.kotlin.multiplatform") version "2.3.20" apply false
    id("com.android.application") version "8.11.2" apply false
    id("com.android.library") version "8.11.2" apply false
    id("org.jetbrains.compose") version "1.10.3" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.20" apply false
    id("io.ktor.plugin") version "2.3.12" apply false
    kotlin("jvm") version "2.0.0" apply false
    kotlin("plugin.serialization") version "2.0.0" apply false
}