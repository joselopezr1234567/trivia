package cl.jlopezr.trivia.ranking.presentation

// Y asegúrate de importar el modelo de UI que tú creaste
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.jlopezr.network.fetchRanking
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RankingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadRanking()
    }

    // Dentro de tu RankingViewModel.kt
    private fun loadRanking() {
        viewModelScope.launch {
            println("LOG [VM]: Iniciando carga de ranking...")
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 1. Fetch de los datos (el modelo de red de commonMain)
                val networkData = fetchRanking()
                println("LOG [VM]: Datos recibidos de red: ${networkData.size} elementos")

                // 2. Mapeo: Traducimos del modelo de Red al modelo de UI
                val uiData = networkData.mapIndexed { index, networkItem ->
                    RankingItem(
                        position = index + 1,
                        username = networkItem.username, // Mapeamos el username de la red al campo username de la UI
                        score = networkItem.score,       // Mapeamos el score de la red al campo score de la UI
                        level = 1                        // El modelo de red no trae nivel, usamos 1 por defecto
                    )
                }
                println("LOG [VM]: Mapeo completado: ${uiData.size} elementos para UI")

                // 3. Actualizamos el estado
                _uiState.update { it.copy(rankingList = uiData, isLoading = false) }
            } catch (e: Exception) {
                println("ERROR [VM]: Fallo al cargar ranking -> ${e.message}")
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error al cargar: ${e.message}") }
            }
        }
    }
}