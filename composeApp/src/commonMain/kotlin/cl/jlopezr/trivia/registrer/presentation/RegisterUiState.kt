package cl.jlopezr.trivia.registrer.presentation

data class RegisterUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val isRegistered: Boolean = false
)



