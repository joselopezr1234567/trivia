import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    // Tu servidor espera "email", no "username" según tus logs anteriores
    @SerialName("email") val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    // ESTA ES LA CLAVE: Tu servidor envía "success", hay que recibirlo
    @SerialName("success") val success: Boolean = false,
    val message: String? = null,
    val token: String? = null // Lo dejamos opcional por si lo usas después
)