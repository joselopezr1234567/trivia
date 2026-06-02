import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String? = null,
    val message: String? = null // Para capturar mensajes de error del servidor
)
