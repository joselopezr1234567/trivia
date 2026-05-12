package cl.jlopezr.trivia

import cl.jlopezr.trivia.login.domain.AuthRepository
import cl.jlopezr.trivia.login.presentation.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MockAuthRepository(private val result: Result<Unit>) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<Unit> {
        return result
    }
}

@OptIn(ExperimentalCoroutinesApi::class) // Necesario para usar funciones de prueba de corrutinas
class LoginViewModelTest {

    // 1. Definimos un despachador de pruebas
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        // 2. Antes de cada prueba, establecemos el testDispatcher como el "Main"
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        // 3. Después de cada prueba, limpiamos el despachador
        Dispatchers.resetMain()
    }

    @Test
    fun `cuando el login es exitoso debe cambiar isLoginSuccess a true`() = runTest {
        val mockRepo = MockAuthRepository(Result.success(Unit))
        val viewModel = LoginViewModel(mockRepo)

        viewModel.onEmailChanged("test@test.com")
        viewModel.onPasswordChanged("123456")
        viewModel.login()

        assertTrue(viewModel.state.value.isLoginSuccess)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `cuando el login falla debe mostrar un error`() = runTest {
        val mockRepo = MockAuthRepository(Result.failure(Exception("Error de Render")))
        val viewModel = LoginViewModel(mockRepo)

        viewModel.onEmailChanged("test@test.com")
        viewModel.onPasswordChanged("123456")
        viewModel.login()

        assertNotNull(viewModel.state.value.errorMessage)
        assertEquals("Error de Render", viewModel.state.value.errorMessage)
        assertFalse(viewModel.state.value.isLoginSuccess)
    }
}