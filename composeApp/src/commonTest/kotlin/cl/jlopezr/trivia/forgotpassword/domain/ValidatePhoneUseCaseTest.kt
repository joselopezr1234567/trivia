import cl.jlopezr.trivia.forgotpassword.domain.usecase.ValidatePhoneUseCase


import cl.jlopezr.trivia.forgotpassword.domain.ForgotPasswordRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class ValidatePhoneUseCaseTest {
    private val repository = mockk<ForgotPasswordRepository>()
    private val useCase = ValidatePhoneUseCase(repository)

    @Test
    fun `debe fallar si el numero es muy corto`() = runTest {
        val result = useCase("123")
        assertTrue(result.isFailure)
    }

    @Test
    fun `debe llamar al repositorio si el numero es valido`() = runTest {
        val phone = "+56912345678"
        coEvery { repository.validatePhoneAndSendCode(phone) } returns Result.success(Unit)

        val result = useCase(phone)
        assertTrue(result.isSuccess)
    }
}