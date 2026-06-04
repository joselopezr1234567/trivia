package cl.jlopezr.trivia.home.presentation

data class HomeUiState(
    val category: String = "",
    val difficulty: String = "Básico",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // --- AGREGA ESTAS DOS LÍNEAS ---
    val currentLevel: String = "Básico",
    val totalScore: Int = 0
)