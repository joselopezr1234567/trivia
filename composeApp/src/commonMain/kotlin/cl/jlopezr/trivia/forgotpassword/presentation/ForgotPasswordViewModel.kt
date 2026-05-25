package cl.jlopezr.trivia.forgotpassword.presentation



// --- IMPORTACIONES NUEVAS ---
// Nota: Si no usas el repositorio directamente en el VM (porque ya usas los UseCase), puedes borrar el import del repositorio.
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.jlopezr.trivia.forgotpassword.domain.usecase.ResetPasswordUseCase
import cl.jlopezr.trivia.forgotpassword.domain.usecase.ValidatePhoneUseCase
import kotlinx.coroutines.launch

// ----------------------------
class ForgotPasswordViewModel(
    private val validatePhoneUseCase: ValidatePhoneUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {

    // Pantalla 1: Ingreso de Teléfono
    var state by mutableStateOf(ForgotPasswordState())
        private set

    // Pantalla 2: Reset de Contraseña (Referencia al archivo independiente)
    var resetState by mutableStateOf(ResetPasswordState())
        private set

    // --- Lógica Pantalla 1 ---
    fun onPhoneChange(newPhone: String) {
        state = state.copy(phoneNumber = newPhone, errorMessage = null)
    }

    fun sendCode() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val result = validatePhoneUseCase(state.phoneNumber)
            state = if (result.isSuccess) {
                state.copy(isLoading = false, isCodeSent = true)
            } else {
                state.copy(isLoading = false, errorMessage = "Número no registrado")
            }
        }
    }

    // --- Lógica Pantalla 2 (Necesaria para Navigation.kt) ---
    fun onCodeChange(newValue: String) {
        resetState = resetState.copy(code = newValue)
    }

    fun onPasswordChange(newValue: String) {
        resetState = resetState.copy(newPassword = newValue)
    }

    fun onConfirmPasswordChange(newValue: String) {
        resetState = resetState.copy(confirmPassword = newValue)
    }

    fun onTogglePasswordVisibility() {
        resetState = resetState.copy(isPasswordVisible = !resetState.isPasswordVisible)
    }

    fun resetPassword(phone: String) {
        viewModelScope.launch {
            resetState = resetState.copy(isLoading = true, errorMessage = null)

            val result = resetPasswordUseCase(
                phone = phone,
                code = resetState.code,
                newPass = resetState.newPassword
            )

            resetState = if (result.isSuccess) {
                resetState.copy(isLoading = false, isSuccess = true)
            } else {
                resetState.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Error al cambiar contraseña"
                )
            }
        }
    }
}