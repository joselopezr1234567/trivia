


plugins {
    kotlin("multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("plugin.serialization")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework { baseName = "ComposeApp"; isStatic = true }
    }

    sourceSets {
        commonMain.dependencies {
            val ktorVersion = "3.1.0"
            // Dependencias de red para la app

            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.8.0-alpha10")
            implementation("io.insert-koin:koin-core:4.0.0")
            implementation("io.insert-koin:koin-compose:4.0.0")
            implementation("io.insert-koin:koin-compose-viewmodel:4.0.0")
            implementation("io.ktor:ktor-client-core:$ktorVersion")
            implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
            implementation(compose.components.uiToolingPreview) // Usually already there
            implementation("org.jetbrains.compose.components:components-resources:1.7.0") // Check version matches your project

            // Specifically for Extended Icons:
            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.0")

            implementation(compose.components.resources)
            // Añade aquí tus dependencias de Compose UI
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(project(":shared"))
        }
        androidMain.dependencies {
            val ktorVersion = "3.1.0"
            implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
            implementation("androidx.activity:activity-compose:1.9.3")
            implementation("androidx.compose.ui:ui-tooling-preview:1.7.6")
            implementation("io.insert-koin:koin-android:4.0.0")
            implementation("io.insert-koin:koin-androidx-compose:4.0.0")
            implementation("io.ktor:ktor-client-cio:${ktorVersion}")
            
            // Google AdMob SDK
            implementation("com.google.android.gms:play-services-ads:23.6.0")

            // Splash Screen API
            implementation("androidx.core:core-splashscreen:1.0.1")
        }
    }
}

android {
    packaging {
        resources {
            excludes += "META-INF/io.netty.versions.properties"
            // Excluye el archivo específico que causa el error
            excludes += "META-INF/INDEX.LIST"

            // Es buena práctica excluir también estos otros para evitar errores similares
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/license.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/notice.txt"
            excludes += "META-INF/ASL2.0"
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/*.kotlin_module"
        }
    }
    namespace = "cl.jlopezr.trivia"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        // Esto es lo que estaba causando el conflicto (probablemente tenías 1.8 aquí)
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
// Elimina el bloque dependencies fuera de kotlin si no es necesario o asegúrate de que esté vacío




