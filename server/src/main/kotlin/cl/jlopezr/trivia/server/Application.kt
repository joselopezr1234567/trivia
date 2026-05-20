package cl.jlopezr.trivia.server

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.cors.routing.*
import kotlinx.serialization.json.Json
import cl.jlopezr.trivia.server.registerAuthRoutes

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {

    // 🛠️ 1. INSTALAR EL SOPORTE PARA JSON (Resuelve el error 406)
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    // 🌐 2. CONFIGURACIÓN DE CORS (Opcional, pero ideal para desarrollo)
    install(CORS) {
        anyHost()
        allowHeader(io.ktor.http.HttpHeaders.ContentType)
        allowHeader(io.ktor.http.HttpHeaders.Authorization)
    }

    // 🗄️ 3. Inicializar Base de Datos
    DatabaseFactory.init()

    // 👤 4. Instanciar Repositorio
    val userRepository: UserRepository = UserRepositoryImpl()

    // 🚀 5. Registro de Enrutamiento
    routing {
        get("/") {
            call.respondText("¡Servidor de Trivia Ktor corriendo con éxito!")
        }

        registerAuthRoutes(userRepository)
    }
}