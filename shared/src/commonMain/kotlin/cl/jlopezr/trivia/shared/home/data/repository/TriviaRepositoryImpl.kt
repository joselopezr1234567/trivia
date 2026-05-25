package cl.jlopezr.trivia.home.data.repository

import cl.jlopezr.trivia.home.domain.model.Question
import cl.jlopezr.trivia.home.domain.repository.TriviaRepository
import kotlinx.coroutines.delay

class TriviaRepositoryImpl : TriviaRepository {

    override suspend fun generateQuestions(category: String, difficulty: String): Result<List<Question>> {
        return try {
            // TODO: Aquí integrarás tu cliente de OpenAI o Ktor
            // Por ahora simulamos una carga de red
            delay(2000)

            val mockQuestions = listOf(
                Question(
                    id = "1",
                    statement = "¿Cuál es el lenguaje principal de KMP?",
                    options = listOf("Java", "Kotlin", "Swift", "Dart"),
                    correctAnswerIndex = 1,
                    explanation = "Kotlin es el lenguaje core de Kotlin Multiplatform."
                )
            )
            Result.success(mockQuestions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}