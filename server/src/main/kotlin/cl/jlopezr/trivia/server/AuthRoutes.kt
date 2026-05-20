package cl.jlopezr.trivia.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.insert

fun Route.registerAuthRoutes(userRepository: UserRepository) {

    // ==========================================
    // 👤 CAPA DE AUTENTICACIÓN Y USUARIOS (/auth)
    // ==========================================
    route("/auth") {

        // 📝 ENDPOINT: REGISTRO DE USUARIOS
        post("/register") {
            try {
                val request = call.receive<UserRegisterRequest>()

                if (request.username.isBlank() || request.email.isBlank() || request.password.isBlank()) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Todos los campos obligatorios deben ser completados"))
                }

                val profile = userRepository.registerUser(request)
                if (profile != null) {
                    val token = JwtConfig.generateToken(profile.id)
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

        // 📲 ENDPOINT: VERIFICACIÓN DE CÓDIGO
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

        // 💾 ENDPOINT: GUARDAR RESPUESTA DEL USUARIO EN HISTORIAL
        post("/answer") {
            try {
                @kotlinx.serialization.Serializable
                data class AnswerRequest(val userId: Int, val questionId: Int, val isCorrect: Boolean)

                val request = call.receive<AnswerRequest>()

                DatabaseFactory.dbQuery {
                    UserAnswersTable.insert {
                        it[userId] = request.userId
                        it[questionId] = request.questionId
                        it[isCorrect] = request.isCorrect
                    }
                }

                call.respond(HttpStatusCode.OK, mapOf("status" to "success", "message" to "Respuesta guardada en el historial"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.localizedMessage))
            }
        }
    }


    // ==========================================
    // 🧠 CAPA DE TRIVIA E IA (/trivia)
    // ==========================================
    route("/trivia") {

        // 🚀 ENDPOINT: GENERAR PREGUNTA CON IA Y GUARDAR EN POSTGRES
        get("/generate") {
            try {
                val category = call.request.queryParameters["category"] ?: "general"
                val userId = call.request.queryParameters["userId"]?.toIntOrNull()

                if (userId == null) {
                    return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "El parámetro 'userId' es obligatorio"))
                }

                val openAiService = OpenAiService()

                // ✅ 1. Forzamos el nombre de tu función real 'generateQuestionFromAi'
                // ✅ 2. Le asignamos explícitamente el tipo de dato QuestionResponse para evitar el bug del .copy()
                val preguntaIA: QuestionResponse? = openAiService.generateQuestionFromAi(category)

                if (preguntaIA == null) {
                    return@get call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "No se pudo obtener una respuesta válida de OpenAI"))
                }

                var questionIdInserted = 0
                DatabaseFactory.dbQuery {
                    questionIdInserted = QuestionsTable.insert {
                        it[QuestionsTable.category] = preguntaIA.category
                        it[QuestionsTable.questionText] = preguntaIA.questionText
                        it[QuestionsTable.optionA] = preguntaIA.optionA
                        it[QuestionsTable.optionB] = preguntaIA.optionB
                        it[QuestionsTable.optionC] = preguntaIA.optionC
                        it[QuestionsTable.optionD] = preguntaIA.optionD

                        // ✅ SOLUCIÓN: Extraemos el Char primitivo ('A', 'B', etc.) para que calce con la columna char()
                        it[QuestionsTable.correctOption] = preguntaIA.correctOption.first()
                    }[QuestionsTable.id]
                }

                // ✅ 4. Ahora el .copy() funcionará perfecto porque sabe que es tu Data Class
                val respuestaFinal = preguntaIA.copy(id = questionIdInserted)
                call.respond(HttpStatusCode.OK, respuestaFinal)

            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al generar la trivia con IA: ${e.localizedMessage}"))
            }
        }
    }
}