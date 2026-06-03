package cl.jlopezr.trivia.shared.core.network

import cl.jlopezr.trivia.shared.core.network.model.TriviaRequest
import cl.jlopezr.trivia.shared.core.network.model.TriviaResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class TriviaService {
    // Cliente de Ktor configurado para JSON
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    /**
     * Esta función llama a tu servidor Ktor.
     * Si usas el emulador de Android, localhost es 10.0.2.2
     */
    suspend fun generateTrivia(topic: String): TriviaResponse? {
        return try {
            // ✅ USAR 10.0.2.2 PARA EL EMULADOR
            val response = client.post("http://10.0.2.2:8080/trivia/generate") {
                contentType(ContentType.Application.Json)
                setBody(TriviaRequest(topic = topic))
            }
            response.body<TriviaResponse>()
        } catch (e: Exception) {
            println("DEBUG: Error en red: ${e.message}") // Esto saldrá en el Logcat de Android Studio
            null
        }
    }
}