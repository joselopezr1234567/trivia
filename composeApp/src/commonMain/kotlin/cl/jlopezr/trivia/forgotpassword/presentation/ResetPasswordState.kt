package cl.jlopezr.trivia.forgotpassword.presentation

data class ResetPasswordState(
    val code: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val isPasswordVisible: Boolean = false
)