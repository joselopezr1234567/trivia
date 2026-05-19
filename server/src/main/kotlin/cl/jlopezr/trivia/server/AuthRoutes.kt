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

                if (request.username.isBlank() || request.email.isBlank() || request.password.isBlank()) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Todos los campos obligatorios deben ser completados"))
                }

                val profile = userRepository.registerUser(request)
                if (profile != null) {
                    // Generamos el token usando el ID asignado por Postgres
                    val token = JwtConfig.generateToken(profile.id)

                    // Ya no generamos un random extra aquí. El repositorio ya imprimió
                    // en la consola el código real que se guardó en la base de datos.

                    call.respond(HttpStatusCode.Created, profile.copy(token = token))
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "El nombre de usuario o el correo ya existen"))
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
                    val token = JwtConfig.generateToken(profile.id)
                    call.respond(HttpStatusCode.OK, profile.copy(token = token))
                } else {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Credenciales incorrectas. Verifica tu email o contraseña."))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno: ${e.localizedMessage}"))
            }
        }

        // 📲 ENDPOINT: VERIFICACIÓN DEL CÓDIGO HARDCOREADO
        post("/verify") {
            try {
                val request = call.receive<VerifyCodeRequest>()

                if (request.code.isBlank()) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "El código de verificación no puede estar vacío"))
                }

                val isSuccess = userRepository.verifySmsCode(request.userId, request.code)
                if (isSuccess) {
                    call.respond(HttpStatusCode.OK, mapOf("status" to "success", "message" to "Cuenta verificada exitosamente"))
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("status" to "error", "message" to "Código incorrecto o expirado"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno: ${e.localizedMessage}"))
            }
        }
    }
}