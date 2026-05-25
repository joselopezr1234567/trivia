package cl.jlopezr.trivia.splash.domain.model

sealed class SplashAction {
    object GoToLongin : SplashAction()
    object GoToHome : SplashAction()

}