package cl.jlopezr.trivia.shared.forgotpassword.domain

interface ForgotPasswordRepository {
    suspend fun validatePhoneAndSendCode(phone: String): Result<Unit>
    suspend fun resetPassword(phone: String, code: String, newPassword: String): Result<Unit>
}