package cl.jlopezr.trivia.login.domain.use_case

import cl.jlopezr.trivia.login.domain.model.RegisterUser
import cl.jlopezr.trivia.login.domain.repository.LoginRepository
import cl.jlopezr.trivia.login.domain.usecase.LoginUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertTrue as assert




class LoginUseCaseTest {

    // 1. Creamos el Mock del repositorio
    private val repository = mockk<LoginRepository>()
    private val loginUseCase = LoginUseCase(repository)

    @Test
    fun `cuando el login es exitoso debe retornar un usuario`() = runTest {
        // GIVEN: Configuramos el comportamiento del Mock
        val expectedUser = RegisterUser("1", "test@jl.cl", password = "pass123", phone = "token-123")
        coEvery {
            repository.login("test@jl.cl", "pass123")
        } returns Result.success(expectedUser)

        // WHEN: Ejecutamos el caso de uso
        val result = loginUseCase("test@jl.cl", "pass123")

        // THEN: Verificamos el resultado
        assertTrue(result.isSuccess)
        assert(result.getOrNull()?.email == "test@jl.cl")
    }
}