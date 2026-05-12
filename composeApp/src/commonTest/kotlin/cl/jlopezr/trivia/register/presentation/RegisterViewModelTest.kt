package cl.jlopezr.trivia.register.presentation

import cl.jlopezr.trivia.registrer.presentation.RegisterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*



@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    private lateinit var viewModel: RegisterViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        // Configuramos el dispatcher de Main para los tests
        Dispatchers.setMain(testDispatcher)
        viewModel = RegisterViewModel()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cuando las contrasenas no coinciden debe mostrar error`() = runTest {
        // WHEN: Intentamos registrar con claves distintas
        viewModel.onRegister(
            email = "test@jl.cl",
            pass = "123456",
            confirmPass = "654321" // Distinta
        )

        // THEN: El estado debe reflejar el error
        val state = viewModel.uiState.value
        assertEquals("Las contraseñas no coinciden", state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `cuando los datos son validos debe mostrar el dialogo de OTP`() = runTest {
        // WHEN: Datos correctos
        viewModel.onRegister(
            email = "test@jl.cl",
            pass = "123456",
            confirmPass = "123456"
        )

        // Avanzamos los procesos pendientes del dispatcher
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: Debería activarse el flag para mostrar el diálogo
        assertTrue(viewModel.uiState.value.isRegistered)
        // Nota: Si usas el flag showOtpDialog, cámbialo aquí
    }
}