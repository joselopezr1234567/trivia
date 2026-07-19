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
import cl.jlopezr.trivia.shared.core.data.UserSession
import cl.jlopezr.trivia.core.ads.getAdsManager
import cl.jlopezr.trivia.core.audio.getAudioManager
import cl.jlopezr.trivia.getPlatform

sealed interface TriviaUiState {
    object Loading : TriviaUiState
    data class Success(val question: TriviaResponse) : TriviaUiState
    data class Error(val message: String) : TriviaUiState
}

enum class FeedbackType { CORRECTO, INCORRECTO, SUBIO_NIVEL, TIEMPO_AGOTADO }

class TriviaViewModel(
    private val repository: TriviaRepository,
    private val userRepository: UserRepository // 🔥 AGREGADO AQUÍ: Ahora el ViewModel conoce el repositorio de usuario
) : ViewModel() {

    private val _uiState = MutableStateFlow<TriviaUiState>(TriviaUiState.Loading)
    val uiState: StateFlow<TriviaUiState> = _uiState.asStateFlow()

    var showFeedback by mutableStateOf<FeedbackType?>(null)
        private set

    private val askedQuestions = mutableListOf<String>()

    var questionsCountSinceLastAd by mutableStateOf(0)
        private set

    var showRewardedPrompt by mutableStateOf(false)
        private set

    var timeLeft by mutableStateOf(10)
        private set

    var isMuted by mutableStateOf(ProgressStorage.isMuted) // 🔥 Sincronizado con el global
        private set

    private var timerJob: kotlinx.coroutines.Job? = null

    private var lastCategory: String = ""
    private var lastOnGameOver: (() -> Unit)? = null

    var consecutiveCorrect by mutableStateOf(0)
        private set

    var totalScore by mutableStateOf(ProgressStorage.totalScore)
        private set

    var currentLevel by mutableStateOf(ProgressStorage.currentLevel)
        private set

    fun toggleMute() {
        ProgressStorage.isMuted = !ProgressStorage.isMuted
        isMuted = ProgressStorage.isMuted
        getAudioManager().setMuted(isMuted)
    }

    fun startTimer(category: String, onGameOver: () -> Unit) {
        timerJob?.cancel()
        timeLeft = 10
        getAudioManager().lowerVolume() // 🔥 BAJAMOS VOLUMEN AL EMPEZAR PREGUNTA
        timerJob = viewModelScope.launch {
            while (timeLeft > 0) {
                if (!isAnswerSelected) {
                    getAudioManager().playTickSound() // 🔥 SONIDO DE RELOJ CADA SEGUNDO
                    delay(1000)
                    timeLeft--
                } else {
                    break
                }
            }
            if (timeLeft == 0 && !isAnswerSelected) {
                // Al llegar a 0, ejecutamos processAnswer indicando que falló por tiempo
                processAnswer(isCorrect = false, category = category, onGameOver = onGameOver)
            }
        }
    }

    fun loadQuestion(category: String, onGameOver: () -> Unit) {
        this.lastCategory = category
        this.lastOnGameOver = onGameOver
        viewModelScope.launch {
            _uiState.value = TriviaUiState.Loading
            isAnswerSelected = false // Resetear flag al cargar nueva pregunta
            try {
                val currentLanguage = getPlatform().language
                println("LOG [TRIVIA]: Solicitando trivia en idioma: $currentLanguage")
                
                val request = TriviaRequest(
                    topic = category,
                    difficulty = currentLevel.toString(),
                    history = askedQuestions.toList(),
                    language = currentLanguage
                )

                repository.getNewQuestion(request)
                    .onSuccess { result ->
                        // --- ALEATORIEDAD POR CÓDIGO ---
                        val originalOptions = result.options
                        val correctAnswer = originalOptions[result.correctIndex]
                        
                        val shuffledOptions = originalOptions.shuffled()
                        val newCorrectIndex = shuffledOptions.indexOf(correctAnswer)
                        
                        val randomizedResult = result.copy(
                            options = shuffledOptions,
                            correctIndex = newCorrectIndex
                        )

                        askedQuestions.add(result.question)
                        _uiState.value = TriviaUiState.Success(randomizedResult)
                        
                        // Iniciar el temporizador pasando el callback de fin de juego
                        startTimer(category, onGameOver)
                    }
                    .onFailure { error ->
                        _uiState.value = TriviaUiState.Error(error.message ?: "Error de red")
                    }
            } catch (e: Exception) {
                _uiState.value = TriviaUiState.Error("Error: ${e.message}")
            }
        }
    }

    var isAnswerSelected by mutableStateOf(false)
        private set

    fun processAnswer(
        isCorrect: Boolean,
        category: String,
        onGameOver: () -> Unit
    ) {
        if (isAnswerSelected && timeLeft > 0) return // BLOQUEO DE SELECCIÓN MÚLTIPLE
        
        val wasTimeOut = timeLeft == 0 && !isAnswerSelected
        isAnswerSelected = true
        timerJob?.cancel() // Detener el reloj
        getAudioManager().stopTickSound() // 🔥 DETENER TICK-TAC INMEDIATAMENTE
        getAudioManager().restoreVolume() // 🔥 RESTAURAMOS VOLUMEN AL TERMINAR PREGUNTA

        viewModelScope.launch {
            if (isCorrect) {
                // ... (mantener lógica existente)
                val pointsToAdd = when {
                    currentLevel <= 2 -> 1
                    currentLevel <= 5 -> 2
                    else -> 3
                }
                totalScore += pointsToAdd
                consecutiveCorrect += 1
                questionsCountSinceLastAd += 1

                if (questionsCountSinceLastAd >= 3) {
                    showRewardedPrompt = true
                    questionsCountSinceLastAd = 0
                }

                if (consecutiveCorrect >= 3) {
                    upgradeLevel()
                    consecutiveCorrect = 0
                    showFeedback = FeedbackType.SUBIO_NIVEL
                } else {
                    showFeedback = FeedbackType.CORRECTO
                }

                saveProgress()

                delay(3500) // Tiempo para ver el feedback
                showFeedback = null
                
                if (!showRewardedPrompt) {
                    loadQuestion(category, onGameOver)
                }

            } else {
                showFeedback = if (wasTimeOut) FeedbackType.TIEMPO_AGOTADO else FeedbackType.INCORRECTO
                delay(5500) // Tiempo extra para leer la explicación de la respuesta correcta
                showFeedback = null
                saveProgress()

                // Pausar música para el anuncio
                getAudioManager().pauseBackgroundMusic()

                // Mostrar anuncio automático al perder
                getAdsManager().showInterstitial(
                    onAdClosed = {
                        getAudioManager().resumeBackgroundMusic() // Resumir al volver
                        onGameOver()
                    }
                )
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
            val userEmail = UserSession.email
            if (userEmail.isNotBlank()) {
                println("LOG [VM]: Sincronizando progreso para $userEmail -> Puntos: $totalScore, Nivel: $currentLevel")
                userRepository.updateRemoteProgress(
                    email = userEmail,
                    points = totalScore,
                    level = currentLevel
                )
            } else {
                println("ERROR [VM]: No se pudo sincronizar, UserSession.email está vacío")
            }
        }
    }

    fun saveFinalScore() {
        saveProgress()
    }

    /**
     * Añade puntos extra (ej: por ver un anuncio)
     */
    fun addBonusPoints(points: Int) {
        totalScore += points
        // Sumar ganancia promedio (ej: $0.01 / 2 = $0.005)
        ProgressStorage.totalEarnings += 0.005
        saveProgress()
    }

    fun dismissRewardedPrompt() {
        showRewardedPrompt = false
        // Al cerrar el prompt (ya sea viendo el video o no), continuamos el juego
        if (lastCategory.isNotEmpty() && lastOnGameOver != null) {
            loadQuestion(lastCategory, lastOnGameOver!!)
        }
    }
}