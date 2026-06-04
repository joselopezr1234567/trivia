package cl.jlopezr.trivia.shared.features.game.domain.repository

import cl.jlopezr.trivia.shared.core.network.TriviaService
import cl.jlopezr.trivia.shared.core.network.model.TriviaRequest
import cl.jlopezr.trivia.shared.core.network.model.TriviaResponse

class TriviaRepository(
    // 1. Asegúrate de usar el nombre 'service' que es el que inyectamos
    private val service: TriviaService = TriviaService()
) {
    /**
     * Obtiene una nueva pregunta desde el servidor Ktor.
     */
    suspend fun getNewQuestion(request: TriviaRequest): Result<TriviaResponse> {
        return try {
            // 2. Cambiado 'apiService' por 'service'
            // 3. Llamamos a generateTrivia pasando el objeto request completo
            val response = service.generateTrivia(request)

            if (response != null) {
                Result.success(response)
            } else {
                Result.failure(Exception("El servidor devolvió una respuesta vacía"))
            }
        } catch (e: Exception) {
            // Especificamos el tipo para evitar el error de 'Result<Any>'
            Result.failure<TriviaResponse>(e)
        }
    }
}