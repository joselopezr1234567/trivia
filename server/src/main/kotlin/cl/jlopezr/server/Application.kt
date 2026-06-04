package cl.jlopezr.server

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import cl.jlopezr.trivia.shared.core.network.model.*

// 1. Mapeo de la tabla corregido
object UsersTable : Table("users") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 100)
    val email = varchar("email", 100).uniqueIndex()
    val password = varchar("password", 100)
    val phone = varchar("phone", 20)
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class LoginResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String
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
            route("/auth") {
                post("/login") {
                    try {
                        val credentials = call.receive<UserLoginRequest>()
                        if (checkInDatabase(credentials)) {
                            call.respond(HttpStatusCode.OK, LoginResponse(true, "Acceso concedido"))
                        } else {
                            call.respond(HttpStatusCode.Unauthorized, LoginResponse(false, "Credenciales incorrectas"))
                        }
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, LoginResponse(false, "Error: ${e.message}"))
                    }
                }

                post("/register") {
                    try {
                        val signup = call.receive<UserRegisterRequest>()
                        val alreadyExists = transaction {
                            UsersTable.selectAll().where { UsersTable.email eq signup.email }.count() > 0
                        }

                        if (alreadyExists) {
                            call.respond(HttpStatusCode.Conflict, LoginResponse(false, "El correo ya está registrado"))
                        } else {
                            transaction {
                                UsersTable.insert {
                                    it[username] = signup.username
                                    it[email] = signup.email
                                    it[password] = signup.password
                                    it[phone] = signup.phone
                                    it[createdAt] = Instant.now()
                                }
                            }
                            call.respond(HttpStatusCode.Created, LoginResponse(true, "Usuario creado exitosamente"))
                        }
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, LoginResponse(false, "Error: ${e.message}"))
                    }
                }
            }

            route("/trivia") {
                post("/generate") {
                    try {
                        val request = call.receive<TriviaRequest>()

                        val historyBlock = if (request.history.isNullOrEmpty() == false) {
                            "\nPROHIBIDO REPETIR ESTAS PREGUNTAS: \n- ${request.history?.joinToString("\n- ")}"
                        } else ""

                        val chatCompletion = openAI.chatCompletion(
                            ChatCompletionRequest(
                                model = ModelId("gpt-3.5-turbo"),
                                messages = listOf(
                                    ChatMessage(
                                        role = ChatRole.System,
                                        content = """
                                            Eres un generador de trivias experto. 
                                            Responde UNICAMENTE con JSON.
                                            REGLAS DE DIFICULTAD:
                                            - Básico: Preguntas muy simples.
                                            - Intermedio: Conocimiento específico.
                                            - Difícil: Solo para expertos.
                                            
                                            FORMATO: {"question": "...", "options": ["...", "...", "...", "..."], "correctIndex": 0}
                                            $historyBlock
                                        """.trimIndent()
                                    ),
                                    ChatMessage(
                                        role = ChatRole.User,
                                        content = "Tema: ${request.topic}. DIFICULTAD OBLIGATORIA: ${request.difficulty}."
                                    )
                                ),
                                temperature = 0.8
                            )
                        )

                        val rawResult = chatCompletion.choices.first().message.content ?: "{}"
                        val cleanJson = rawResult.trim()
                            .removePrefix("```json")
                            .removeSuffix("```")
                            .trim()

                        val triviaResponse = Json.decodeFromString<TriviaResponse>(cleanJson)
                        call.respond(HttpStatusCode.OK, triviaResponse)

                    } catch (e: Exception) {
                        println("Error: ${e.message}")
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error IA: ${e.message}"))
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