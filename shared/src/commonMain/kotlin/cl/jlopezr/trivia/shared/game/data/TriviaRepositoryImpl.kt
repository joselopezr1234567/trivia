package cl.jlopezr.trivia.game.data

import cl.jlopezr.trivia.game.domain.TriviaRepository
import cl.jlopezr.trivia.core.network.model.QuestionResponse


class TriviaRepositoryImpl(
    private val remoteDataSource: TriviaRemoteDataSource
) : TriviaRepository {

    // ✅ Aseguramos que devuelva el mismo kotlin.Result
    override suspend fun getNewQuestion(category: String, userId: Int): kotlin.Result<QuestionResponse> {
        return runCatching {
            remoteDataSource.generateTriviaQuestion(category, userId)
        }
    }
}