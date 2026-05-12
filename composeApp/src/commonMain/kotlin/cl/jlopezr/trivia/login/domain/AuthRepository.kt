package cl.jlopezr.trivia.login.domain

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
}