package cl.jlopezr.trivia.server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.deleteAll

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()

    routing {
        get("/") {
            call.respondText("¡Servidor de Trivia Ktor con Hashing Automatizado!")
        }

        get("/test-hash") {
            try {
                DatabaseFactory.dbQuery {
                    // Limpiamos registros anteriores para que no tire error de UNIQUE index al probar
                    UserLevelsTable.deleteAll()
                    UserPointsTable.deleteAll()
                    UsersTable.deleteAll()

                    // 1. Encriptamos la contraseña usando nuestro utilitario
                    val contrasenaOriginal = "mi_clave_secreta_123"
                    val contrasenaEncriptada = SecurityUtils.hashPassword(contrasenaOriginal)

                    // 2. Insertar Usuario con la contraseña protegida
                    val userIdInserted = UsersTable.insert {
                        it[username] = "JLdeveloper"
                        it[email] = "contacto@jlopezr.cl"
                        it[password] = contrasenaEncriptada // 🔒 Guardamos el Hash irreversible
                        it[phone] = "+56912345678"
                    } get UsersTable.id

                    // 3. Crear registros satélites iniciales en cascada
                    UserPointsTable.insert {
                        it[userId] = userIdInserted
                        it[totalPoints] = 100 // Le damos 100 puntos de bienvenida por probar
                    }
                    UserLevelsTable.insert {
                        it[userId] = userIdInserted
                        it[currentLevel] = 1
                    }
                }
                call.respondText("✅ Prueba Exitosa. Usuario registrado con contraseña encriptada en SHA-256.")
            } catch (e: Exception) {
                call.respondText("❌ Error: ${e.localizedMessage}")
            }
        }
    }
}