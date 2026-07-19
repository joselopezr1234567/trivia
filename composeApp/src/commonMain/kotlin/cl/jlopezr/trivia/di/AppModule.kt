package cl.jlopezr.trivia.di

import cl.jlopezr.trivia.login.presentation.LoginViewModel
import cl.jlopezr.trivia.registrer.presentation.RegisterViewModel
import cl.jlopezr.trivia.home.presentation.HomeViewModel
import cl.jlopezr.trivia.home.domain.usecase.GetQuestionsUseCase
import cl.jlopezr.trivia.home.data.repository.TriviaRepositoryImpl
import cl.jlopezr.trivia.shared.features.login.data.repository.AuthRepositoryImpl
import cl.jlopezr.trivia.shared.features.login.domain.AuthRepository
import cl.jlopezr.trivia.shared.features.user.data.UserRepository
import cl.jlopezr.trivia.shared.features.game.domain.repository.TriviaRepository
import cl.jlopezr.trivia.game.presentation.TriviaViewModel
import cl.jlopezr.trivia.ranking.presentation.RankingViewModel

// Koin
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

// Ktor
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

// Importamos el platformModule que es diferente para cada plataforma
expect val platformModule: org.koin.core.module.Module

val appModule = module {
    // Incluir el módulo de plataforma
    includes(platformModule)

    // 1. Cliente HTTP
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                })
            }
        }
    }

    // 2. Repositorios
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single { UserRepository() }
    single { TriviaRepository() }
    single { TriviaRepositoryImpl() }

    // 3. Use Cases
    single { GetQuestionsUseCase(get<TriviaRepositoryImpl>()) }

    // 4. ViewModels
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { TriviaViewModel(get(), get()) }
    viewModel { RankingViewModel() }
}
