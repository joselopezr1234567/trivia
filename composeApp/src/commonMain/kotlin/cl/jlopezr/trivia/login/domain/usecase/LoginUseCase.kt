package cl.jlopezr.trivia.login.domain.usecase

import cl.jlopezr.trivia.login.domain.model.RegisterUser
import cl.jlopezr.trivia.login.domain.repository.LoginRepository

class LoginUseCase(private val repository: LoginRepository) {

    // El operador 'invoke' permite llamar a la clase como si fuera una función: loginUseCase(...)
    suspend operator fun invoke(email: String, password: String): Result<RegisterUser> {

        // Aquí es donde un Senior añade validaciones antes de ir al repositorio
        if (email.isBlank() || !email.contains("@")) {
            return Result.failure(Exception("Email inválido"))
        }

        if (password.length < 6) {
            return Result.failure(Exception("La contraseña es muy corta"))
        }

        // Si todo está bien, llamamos al repositorio (que luego conectará con Render)
        return repository.login(email, password)
    }
}