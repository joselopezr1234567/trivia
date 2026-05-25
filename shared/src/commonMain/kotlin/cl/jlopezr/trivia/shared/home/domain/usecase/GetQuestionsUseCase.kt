package cl.jlopezr.trivia.home.domain.usecase

import cl.jlopezr.trivia.home.domain.model.Question
import cl.jlopezr.trivia.home.domain.repository.TriviaRepository

class GetQuestionsUseCase(private val repository: TriviaRepository) {
    suspend operator fun invoke(category: String, difficulty: String): Result<List<Question>> {
        // Aquí podrías agregar lógica extra, como limpiar el texto de la categoría
        val cleanCategory = category.trim().lowercase()
        return repository.generateQuestions(cleanCategory, difficulty)
    }
}