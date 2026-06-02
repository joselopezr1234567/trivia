plugins {
    // Aplicamos los plugins necesarios
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("io.ktor.plugin") version "3.1.0"
    application
}

// Configuración para asegurar que el compilador sea compatible con tu entorno
kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
}

dependencies {
    val ktorVersion = "3.1.0"

    // Ktor Server
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")

    // Serialización
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Exposed (Base de datos)
    val exposedVersion = "0.56.0"
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    // Driver PostgreSQL
    implementation("org.postgresql:postgresql:42.7.4")

    // Logs (útil para ver qué está pasando en el servidor)
    implementation("ch.qos.logback:logback-classic:1.5.6")
}

application {
    mainClass.set("cl.jlopezr.server.ApplicationKt")
}