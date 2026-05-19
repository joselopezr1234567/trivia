package cl.jlopezr.trivia.server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.insert

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()

    routing {
        get("/") {
            call.respondText("¡Servidor de Trivia Ktor escuchando de forma nativa!")
        }

        // 🚀 ENDPOINT DE PRUEBA
        get("/test-user") {
            try {
                DatabaseFactory.dbQuery {
                    // 1. Insertar Usuario
                    val userIdInserted = UsersTable.insert {
                        it[username] = "JLdeveloper"
                        it[email] = "contacto@jlopezr.cl"
                        it[password] = "super_password_encriptado_123"
                        it[phone] = "+56912345678"
                    } get UsersTable.id

                    // 2. Insertar sus puntos iniciales vinculados a su ID
                    UserPointsTable.insert {
                        it[userId] = userIdInserted
                        it[totalPoints] = 0
                    }

                    // 3. Insertar su nivel inicial vinculado a su ID
                    UserLevelsTable.insert {
                        it[userId] = userIdInserted
                        it[currentLevel] = 1
                        it[currentExperience] = 0
                    }
                }
                call.respondText("✅ ¡Prueba Exitosa! Usuario 'JLdeveloper' e historiales creados en PostgreSQL 16.")
            } catch (e: Exception) {
                call.respondText("❌ Error en la prueba: ${e.localizedMessage}")
            }
        }
    }
}