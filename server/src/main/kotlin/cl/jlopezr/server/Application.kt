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
import io.ktor.http.*
import kotlinx.serialization.SerialName
// Importaciones de la Base de Datos corregidas
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq // Necesario para 'eq'
import org.jetbrains.exposed.sql.transactions.transaction

// 1. Mapeo de la tabla (Ya confirmamos que existe en trivia_db)
object UsersTable : Table("users") {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 100).uniqueIndex()
    val password = varchar("password", 100)
    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class LoginRequest(
    @SerialName("email") val email: String,
    val password: String
)

@Serializable
data class LoginResponse(val success: Boolean, val message: String)

fun main() {
    // 2. CONEXIÓN A LA BASE DE DATOS TRIVIA_DB
    Database.connect(
        // Cambiado de 'postgres' a 'trivia_db' ✅
        url = "jdbc:postgresql://localhost:5432/trivia_db",
        driver = "org.postgresql.Driver",
        user = "macbook",
        password = "" // Postgres.app en local no suele pedir clave
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
                        val credentials = call.receive<LoginRequest>()
                        println("Intentando login en trivia_db para: ${credentials.email}")

                        if (checkInDatabase(credentials)) {
                            call.respond(HttpStatusCode.OK, LoginResponse(true, "Acceso concedido"))
                        } else {
                            call.respond(HttpStatusCode.Unauthorized, LoginResponse(false, "Credenciales incorrectas"))
                        }
                    } catch (e: Exception) {
                        println("Error en login: ${e.message}")
                        call.respond(HttpStatusCode.BadRequest, LoginResponse(false, "Error: ${e.message}"))
                    }
                }
            }
        }
    }.start(wait = true)
}

// 4. FUNCIÓN QUE BUSCA EN TU TABLA REAL
fun checkInDatabase(credentials: LoginRequest): Boolean {
    return transaction {
        // Usamos selectAll().where para versiones recientes de Exposed
        UsersTable.selectAll()
            .where { (UsersTable.email eq credentials.email) and (UsersTable.password eq credentials.password) }
            .count() > 0
    }
}