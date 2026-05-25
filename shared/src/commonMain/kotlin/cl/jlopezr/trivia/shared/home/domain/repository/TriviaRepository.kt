package cl.jlopezr.trivia.home.domain.repository

import cl.jlopezr.trivia.home.domain.model.Question

interface TriviaRepository {
    suspend fun generateQuestions(category: String, difficulty: String): Result<List<Question>>
}