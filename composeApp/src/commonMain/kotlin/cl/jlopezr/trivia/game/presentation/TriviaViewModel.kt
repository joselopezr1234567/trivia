package cl.jlopezr.trivia.game.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.jlopezr.trivia.shared.features.game.domain.repository.TriviaRepository
import cl.jlopezr.trivia.shared.core.network.model.TriviaResponse
import cl.jlopezr.trivia.shared.core.network.model.TriviaRequest
import cl.jlopezr.trivia.shared.core.data.ProgressStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import cl.jlopezr.trivia.shared.features.user.data.UserRepository

sealed interface TriviaUiState {
    object Loading : TriviaUiState
    data class Success(val question: TriviaResponse) : TriviaUiState
    data class Error(val message: String) : TriviaUiState
}

enum class FeedbackType { CORRECTO, INCORRECTO, SUBIO_NIVEL }

class TriviaViewModel(
    private val repository: TriviaRepository,
    private val userRepository: UserRepository // 🔥 AGREGADO AQUÍ: Ahora el ViewModel conoce el repositorio de usuario
) : ViewModel() {

    private val _uiState = MutableStateFlow<TriviaUiState>(TriviaUiState.Loading)
    val uiState: StateFlow<TriviaUiState> = _uiState.asStateFlow()

    var showFeedback by mutableStateOf<FeedbackType?>(null)
        private set

    private val askedQuestions = mutableListOf<String>()

    var consecutiveCorrect by mutableStateOf(0)
        private set

    var totalScore by mutableStateOf(ProgressStorage.totalScore)
        private set

    var currentLevel by mutableStateOf(ProgressStorage.currentLevel)
        private set

    fun loadQuestion(category: String) {
        viewModelScope.launch {
            _uiState.value = TriviaUiState.Loading
            try {
                val request = TriviaRequest(
                    category,
                    currentLevel.toString(),
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

    fun processAnswer(
        isCorrect: Boolean,
        category: String,
        onGameOver: () -> Unit
    ) {
        viewModelScope.launch {
            if (isCorrect) {
                val pointsToAdd = when {
                    currentLevel <= 2 -> 1
                    currentLevel <= 5 -> 2
                    else -> 3
                }
                totalScore += pointsToAdd
                consecutiveCorrect += 1

                if (consecutiveCorrect >= 3) {
                    upgradeLevel()
                    consecutiveCorrect = 0
                    showFeedback = FeedbackType.SUBIO_NIVEL
                } else {
                    showFeedback = FeedbackType.CORRECTO
                }

                saveProgress()

                delay(2000)
                showFeedback = null
                loadQuestion(category)

            } else {
                showFeedback = FeedbackType.INCORRECTO
                delay(2000)
                showFeedback = null
                saveProgress()
                onGameOver()
            }
        }
    }

    private fun upgradeLevel() {
        currentLevel += 1
    }

    /**
     * Sincroniza localmente y envía a la base de datos SQL.
     */
    private fun saveProgress() {
        // 1. Guardar en memoria local (ProgressStorage)
        ProgressStorage.totalScore = totalScore
        ProgressStorage.currentLevel = currentLevel

        // 2. Guardar en la nube (SQL)
        viewModelScope.launch {
            val userId = "id-del-usuario-actual" // Esto debería venir de tu sesión real

            userRepository.updateRemoteProgress(
                email = userId, // <--- Cambiamos 'userId' por 'email'
                points = totalScore,
                level = currentLevel
            )
        }
    }

    fun saveFinalScore() {
        saveProgress()
    }
}