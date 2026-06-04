package cl.jlopezr.trivia.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.jlopezr.trivia.home.domain.usecase.GetQuestionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de la pantalla de Home.
 * Se incluyen totalScore y currentLevel para que sean persistentes en la UI.
 */


class HomeViewModel(
    private val getQuestionsUseCase: GetQuestionsUseCase // Inyectado
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Al iniciar el ViewModel, cargamos el progreso guardado
        loadUserProgress()
    }

    /**
     * Carga los puntos y el nivel desde la persistencia (DB o Repositorio).
     * Por ahora inicializamos valores, pero aquí llamarías a tu base de datos.
     */
    fun loadUserProgress() {
        viewModelScope.launch {
            // Aquí llamarías a: repository.getUserProgress()
            // Simulamos la carga de datos persistentes:
            _uiState.update {
                it.copy(
                    // Estos valores vendrán de tu base de datos local o remota
                    totalScore = it.totalScore,
                    currentLevel = it.currentLevel
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
     * Actualiza el estado del Home con los nuevos puntos obtenidos tras una partida.
     */
    fun refreshProgress(newScore: Int, newLevel: String) {
        _uiState.update {
            it.copy(totalScore = newScore, currentLevel = newLevel)
        }
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
                // Pasamos la categoría y dificultad a la navegación
                onSuccess(category, difficulty)
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = "Error: ${error.message}") }
            }
        }
    }
}