package cl.jlopezr.trivia.splash.domain.usecase

import cl.jlopezr.trivia.splash.domain.model.SplashAction
import cl.jlopezr.trivia.splash.domain.repository.SplashRepository
import kotlinx.coroutines.delay

class GetSplashActionUseCase(private val repository: SplashRepository) {
    suspend operator fun invoke(): SplashAction {
        delay(2000)

        return if (repository.IsUserLoggedIn()) {
            SplashAction.GoToHome
        } else {
            SplashAction.GoToLongin
        }

    }

}