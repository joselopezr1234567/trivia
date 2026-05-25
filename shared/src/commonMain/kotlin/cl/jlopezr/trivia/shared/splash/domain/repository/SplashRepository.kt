package cl.jlopezr.trivia.splash.domain.repository

interface SplashRepository {
    suspend fun IsUserLoggedIn(): Boolean
}