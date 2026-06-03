plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
}

kotlin {
    jvmToolchain(17)
    androidTarget {
        // Esto es suficiente para Kotlin, pero necesitamos que Android también lo sepa
    }
    jvm()

    iosX64(); iosArm64(); iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            val ktorVersion = "3.1.0"
            implementation("io.ktor:ktor-client-core:$ktorVersion")
            implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            implementation("io.ktor:ktor-client-cio:$ktorVersion") // ¡Necesitas el motor!
        }
    }
}

android {
    namespace = "cl.jlopezr.trivia.shared"
    compileSdk = 35 // Cambiado a 35 (más estable que 36)
    defaultConfig { minSdk = 24 }

    // ESTO ES LO QUE HACE QUE LAS DEPENDENCIAS NO SEAN ROJAS
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}