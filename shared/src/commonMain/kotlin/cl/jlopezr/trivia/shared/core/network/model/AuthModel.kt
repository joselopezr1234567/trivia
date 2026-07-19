package cl.jlopezr.trivia.shared.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginServerResponse(
    val success: Boolean,
    val message: String
)

@Serializable
data class UserLoginRequest(
    @SerialName("email")
    val email: String,
    val password: String
)

@Serializable
data class UserRegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val phone: String,
    val inviteCode: String? = null // 🔥 Agregado
)

@Serializable
data class InviteInfoResponse(
    val inviteCode: String,
    val invitesSentThisWeek: Int,
    val maxInvitesPerWeek: Int = 15
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

// shared/src/commonMain/kotlin/.../TriviaRequest.kt
@Serializable
data class TriviaRequest(
    val topic: String,
    val difficulty: String,
    val history: List<String>?,
    val language: String = "es" // Nuevo campo para el idioma
)

@Serializable
data class TriviaResponse(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String = "" // 🔥 Agregamos explicación
)