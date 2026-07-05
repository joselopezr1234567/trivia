package cl.jlopezr.server

import cl.jlopezr.trivia.shared.core.network.model.*
import cl.jlopezr.trivia.core.network.model.RankingItem
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

// 1. Mapeo de Tablas
object UsersTable : Table("users") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 100)
    val email = varchar("email", 100).uniqueIndex()
    val password = varchar("password", 100)
    val phone = varchar("phone", 20)
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

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
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
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
                                            FORMATO: {"question": "...", "options": ["...", "...", "...", "..."], "correctIndex": 0}
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