package cl.jlopezr.trivia.home.presentation

data class HomeUiState(
    val category: String = "",
    val difficulty: String = "Básico",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)