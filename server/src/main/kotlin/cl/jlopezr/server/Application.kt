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
import kotlinx.serialization.json.Json
import io.ktor.http.* // Importante para los códigos de estado
import kotlinx.serialization.SerialName

@Serializable
data class LoginRequest(
    @SerialName("email")
    val email: String,
    val password: String)

@Serializable
data class LoginResponse(val success: Boolean, val message: String)

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }

        routing {
            route("/auth") {
                post("/login") {
                    try {
                        val credentials = call.receive<LoginRequest>()

                        if (checkInDatabase(credentials)) {
                            call.respond(HttpStatusCode.OK, LoginResponse(true, "Acceso concedido"))
                        } else {
                            // Enviamos 401 para que el Android sepa que falló el login
                            call.respond(HttpStatusCode.Unauthorized, LoginResponse(false, "Credenciales incorrectas"))
                        }
                    } catch (e: Exception) {
                        // Esto captura si el JSON que llega de Android está mal
                        call.respond(HttpStatusCode.BadRequest, LoginResponse(false, "Error en el formato: ${e.message}"))
                    }
                }

                post("/register") {
                    call.respond(LoginResponse(true, "Registro pendiente de implementar"))
                }
            }
        }
    }.start(wait = true)
}

fun checkInDatabase(credentials: LoginRequest): Boolean {
    // CORREGIDO: credentials.email (antes decía emailtest)
    return credentials.email == "admin" && credentials.password == "1234"
}