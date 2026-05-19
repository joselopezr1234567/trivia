package cl.jlopezr.trivia.server

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // Inicializa la conexión local a PostgreSQL 16
    DatabaseFactory.init()

    // 🌐 Instala el soporte nativo para JSON
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true // Evita que el servidor explote si la app móvil envía campos de más
        })
    }

    // Instanciamos el repositorio bajo Clean Architecture
    val userRepository: UserRepository = UserRepositoryImpl()

    // Registramos las rutas pasándole el repositorio
    routing {
        registerAuthRoutes(userRepository)
    }
}