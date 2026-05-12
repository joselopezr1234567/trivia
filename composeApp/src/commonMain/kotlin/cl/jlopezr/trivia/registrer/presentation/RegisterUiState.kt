package cl.jlopezr.trivia.registrer.presentation

data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRegistered: Boolean = false,
    val showOtpDialog: Boolean = false
)



