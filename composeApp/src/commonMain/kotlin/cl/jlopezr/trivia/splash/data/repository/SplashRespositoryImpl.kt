package cl.jlopezr.trivia.splash.data.repository

import cl.jlopezr.trivia.splash.domain.repository.SplashRepository

class SplashRepositoryImpl : SplashRepository {

    override suspend fun IsUserLoggedIn() : Boolean {

        return false
    }
}