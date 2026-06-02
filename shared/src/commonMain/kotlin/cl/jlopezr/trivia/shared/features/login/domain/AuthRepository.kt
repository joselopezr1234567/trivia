package cl.jlopezr.trivia.shared.features.login.domain

import cl.jlopezr.trivia.core.network.model.UserRegisterRequest

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<cl.jlopezr.trivia.core.network.model.UserProfileResponse>
    suspend fun register(user: UserRegisterRequest): Result<cl.jlopezr.trivia.core.network.model.UserProfileResponse>
}