import org.jetbrains.compose.reload.core.Environment.Companion.application
import org.jetbrains.compose.reload.core.HotReloadEnvironment.mainClass

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") // Necesario para @Serializable
    id("io.ktor.plugin")
    application
}

dependencies {
    // TODAS las dependencias de Ktor deben tener la misma versión (3.1.0 es la más reciente estable)
    val ktorVersion = "3.1.0"
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")

    // Serialización necesaria para el runtime
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Exposed (Base de datos) - versión moderna
    val exposedVersion = "0.56.0"
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.postgresql:postgresql:42.7.4")
}
