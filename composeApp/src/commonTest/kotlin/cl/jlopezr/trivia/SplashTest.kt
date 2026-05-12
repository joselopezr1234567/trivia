package cl.jlopezr.trivia

import cl.jlopezr.trivia.splash.domain.model.SplashAction
import cl.jlopezr.trivia.splash.domain.repository.SplashRepository
import cl.jlopezr.trivia.splash.domain.usecase.GetSplashActionUseCase
import cl.jlopezr.trivia.splash.presentation.SplashViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher

import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals as asserterEquals

@OptIn(ExperimentalCoroutinesApi::class)

class SplashTest {

    private val testDispatcher = StandardTestDispatcher()

    // Se ejecuta antes de cada test

    @BeforeTest
    fun setup() {
        // Reemplaza el Dispatchers.Main por nuestro testDispatcher
        Dispatchers.setMain(testDispatcher)
    }

    // Se ejecuta después de cada test

    @AfterTest
    fun tearDown() {
        // Limpia el Dispatcher para no afectar a otros tests
        Dispatchers.resetMain()
    }

    //MOCK
    private class FakeSplashRepository(val LoggedIn: Boolean) : SplashRepository {
        override suspend fun IsUserLoggedIn(): Boolean {
            return LoggedIn
        }
    }


    @Test
    fun `debe navegar a login cuando el usuario no este logeado`() = runTest{
        val fakeRepo = FakeSplashRepository(false)
        val useCase = GetSplashActionUseCase(fakeRepo)
        val viewModel = SplashViewModel(useCase)

        testDispatcher.scheduler.advanceUntilIdle()

        asserterEquals(SplashAction.GoToLongin, viewModel.action.value)
    }


    @Test
    fun `debe navegar a home cuando el usuario  este logeado`() = runTest{
        val fakeRepo = FakeSplashRepository(true)
        val useCase = GetSplashActionUseCase(fakeRepo)
        val viewModel = SplashViewModel(useCase)

        testDispatcher.scheduler.advanceUntilIdle()

        asserterEquals(SplashAction.GoToHome, viewModel.action.value)
    }
}