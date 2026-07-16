package cl.jlopezr.server

import cl.jlopezr.trivia.core.network.model.RankingItem
import cl.jlopezr.trivia.shared.core.network.model.TriviaRequest
import cl.jlopezr.trivia.shared.core.network.model.TriviaResponse
import cl.jlopezr.trivia.shared.core.network.model.UserLoginRequest
import cl.jlopezr.trivia.shared.core.network.model.UserRegisterRequest
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Message
import com.twilio.type.PhoneNumber
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

// 1. Mapeo de Tablas
object UsersTable : Table("users") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 100)
    val email = varchar("email", 100).uniqueIndex()
    val password = varchar("password", 100)
    val phone = varchar("phone", 20)
    val createdAt = timestamp("created_at")
    val verificationCode = varchar("verification_code", 10).nullable() // 🔥 Agregado
    override val primaryKey = PrimaryKey(id)
}

// ... (en los modelos @Serializable)
@Serializable
data class ForgotPasswordRequest(val phone: String)

@Serializable
data class ResetPasswordRequest(
    val phone: String,
    val code: String,
    val newPassword: String
)

object UserPointsTable : Table("user_points") {
    val id = integer("id").autoIncrement()
    val userEmail = varchar("user_id", 100)
    val totalPoints = integer("total_points")
    val updatedAt = timestamp("updated_at")
    override val primaryKey = PrimaryKey(id)
}

object UserLevelsTable : Table("user_levels") {
    val id = integer("id").autoIncrement()
    val userEmail = varchar("user_id", 100)
    val currentLevel = integer("current_level")
    val updatedAt = timestamp("updated_at")
    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class LoginResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String
)

@Serializable
data class UserProgressRequest(
    val email: String,
    val points: Int,
    val level: Int
)

fun main() {
    val apiKey = System.getenv("OPENAI_API_KEY")
        ?: throw Exception("ERROR: No se encontró la variable de entorno OPENAI_API_KEY")

    val openAI = OpenAI(apiKey)

    Database.connect(
        url = "jdbc:postgresql://localhost:5432/trivia_db",
        driver = "org.postgresql.Driver",
        user = "macbook",
        password = ""
    )

    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }

        routing {
            // --- RUTAS DE AUTENTICACIÓN ---
            route("/auth") {
                post("/login") {
                    try {
                        val credentials = call.receive<UserLoginRequest>()
                        println("LOG [LOGIN]: Intentando acceso para ${credentials.email}")
                        if (checkInDatabase(credentials)) {
                            call.respond(HttpStatusCode.OK, LoginResponse(true, "Acceso concedido"))
                        } else {
                            call.respond(HttpStatusCode.Unauthorized, LoginResponse(false, "Credenciales incorrectas"))
                        }
                    } catch (e: Exception) {
                        println("ERROR [LOGIN]: ${e.message}")
                        call.respond(HttpStatusCode.BadRequest, LoginResponse(false, "Error: ${e.message}"))
                    }
                }

                post("/forgot-password") {
                    try {
                        val req = call.receive<ForgotPasswordRequest>()
                        println("LOG [FORGOT]: Validando teléfono ${req.phone}")
                        
                        val code = (100000..999999).random().toString()
                        
                        val updated = transaction {
                            UsersTable.update({ UsersTable.phone eq req.phone }) {
                                it[verificationCode] = code
                            }
                        }
                        
                        if (updated > 0) {
                            println("LOG [FORGOT]: Código generado para ${req.phone}: $code. Enviando SMS real...")
                            
                            // --- INTEGRACIÓN REAL CON TWILIO (Usando variables de entorno) ---
                            try {
                                val accountSid = System.getenv("TWILIO_ACCOUNT_SID") ?: throw Exception("Falta TWILIO_ACCOUNT_SID")
                                val authToken = System.getenv("TWILIO_AUTH_TOKEN") ?: throw Exception("Falta TWILIO_AUTH_TOKEN")
                                val fromPhone = System.getenv("TWILIO_PHONE_NUMBER") ?: throw Exception("Falta TWILIO_PHONE_NUMBER")

                                Twilio.init(accountSid, authToken)
                                Message.creator(
                                    PhoneNumber(req.phone),
                                    PhoneNumber(fromPhone),
                                    "Tu código de recuperación para TRIV-IA es: $code"
                                ).create()
                                
                                println("✅ SMS enviado con éxito a ${req.phone}")
                                call.respond(HttpStatusCode.OK, LoginResponse(true, "Código enviado por SMS"))
                            } catch (smsError: Exception) {
                                println("❌ ERROR AL ENVIAR SMS: ${smsError.message}")
                                // Aun si falla el SMS, el código existe en la DB (para pruebas)
                                call.respond(HttpStatusCode.OK, LoginResponse(true, "Código generado (Error en envío)"))
                            }
                        } else {
                            call.respond(HttpStatusCode.NotFound, LoginResponse(false, "Teléfono no registrado"))
                        }
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, LoginResponse(false, "Error: ${e.message}"))
                    }
                }

                post("/reset-password") {
                    try {
                        val req = call.receive<ResetPasswordRequest>()
                        println("LOG [RESET]: Intentando reset para ${req.phone} con código ${req.code}")
                        
                        val success = transaction {
                            val user = UsersTable.selectAll()
                                .where { (UsersTable.phone eq req.phone) and (UsersTable.verificationCode eq req.code) }
                                .singleOrNull()
                                
                            if (user != null) {
                                UsersTable.update({ UsersTable.phone eq req.phone }) {
                                    it[password] = req.newPassword
                                    it[verificationCode] = null // Limpiar código tras uso
                                }
                                true
                            } else false
                        }
                        
                        if (success) {
                            call.respond(HttpStatusCode.OK, LoginResponse(true, "Contraseña actualizada"))
                        } else {
                            call.respond(HttpStatusCode.Unauthorized, LoginResponse(false, "Código inválido"))
                        }
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, LoginResponse(false, "Error: ${e.message}"))
                    }
                }

                post("/register") {
                    try {
                        val user = call.receive<UserRegisterRequest>()
                        println("LOG [REGISTER]: Registrando a ${user.email}")

                        val result = transaction {
                            // 1. Verificar si ya existe el email
                            val exists = UsersTable.selectAll().where { UsersTable.email eq user.email }.count() > 0
                            if (exists) return@transaction "Email ya registrado"

                            // 2. Insertar usuario
                            UsersTable.insert {
                                it[username] = user.username
                                it[email] = user.email
                                it[password] = user.password
                                it[phone] = user.phone ?: ""
                                it[createdAt] = Instant.now()
                            }

                            // 3. Inicializar puntos y nivel
                            UserPointsTable.insert {
                                it[userEmail] = user.email
                                it[totalPoints] = 0
                                it[updatedAt] = Instant.now()
                            }
                            UserLevelsTable.insert {
                                it[userEmail] = user.email
                                it[currentLevel] = 1
                                it[updatedAt] = Instant.now()
                            }

                            null // Éxito
                        }

                        if (result == null) {
                            call.respond(HttpStatusCode.Created, LoginResponse(true, "Usuario creado exitosamente"))
                        } else {
                            call.respond(HttpStatusCode.Conflict, LoginResponse(false, result))
                        }
                    } catch (e: Exception) {
                        println("ERROR [REGISTER]: ${e.message}")
                        call.respond(HttpStatusCode.BadRequest, LoginResponse(false, "Error: ${e.message}"))
                    }
                }
            }

            // --- RUTAS DE PROGRESO ---
            route("/user") {
                get("/progress/{email}") {
                    val email = call.parameters["email"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    println("LOG [GET PROGRESS]: Consultando puntos para -> $email")
                    try {
                        val progress = transaction {
                            val p = UserPointsTable.selectAll().where { UserPointsTable.userEmail eq email }
                                .map { it[UserPointsTable.totalPoints] }.singleOrNull() ?: 0
                            val l = UserLevelsTable.selectAll().where { UserLevelsTable.userEmail eq email }
                                .map { it[UserLevelsTable.currentLevel] }.singleOrNull() ?: 1
                            UserProgressRequest(email, p, l)
                        }
                        println("LOG [GET PROGRESS]: Datos encontrados -> Puntos: ${progress.points}, Nivel: ${progress.level}")
                        call.respond(HttpStatusCode.OK, progress)
                    } catch (e: Exception) {
                        println("ERROR [GET PROGRESS]: ${e.message}")
                        call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
                    }
                }

                post("/progress/update") {
                    println("LOG [UPDATE PROGRESS]: --- SOLICITUD RECIBIDA ---")
                    try {
                        val req = call.receive<UserProgressRequest>()
                        println("LOG [UPDATE PROGRESS]: Datos recibidos -> Email: ${req.email}, Puntos: ${req.points}, Nivel: ${req.level}")

                        transaction {
                            // Sincronizar Puntos
                            val hasPoints = UserPointsTable.selectAll().where { UserPointsTable.userEmail eq req.email }.count() > 0
                            if (hasPoints) {
                                println("LOG [SQL]: Actualizando puntos existentes para ${req.email}")
                                UserPointsTable.update({ UserPointsTable.userEmail eq req.email }) {
                                    it[totalPoints] = req.points
                                    it[updatedAt] = Instant.now()
                                }
                            } else {
                                println("LOG [SQL]: Insertando nuevos puntos para ${req.email}")
                                UserPointsTable.insert {
                                    it[userEmail] = req.email
                                    it[totalPoints] = req.points
                                    it[updatedAt] = Instant.now()
                                }
                            }

                            // Sincronizar Nivel
                            val hasLevel = UserLevelsTable.selectAll().where { UserLevelsTable.userEmail eq req.email }.count() > 0
                            if (hasLevel) {
                                UserLevelsTable.update({ UserLevelsTable.userEmail eq req.email }) {
                                    it[currentLevel] = req.level
                                    it[updatedAt] = Instant.now()
                                }
                            } else {
                                UserLevelsTable.insert {
                                    it[userEmail] = req.email
                                    it[currentLevel] = req.level
                                    it[updatedAt] = Instant.now()
                                }
                            }
                        }
                        println("LOG [UPDATE PROGRESS]: ¡Guardado exitoso en Postgres!")
                        call.respond(HttpStatusCode.OK, mapOf("status" to "success"))
                    } catch (e: Exception) {
                        println("ERROR [UPDATE PROGRESS]: Error crítico al guardar -> ${e.message}")
                        e.printStackTrace()
                        call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
                    }
                }
            }

            // --- RUTA DE TRIVIA ---
            route("/trivia") {
                post("/generate") {
                    try {
                        val request = call.receive<TriviaRequest>()
                        println("LOG [TRIVIA]: Generando pregunta para tema: ${request.topic}")

                        val historyBlock = if (!request.history.isNullOrEmpty()) {
                            "\nPROHIBIDO REPETIR ESTAS PREGUNTAS: \n- ${request.history?.joinToString("\n- ")}"
                        } else ""

                        // LLAMADA A OPENAI
                        val chatCompletion = openAI.chatCompletion(
                            ChatCompletionRequest(
                                model = ModelId("gpt-3.5-turbo"),
                                messages = listOf(
                                    ChatMessage(
                                        role = ChatRole.System,
                                        content = """
                                            Eres un generador de trivias experto. Responde UNICAMENTE con JSON.
                                            FORMATO: {"question": "...", "options": ["...", "...", "...", "..."], "correctIndex": 0, "explanation": "..."}
                                            CRITICO: El campo "correctIndex" DEBE ser aleatorio entre 0 y 3. No pongas siempre la respuesta correcta en la misma posicion.
                                            CRITICO: El campo "explanation" debe ser una breve explicacion de por qué esa es la respuesta correcta.
                                            $historyBlock
                                        """.trimIndent()
                                    ),
                                    ChatMessage(role = ChatRole.User, content = "Tema: ${request.topic}. Dificultad: ${request.difficulty}.")
                                ),
                                temperature = 0.8
                            )
                        )

                        val rawResult = chatCompletion.choices.first().message.content ?: "{}"
                        val cleanJson = rawResult.trim().removePrefix("```json").removeSuffix("```").trim()

                        // IMPORTANTE: Aquí es donde enviamos la respuesta para evitar el 404
                        val triviaResponse = Json.decodeFromString<TriviaResponse>(cleanJson)
                        println("LOG [TRIVIA]: Enviando respuesta exitosa")
                        call.respond(HttpStatusCode.OK, triviaResponse)

                    } catch (e: Exception) {
                        println("ERROR [TRIVIA]: ${e.message}")
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Unknown error")))
                    }
                }
            }

            // --- RUTA DE RANKING ---
            route("/ranking") {
                get {
                    println("LOG [RANKING]: Consultando ranking global")
                    try {
                        val ranking = transaction {
                            // Usamos leftJoin para que aparezcan todos los usuarios aunque no tengan puntos aún
                            UsersTable
                                .leftJoin(UserPointsTable, { email }, { userEmail })
                                .leftJoin(UserLevelsTable, { UsersTable.email }, { UserLevelsTable.userEmail })
                                .slice(
                                    UsersTable.username,
                                    UserPointsTable.totalPoints,
                                    UserLevelsTable.currentLevel
                                )
                                .selectAll()
                                .orderBy(UserPointsTable.totalPoints to SortOrder.DESC_NULLS_LAST)
                                .limit(20)
                                .mapIndexed { index, row ->
                                    RankingItem(
                                        username = row[UsersTable.username],
                                        score = row.getOrNull(UserPointsTable.totalPoints) ?: 0,
                                        level = row.getOrNull(UserLevelsTable.currentLevel) ?: 1, // 🔥 Ahora enviamos el nivel real
                                        position = index + 1
                                    )
                                }
                        }
                        call.respond(HttpStatusCode.OK, ranking)
                    } catch (e: Exception) {
                        println("ERROR [RANKING]: ${e.message}")
                        e.printStackTrace()
                        call.respond(HttpStatusCode.InternalServerError, emptyList<RankingItem>())
                    }
                }
            }
        }
    }.start(wait = true)
}

fun checkInDatabase(credentials: UserLoginRequest): Boolean {
    return transaction {
        UsersTable.selectAll()
            .where { (UsersTable.email eq credentials.email) and (UsersTable.password eq credentials.password) }
            .count() > 0
    }
}