package cl.jlopezr.trivia.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.registerAuthRoutes(userRepository: UserRepository) {

    route("/auth") {

        // 📝 ENDPOINT: REGISTRO
        post("/register") {
            try {
                val request = call.receive<UserRegisterRequest>()

                // Validaciones básicas antes de tocar la BD
                if (request.username.isBlank() || request.email.isBlank() || request.password.isBlank()) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Todos los campos obligatorios deben ser completados"))
                }

                val profile = userRepository.registerUser(request)
                if (profile != null) {
                    call.respond(HttpStatusCode.Created, profile)
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "El nombre de usuario o el correo electrónico ya se encuentran registrados"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno: ${e.localizedMessage}"))
            }
        }

        // 🔑 ENDPOINT: INICIO DE SESIÓN
        post("/login") {
            try {
                val request = call.receive<UserLoginRequest>()

                if (request.email.isBlank() || request.password.isBlank()) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Email y contraseña son requeridos"))
                }

                val profile = userRepository.loginUser(request)
                if (profile != null) {
                    call.respond(HttpStatusCode.OK, profile)
                } else {
                    // 401 Unauthorized para credenciales incorrectas
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Credenciales incorrectas. Verifica tu email o contraseña."))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno: ${e.localizedMessage}"))
            }
        }
    }
}