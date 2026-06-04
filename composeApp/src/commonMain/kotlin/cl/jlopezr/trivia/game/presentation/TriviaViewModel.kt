package cl.jlopezr.trivia.game.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel // IMPORTANTE
import androidx.lifecycle.viewModelScope // IMPORTANTE
import cl.jlopezr.trivia.shared.features.game.domain.repository.TriviaRepository
import cl.jlopezr.trivia.shared.core.network.model.TriviaResponse
import cl.jlopezr.trivia.shared.core.network.model.TriviaRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

sealed interface TriviaUiState {
    object Loading : TriviaUiState
    data class Success(val question: TriviaResponse) : TriviaUiState
    data class Error(val message: String) : TriviaUiState
}

// Definimos los tipos de feedback visual
enum class FeedbackType { CORRECTO, INCORRECTO, SUBIO_NIVEL }

class TriviaViewModel(
    private val repository: TriviaRepository
) : ViewModel() { // Ahora hereda de ViewModel correctamente

    private val _uiState = MutableStateFlow<TriviaUiState>(TriviaUiState.Loading)
    val uiState: StateFlow<TriviaUiState> = _uiState.asStateFlow()

    // --- ESTADOS PARA ANIMACIÓN ---
    var showFeedback by mutableStateOf<FeedbackType?>(null)
        private set

    // --- LÓGICA DE JUEGO ---
    private val askedQuestions = mutableListOf<String>()

    var consecutiveCorrect by mutableStateOf(0)
        private set

    var totalScore by mutableStateOf(0)
        private set

    var currentLevel by mutableStateOf(1)
        private set


    /**
     * Carga una nueva pregunta desde el repositorio.
     */
    fun loadQuestion(category: String) {
        viewModelScope.launch {
            _uiState.value = TriviaUiState.Loading
            try {
                val request = TriviaRequest(
                    category,
                    currentLevel.toString(), // <--- CAMBIO: Agregamos .toString()
                    askedQuestions.toList()
                )

                repository.getNewQuestion(request)
                    .onSuccess { result ->
                        askedQuestions.add(result.question)
                        _uiState.value = TriviaUiState.Success(result)
                    }
                    .onFailure { error ->
                        _uiState.value = TriviaUiState.Error(error.message ?: "Error de red")
                    }
            } catch (e: Exception) {
                _uiState.value = TriviaUiState.Error("Error: ${e.message}")
            }
        }
    }

    /**
     * Procesa la respuesta con ANIMACIONES y PERSISTENCIA.
     */
    fun processAnswer(
        isCorrect: Boolean,
        category: String,
        onGameOver: () -> Unit
    ) {
        viewModelScope.launch {
            if (isCorrect) {
                // 1. Lógica de puntos
                val pointsToAdd = when {
                    currentLevel <= 2 -> 1      // Niveles 1-2: 1 punto
                    currentLevel <= 5 -> 2      // Niveles 3-5: 2 puntos
                    else -> 3                   // Nivel 6+: 3 puntos
                }
                totalScore += pointsToAdd
                consecutiveCorrect += 1

                // 2. Determinar si es acierto normal o subida de nivel
                if (consecutiveCorrect >= 3) {
                    upgradeLevel()
                    consecutiveCorrect = 0
                    showFeedback = FeedbackType.SUBIO_NIVEL
                } else {
                    showFeedback = FeedbackType.CORRECTO
                }

                // 3. Persistencia
                saveProgress()

                // 4. Esperar a que termine la animación (2 segundos)
                delay(2000)
                showFeedback = null

                // 5. Cargar siguiente pregunta
                loadQuestion(category)

            } else {
                // SE EQUIVOCÓ
                showFeedback = FeedbackType.INCORRECTO

                // Esperar para mostrar el error
                delay(2000)
                showFeedback = null

                saveProgress() // Guardar antes de salir
                onGameOver()
            }
        }
    }

    private fun upgradeLevel() {
        currentLevel += 1 // Simplemente sumamos 1 al nivel actual
    }

    // En TriviaViewModel.kt
    private fun saveProgress() {
        // Guardamos en el objeto compartido
        cl.jlopezr.trivia.core.data.ProgressStorage.totalScore = totalScore
        cl.jlopezr.trivia.core.data.ProgressStorage.currentLevel = currentLevel

        println("PROGRESO GUARDADO EN STORAGE: Nivel $currentLevel - Puntos $totalScore")
    }

    fun saveFinalScore() {
        saveProgress()
    }
}