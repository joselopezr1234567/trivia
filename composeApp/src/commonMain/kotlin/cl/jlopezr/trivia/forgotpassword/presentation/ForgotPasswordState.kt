package cl.jlopezr.trivia.forgotpassword.presentation

data class ForgotPasswordState(
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isCodeSent: Boolean = false
)