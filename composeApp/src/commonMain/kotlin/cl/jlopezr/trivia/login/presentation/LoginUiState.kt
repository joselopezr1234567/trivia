package cl.jlopezr.trivia.login.presentation




data class LoginUiState (

    val email : String = "",
    val password : String = "",
    val isLoading : Boolean = false,
    val errorMessage : String? = null,
    val isLoginSuccess : Boolean = false,
    val isEmailValid: Boolean = true
)
