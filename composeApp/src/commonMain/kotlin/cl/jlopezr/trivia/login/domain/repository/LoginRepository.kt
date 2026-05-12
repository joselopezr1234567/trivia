package cl.jlopezr.trivia.login.domain.repository

import cl.jlopezr.trivia.login.domain.model.RegisterUser
import cl.jlopezr.trivia.registrer.presentation.RegisterUiState

interface LoginRepository {
    suspend fun login(email: String, password: String): Result<RegisterUser>
}