package cl.jlopezr.trivia.login.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import cl.jlopezr.trivia.shared.features.login.domain.AuthRepository
import cl.jlopezr.trivia.shared.core.data.UserSession



class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onPasswordChanged(newPassword: String) {
        _state.update {
            it.copy(
                password = newPassword,
                errorMessage = null
            )
        }
    }

    fun onEmailChanged(newEmail: String) {
        _state.update { state ->
            state.copy(
                email = newEmail,
                errorMessage = null
            )
        }
    }

    fun login() {
        val currentEmail = state.value.email
        val currentPassword = state.value.password

        if (currentEmail.isBlank() || currentPassword.isBlank()) {
            _state.update { it.copy(errorMessage = "Por favor, completa todos los campos") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            // Llamada al repositorio (Render o Mock)
            authRepository.login(currentEmail, currentPassword)
                .onSuccess { userProfile ->
                    println("DEBUG: Login exitoso en el repositorio para $currentEmail")
                    UserSession.email = currentEmail
                    UserSession.username = userProfile.username // 🔥 GUARDAMOS EL USERNAME
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isLoginSuccess = true
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error desconocido" // Corregido: message
                        )
                    }
                }
        }
    }
}