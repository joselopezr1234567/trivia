package cl.jlopezr.trivia.game.presentation

// 1. IMPORTANTE: Importar el repositorio desde el paquete correcto del módulo SHARED
import cl.jlopezr.trivia.shared.features.game.domain.repository.TriviaRepository
// 2. IMPORTANTE: Usar TriviaResponse que es el modelo que viene del servidor
import cl.jlopezr.trivia.shared.core.network.model.TriviaResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


sealed interface TriviaUiState {
    object Loading : TriviaUiState
    // Cambiado QuestionResponse -> TriviaResponse
    data class Success(val question: TriviaResponse) : TriviaUiState
    data class Error(val message: String) : TriviaUiState
}

class TriviaViewModel(
    private val repository: TriviaRepository,
    // En Compose Multiplatform, Dispatchers.Main está bien,
    // pero para el scope suele usarse viewModelScope si usas una librería de VM,
    // o este scope manual está bien para empezar.
    private val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {

    private val _uiState = MutableStateFlow<TriviaUiState>(TriviaUiState.Loading)
    val uiState: StateFlow<TriviaUiState> = _uiState.asStateFlow()

    fun loadQuestion(category: String) {
        viewModelScope.launch {
            _uiState.value = TriviaUiState.Loading

            // Llamamos al repo (quitamos userId si el servidor no lo pide aún)
            repository.getNewQuestion(category)
                .onSuccess { question ->
                    _uiState.value = TriviaUiState.Success(question)
                }
                .onFailure { error ->
                    _uiState.value = TriviaUiState.Error(error.message ?: "Error al cargar la pregunta")
                }
        }
    }
}