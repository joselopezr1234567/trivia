package cl.jlopezr.trivia.shared.login.domain.repository

import cl.jlopezr.trivia.login.domain.model.RegisterUser


interface LoginRepository {
    suspend fun login(email: String, password: String): Result<RegisterUser>
}