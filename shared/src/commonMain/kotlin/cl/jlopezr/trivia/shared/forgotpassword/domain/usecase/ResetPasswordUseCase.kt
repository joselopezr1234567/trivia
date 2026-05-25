package cl.jlopezr.trivia.forgotpassword.domain.usecase


import cl.jlopezr.trivia.shared.forgotpassword.domain.ForgotPasswordRepository

class ResetPasswordUseCase(private val repository: ForgotPasswordRepository) {
    suspend operator fun invoke(phone: String, code: String, newPass: String): Result<Unit> {
        return repository.resetPassword(phone, code, newPass)
    }
}