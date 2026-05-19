import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // ✅ Definimos los targets una sola vez
    val iosX64 = iosX64()
    val iosArm64 = iosArm64()
    val iosSimulatorArm64 = iosSimulatorArm64()

    // ✅ Configuramos los binarios usando la referencia a los targets ya creados
    listOf(iosX64, iosArm64, iosSimulatorArm64).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            // OPCIONAL: Desactivar caché aquí si el error de Autoboxing persiste
             freeCompilerArgs += listOf("-Xdisable-phases=Autoboxing")
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.uiToolingPreview)
                implementation(compose.materialIconsExtended) // Asegúrate de mantener la estructura exacta que tienes aquí

                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.0")

                implementation("org.jetbrains.androidx.navigation:navigation-compose:2.7.0-alpha07")
                implementation("org.jetbrains.androidx.navigation:navigation-runtime:2.7.0-alpha07")
                implementation("org.jetbrains.androidx.navigation:navigation-common:2.7.0-alpha07")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
                implementation("org.jetbrains.androidx.core:core-bundle:1.0.0-alpha01")

                // 🌐 AGREGA ESTAS 3 LÍNEAS PARA EL CLIENTE KTOR:
                implementation("io.ktor:ktor-client-core:2.3.11")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.11")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.11")
                implementation(libs.compose.material3)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.androidx.activity.compose)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
                implementation("io.mockk:mockk:1.13.12")
                implementation(kotlin("test"))

            }
        }
    }
}

android {
    namespace = "cl.jlopezr.trivia"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "cl.jlopezr.trivia"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}
