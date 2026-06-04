package cl.jlopezr.trivia.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.jlopezr.trivia.home.domain.usecase.GetQuestionsUseCase
import cl.jlopezr.trivia.core.data.ProgressStorage // Importamos el storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch



class HomeViewModel(
    private val getQuestionsUseCase: GetQuestionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserProgress()
    }

    /**
     * Lee los puntos y el nivel desde el ProgressStorage.
     * Esta función se llama desde el Navigation mediante LaunchedEffect.
     */
    fun loadUserProgress() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    totalScore = ProgressStorage.totalScore,
                    currentLevel = ProgressStorage.currentLevel // Ahora ambos son Int
                )
            }
        }
    }

    fun onCategoryChanged(newCategory: String) {
        _uiState.update { it.copy(category = newCategory, errorMessage = null) }
    }

    fun onDifficultySelected(newDiff: String) {
        _uiState.update { it.copy(difficulty = newDiff) }
    }

    /**
     * Permite actualizar manualmente si fuera necesario.
     */
    fun refreshProgress(newScore: Int, newLevel: Int) {
        ProgressStorage.totalScore = newScore
        ProgressStorage.currentLevel = newLevel
        loadUserProgress()
    }

    fun generateTrivia(onSuccess: (String, String) -> Unit) {
        val category = _uiState.value.category
        val difficulty = _uiState.value.difficulty

        if (category.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Escribe una categoría") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = getQuestionsUseCase(category, difficulty)

            _uiState.update { it.copy(isLoading = false) }

            result.onSuccess {
                onSuccess(category, difficulty)
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = "Error: ${error.message}") }
            }
        }
    }
}