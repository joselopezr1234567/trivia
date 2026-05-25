package cl.jlopezr.trivia.forgotpassword.domain.usecase


import cl.jlopezr.trivia.shared.forgotpassword.domain.ForgotPasswordRepository

class ValidatePhoneUseCase(private val repository: ForgotPasswordRepository) {
    suspend operator fun invoke(phone: String): Result<Unit> {
        // Aquí podrías añadir lógica extra antes de llamar al repo,
        // como validar que el número tenga el formato de Chile (+56)
        return if (phone.length < 8) {
            Result.failure(Exception("Número inválido"))
        } else {
            repository.validatePhoneAndSendCode(phone)
        }
    }
}