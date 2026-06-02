package cl.jlopezr.server

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
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
import org.jetbrains.exposed.sql.javatime.timestamp // ✅ Importación necesaria para fechas
import java.time.Instant // ✅ Importación para la hora actual

// 1. Mapeo de la tabla corregido
object UsersTable : Table("users") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 100)
    val email = varchar("email", 100).uniqueIndex()
    val password = varchar("password", 100)
    val phone = varchar("phone", 20)

    // ✅ CAMBIO: Ahora es tipo timestamp para que Postgres no proteste
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class LoginRequest(
    @SerialName("email") val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String
)

@Serializable
data class UserRegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val phone: String
)

fun main() {
    // 2. CONEXIÓN A LA BASE DE DATOS
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
                // ENDPOINT LOGIN
                post("/login") {
                    try {
                        val credentials = call.receive<LoginRequest>()
                        println("Intentando login para: ${credentials.email}")
                        if (checkInDatabase(credentials)) {
                            call.respond(HttpStatusCode.OK, LoginResponse(true, "Acceso concedido"))
                        } else {
                            call.respond(HttpStatusCode.Unauthorized, LoginResponse(false, "Credenciales incorrectas"))
                        }
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, LoginResponse(false, "Error: ${e.message}"))
                    }
                }

                // ENDPOINT REGISTER
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
                                    // ✅ CAMBIO: Usamos Instant.now() para enviar una FECHA y no un número
                                    it[createdAt] = Instant.now()
                                }
                            }
                            call.respond(HttpStatusCode.Created, LoginResponse(true, "Usuario creado exitosamente"))
                        }
                    } catch (e: Exception) {
                        println("Error en registro: ${e.message}")
                        call.respond(HttpStatusCode.BadRequest, LoginResponse(false, "Error: ${e.message}"))
                    }
                }
            }
        }
    }.start(wait = true)
}

fun checkInDatabase(credentials: LoginRequest): Boolean {
    return transaction {
        UsersTable.selectAll()
            .where { (UsersTable.email eq credentials.email) and (UsersTable.password eq credentials.password) }
            .count() > 0
    }
}