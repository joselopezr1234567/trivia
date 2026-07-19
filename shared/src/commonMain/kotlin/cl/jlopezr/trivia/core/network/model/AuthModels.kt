package cl.jlopezr.trivia.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserLoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class UserRegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    val inviteCode: String? = null // 🔥 Agregamos el código de invitación
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
@Serializable
data class QuestionResponse(
    val id: Int? = null,
    val category: String,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String
)
@Serializable
data class RankingItem(
    @SerialName("username") val username: String,
    @SerialName("score") val score: Int,
    @SerialName("level") val level: Int = 1, // 🔥 Agregamos nivel
    @SerialName("position") val position: Int? = null
)