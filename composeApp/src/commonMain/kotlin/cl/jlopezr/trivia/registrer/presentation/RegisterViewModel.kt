package cl.jlopezr.trivia.registrer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    fun onRegister(email: String, pass: String, confirmPass: String) {
        if (pass != confirmPass) {
            _uiState.value = _uiState.value.copy(error = "Las contraseñas no coinciden")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _uiState.value = _uiState.value.copy(isRegistered = true)
        }



    }


}