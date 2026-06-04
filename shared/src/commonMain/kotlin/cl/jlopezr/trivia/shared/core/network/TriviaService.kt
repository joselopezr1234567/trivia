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
     * He cambiado el nombre del parámetro de 'topic' a 'request' para que sea más claro.
     */
    suspend fun generateTrivia(request: TriviaRequest): TriviaResponse? {
        return try {
            val response = client.post("http://10.0.2.2:8080/trivia/generate") {
                contentType(ContentType.Application.Json)

                // ✅ CORRECTO: Pasamos el objeto 'request' directamente.
                // No hace falta poner setBody(TriviaRequest(...)) porque 'request' ya es de ese tipo.
                setBody(request)
            }
            response.body<TriviaResponse>()
        } catch (e: Exception) {
            println("DEBUG: Error en red: ${e.message}")
            null
        }
    }
}
