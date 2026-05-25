package cl.jlopezr.trivia.game.data

// 🛠️ Cambia el import por este:
import cl.jlopezr.trivia.core.network.model.QuestionResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class TriviaRemoteDataSource(private val httpClient: HttpClient) {
    private val baseUrl = "http://10.0.2.2:8080"

    suspend fun generateTriviaQuestion(category: String, userId: Int): QuestionResponse {
        return httpClient.get("$baseUrl/trivia/generate") {
            parameter("category", category)
            parameter("userId", userId)
            contentType(ContentType.Application.Json)
        }.body()
    }
}