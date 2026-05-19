plugins {
    // Usamos id estándar y quitamos la versión para que use la global de la raíz
    id("org.jetbrains.kotlin.jvm")
    id("io.ktor.plugin") version "2.3.11" // El plugin de Ktor sí lleva versión porque es único de este módulo
    application
}

group = "cl.jlopezr.trivia"
version = "1.0-SNAPSHOT"

application {
    // Le dice a Gradle dónde está el método main() para arrancar tu backend de Ktor
    mainClass.set("cl.jlopezr.trivia.server.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    // --- KTOR SERVER CORE ---
    implementation("io.ktor:ktor-server-core-jvm:2.3.11")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.11")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.11")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.11")

    // --- EXPOSED ORM & DATABASE DRIVERS ---
    implementation("org.jetbrains.exposed:exposed-core:0.50.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.50.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.50.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.50.0")
    implementation("org.postgresql:postgresql:42.7.2")

    // --- TEST DEPENDENCIES ---
    testImplementation("io.ktor:ktor-server-test-host-jvm:2.3.11")
    testImplementation("org.jetbrains.kotlin:kotlin-test") // Le quitamos la versión también aquí
    testImplementation("com.h2database:h2:2.2.224")
}