package cl.jlopezr.trivia.forgotpassword.presentation

import cl.jlopezr.trivia.forgotpassword.domain.ForgotPasswordRepository
import cl.jlopezr.trivia.forgotpassword.domain.usecase.ResetPasswordUseCase
import cl.jlopezr.trivia.forgotpassword.domain.usecase.ValidatePhoneUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {

    private lateinit var viewModel: ForgotPasswordViewModel
    private val repository = mockk<ForgotPasswordRepository>()
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        // Configuramos el despachador para Corrutinas en modo Test
        Dispatchers.setMain(testDispatcher)

        // 1. Instanciamos los UseCases inyectando el repositorio mockeado
        // Esto resuelve el error de "Argument type mismatch"
        val validatePhoneUseCase = ValidatePhoneUseCase(repository)
        val resetPasswordUseCase = ResetPasswordUseCase(repository)

        // 2. Inicializamos el ViewModel con sus dependencias correctas
        viewModel = ForgotPasswordViewModel(
            validatePhoneUseCase = validatePhoneUseCase,
            resetPasswordUseCase = resetPasswordUseCase
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cuando se cambia el numero, el estado se actualiza correctamente`() {
        val phone = "+56912345678"
        viewModel.onPhoneChange(phone)

        assertEquals(phone, viewModel.state.phoneNumber)
        assertEquals(null, viewModel.state.errorMessage)
    }

    @Test
    fun `cuando el envio de codigo es exitoso, el estado cambia a Success`() {
        // GIVEN: El repositorio devuelve éxito
        val phone = "+56912345678"
        viewModel.onPhoneChange(phone)

        // Ajusta el nombre del método según tu interface ForgotPasswordRepository
        coEvery { repository.validatePhoneAndSendCode(phone) } returns Result.success(Unit)

        // WHEN: Ejecutamos la acción
        viewModel.sendCode()
        testDispatcher.scheduler.advanceUntilIdle() // Esperamos a que la corrutina termine

        // THEN: Verificamos los estados
        assertFalse(viewModel.state.isLoading)
        assertTrue(viewModel.state.isCodeSent)
        assertEquals(null, viewModel.state.errorMessage)
    }

    @Test
    fun `cuando el numero no existe, el estado muestra un mensaje de error`() {
        // GIVEN: El repositorio devuelve falla
        val phone = "000"
        viewModel.onPhoneChange(phone)

        coEvery { repository.validatePhoneAndSendCode(phone) } returns Result.failure(Exception("Not Found"))

        // WHEN: Ejecutamos
        viewModel.sendCode()
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: Verificamos error
        assertFalse(viewModel.state.isLoading)
        assertFalse(viewModel.state.isCodeSent)
        assertEquals("Número no registrado", viewModel.state.errorMessage)
    }
}