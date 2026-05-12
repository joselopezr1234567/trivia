package cl.jlopezr.trivia.login.domain.model

data class RegisterUser(
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String
)