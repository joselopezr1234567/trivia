plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    // Aplicamos el plugin de serialización de forma segura
    kotlin("plugin.serialization")
}

kotlin {
    // 1. Registro del target de Android con la nueva sintaxis 'compilerOptions'
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    // 2. Registro de targets de iOS
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    // 3. Configuración de dependencias
    sourceSets {
        commonMain.dependencies {
            // Asegúrate de tener esta librería en tu libs.versions.toml
            implementation(libs.kotlinx.serialization.json)
            val ktorVersion = "2.3.11" // O usa una versión definida en tu TOML
            implementation("io.ktor:ktor-client-core:$ktorVersion")
            implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
        }
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
        }
    }
}

android {
    namespace = "cl.jlopezr.trivia.shared"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    // Configuración de compatibilidad obligatoria
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}