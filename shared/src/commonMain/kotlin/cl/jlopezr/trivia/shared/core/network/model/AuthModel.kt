package cl.jlopezr.trivia.shared.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class UserLoginRequest(val email: String, val password: String)

@Serializable
data class UserRegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val phone: String? = null
)

@Serializable
data class UserProfileResponse(
    val id: Int,
    val username: String,
    val email: String,
    val phone: String? = null,
    val totalPoints: Int = 0,
    val currentLevel: Int = 1,
    val currentExperience: Int = 0,
    val token: String = ""
)