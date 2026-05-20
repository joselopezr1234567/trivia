plugins {
    id("org.jetbrains.kotlin.jvm") // El de la JVM base que hereda perfecto
    id("application") // Plugin nativo de Gradle para ejecutables
    kotlin("plugin.serialization") version "2.0.0" // 👈 🛠️ ¡ESTA ES LA LÍNEA CRUCIAL QUE FALTABA!
}

group = "cl.jlopezr.trivia"
version = "1.0.0"

application {
    mainClass.set("cl.jlopezr.trivia.server.ApplicationKt")
}

// 🌐 CONFIGURACIÓN GLOBAL DE JAVA
kotlin {
    jvmToolchain(24) // Usando el JDK 24 activo en tu Mac
}

dependencies {

    val mapSourceVersion = "2.3.11"
    val exposedVersion = "0.50.1"

    // 🌐 Ktor Server Core y Motor Netty
    implementation("io.ktor:ktor-server-core-jvm:$mapSourceVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$mapSourceVersion")

    // 📂 Plugins de Ktor para serializar JSON y manejar rutas/CORS
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$mapSourceVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$mapSourceVersion")
    implementation("io.ktor:ktor-server-cors-jvm:$mapSourceVersion")

    // 📦 Dependencias Core de Serialización JSON en Texto Plano
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // 🔐 Módulos de Autenticación y Tokens JWT
    implementation("io.ktor:ktor-server-auth-jvm:$mapSourceVersion")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$mapSourceVersion")

    // 🗄️ Base de datos: Exposed Framework & PostgreSQL Driver
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")
    implementation("org.postgresql:postgresql:42.7.3")

    // 🚀 Cliente HTTP (Motor CIO) para hacer las peticiones a OpenAI
    implementation("io.ktor:ktor-client-core-jvm:$mapSourceVersion")
    implementation("io.ktor:ktor-client-cio-jvm:$mapSourceVersion")
    // ✅ Se le agrega la versión correspondiente para asegurar compatibilidad total en el cliente
    implementation("io.ktor:ktor-client-content-negotiation-jvm:$mapSourceVersion")

    // 📝 Logs del sistema
    implementation("ch.qos.logback:logback-classic:1.4.14")
}