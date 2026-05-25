package cl.jlopezr.trivia.login.domain

import cl.jlopezr.trivia.core.network.model.UserRegisterRequest
import cl.jlopezr.trivia.shared.core.network.model.UserProfileResponse

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<cl.jlopezr.trivia.core.network.model.UserProfileResponse>
    suspend fun register(user: UserRegisterRequest): Result<cl.jlopezr.trivia.core.network.model.UserProfileResponse>
}