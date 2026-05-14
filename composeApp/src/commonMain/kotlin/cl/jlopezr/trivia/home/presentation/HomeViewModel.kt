package cl.jlopezr.trivia.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.jlopezr.trivia.home.domain.usecase.GetQuestionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getQuestionsUseCase: GetQuestionsUseCase // Inyectado
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    fun onCategoryChanged(newCategory: String) = _uiState.update { it.copy(category = newCategory) }
    fun onDifficultySelected(newDiff: String) = _uiState.update { it.copy(difficulty = newDiff) }

    fun generateTrivia(onSuccess: (String, String) -> Unit) {
        val category = _uiState.value.category
        val difficulty = _uiState.value.difficulty

        if (category.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Escribe una categoría") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = getQuestionsUseCase(category, difficulty)

            _uiState.update { it.copy(isLoading = false) }

            result.onSuccess {
                // CORRECCIÓN AQUÍ: Debes pasar ambos parámetros a la lambda
                onSuccess(category, difficulty)
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = "Error: ${error.message}") }
            }
        }
    }
}