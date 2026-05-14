package cl.jlopezr.trivia.forgotpassword.data

import cl.jlopezr.trivia.forgotpassword.domain.ForgotPasswordRepository

class ForgotPasswordRepositoryImpl : ForgotPasswordRepository {

    override suspend fun validatePhoneAndSendCode(phone: String): Result<Unit> {
        // Aquí llamarás a tu endpoint de Node.js o Java
        return Result.success(Unit)
    }

    override suspend fun resetPassword(phone: String, code: String, newPassword: String): Result<Unit> {
        // Lógica para actualizar en PostgreSQL
        return Result.success(Unit)
    }
}