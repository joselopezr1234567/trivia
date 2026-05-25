package cl.jlopezr.server

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class LoginResponse(val success: Boolean, val message: String)

fun main() {
    embeddedServer(Netty, port = 8080) {
        // Instalamos la capacidad de entender JSON
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true
                prettyPrint = true})
        }

        routing {
            // Ruta de prueba
            get("/") {
                call.respondText("Servidor de Login funcionando!")
            }

            // Ruta de login
            post("/login") {
                val rawBody = call.receiveText() // Obtiene el JSON como texto plano
                println("JSON recibido: $rawBody") // Míralo en la consola de tu terminal

                // Si esto falla, el problema es el formato del JSON
                val credentials = Json.decodeFromString<LoginRequest>(rawBody)

                if (checkInDatabase(credentials)) {
                    call.respond(LoginResponse(true, "Acceso concedido"))
                } else {
                    call.respond(LoginResponse(false, "Credenciales incorrectas"))
                }
            }
        }
    }.start(wait = true)
}

fun checkInDatabase(credentials: LoginRequest): Boolean {
    // Aquí después pondrás la consulta real a tu base de datos en Render
    return credentials.username == "admin" && credentials.password == "1234"
}