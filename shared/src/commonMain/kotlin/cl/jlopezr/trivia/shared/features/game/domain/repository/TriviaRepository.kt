package cl.jlopezr.trivia.shared.features.game.domain.repository


import cl.jlopezr.trivia.shared.core.network.TriviaService
import cl.jlopezr.trivia.shared.core.network.model.TriviaResponse

class TriviaRepository(
    // Inyectamos el servicio que ya creaste
    private val service: TriviaService = TriviaService()
) {
    /**
     * Obtiene una nueva pregunta desde el servidor Ktor.
     * Retorna un Result de Kotlin para manejar éxito o falla.
     */
    suspend fun getNewQuestion(topic: String): Result<TriviaResponse> {
        return try {
            val response = service.generateTrivia(topic)
            if (response != null) {
                Result.success(response)
            } else {
                Result.failure(Exception("El servidor no respondió correctamente"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}