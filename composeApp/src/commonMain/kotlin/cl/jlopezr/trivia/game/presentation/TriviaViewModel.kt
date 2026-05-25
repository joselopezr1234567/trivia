package cl.jlopezr.trivia.game.presentation

import cl.jlopezr.trivia.game.domain.TriviaRepository
import cl.jlopezr.trivia.core.network.model.QuestionResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

sealed interface TriviaUiState {
    object Loading : TriviaUiState
    data class Success(val question: QuestionResponse) : TriviaUiState
    data class Error(val message: String) : TriviaUiState
}

class TriviaViewModel(
    private val repository: TriviaRepository,
    private val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {

    private val _uiState = MutableStateFlow<TriviaUiState>(TriviaUiState.Loading)
    val uiState: StateFlow<TriviaUiState> = _uiState.asStateFlow()

    fun loadQuestion(category: String, userId: Int) {
        viewModelScope.launch {
            _uiState.value = TriviaUiState.Loading
            repository.getNewQuestion(category, userId)
                .onSuccess { question ->
                    _uiState.value = TriviaUiState.Success(question)
                }
                .onFailure { error ->
                    // 🛠️ CORREGIDO: Cambiamos 'localizedMessage' por 'message' que es compatible con KMP
                    _uiState.value = TriviaUiState.Error(error.message ?: "Error al cargar la pregunta")
                }
        }
    }
}