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
            // Agrupa bajo /auth para mantener orden
            route("/auth") {

                post("/login") {
                    val credentials = call.receive<LoginRequest>() // Ktor ya hace el decode por ti

                    if (checkInDatabase(credentials)) {
                        call.respond(LoginResponse(true, "Acceso concedido"))
                    } else {
                        call.respond(LoginResponse(false, "Credenciales incorrectas"))
                    }
                }

                post("/register") {
                    // Aquí irá tu lógica de registro más adelante
                    call.respond(LoginResponse(true, "Registro pendiente de implementar"))
                }
            }
        }

    }.start(wait = true)
}

fun checkInDatabase(credentials: LoginRequest): Boolean {
    // Aquí después pondrás la consulta real a tu base de datos en Render
    return credentials.username == "admin" && credentials.password == "1234"
}