package cl.jlopezr.trivia.di

import cl.jlopezr.trivia.login.presentation.LoginViewModel
import cl.jlopezr.trivia.shared.features.login.data.repository.AuthRepositoryImpl
import cl.jlopezr.trivia.shared.features.login.domain.AuthRepository

// Koin
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

// Ktor
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*



val appModule = module {
    // 1. Cliente HTTP
    single {
        HttpClient() {
            install(ContentNegotiation) {
                json()
            }
        }
    }

    // 2. Repositorio
    single<AuthRepository> { AuthRepositoryImpl(get()) }

    // 3. ViewModel
    viewModel<LoginViewModel> { LoginViewModel(get()) }
}