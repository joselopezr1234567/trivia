package cl.jlopezr.trivia.game.domain

// ✅ Forzamos el uso del Result nativo de Kotlin y el DTO correcto
import cl.jlopezr.trivia.core.network.model.QuestionResponse


interface TriviaRepository {
    suspend fun getNewQuestion(category: String, userId: Int): Result<QuestionResponse>
}