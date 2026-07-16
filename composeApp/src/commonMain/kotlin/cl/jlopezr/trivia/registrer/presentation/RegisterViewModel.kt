package cl.jlopezr.trivia.registrer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.jlopezr.trivia.core.network.model.UserRegisterRequest
import cl.jlopezr.trivia.shared.features.login.domain.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import cl.jlopezr.trivia.registrer.presentation.RegisterUiState



// ✅ Agregamos el constructor con el repositorio
class RegisterViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    // ✅ Cambiamos el nombre a 'register' y agregamos los campos faltantes
    fun register(fullName: String, email: String, phone: String, password: String) {

        viewModelScope.launch {
            // 1. Mostrar estado de carga
            _uiState.update { it.copy(isLoading = true, error = null) }

            val cleanEmail = email.trim() // 🔥 Limpieza
            val cleanPass = password.trim() // 🔥 Limpieza

            // 2. Preparar los datos para el servidor
            val request = UserRegisterRequest(
                username = fullName.trim(),
                email = cleanEmail,
                password = cleanPass,
                phone = phone.trim()
            )

            // 3. Llamada real al repositorio (conecta con Ktor)
            val result = repository.register(request)

            result.onSuccess {
                println("✅ Registro exitoso en el servidor")
                _uiState.update { it.copy(
                    isLoading = false,
                    isSuccess = true,
                    isRegistered = true
                )}
            }.onFailure { e ->
                println("❌ Error en el registro: ${e.message}")
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error desconocido"
                )}
            }
        }
    }
}