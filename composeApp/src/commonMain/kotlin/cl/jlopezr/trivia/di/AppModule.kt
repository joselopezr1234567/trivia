package cl.jlopezr.trivia.di

import cl.jlopezr.trivia.login.presentation.LoginViewModel
// ✅ Importamos el RegisterViewModel (Asegúrate de que la ruta sea correcta)
import cl.jlopezr.trivia.registrer.presentation.RegisterViewModel
import cl.jlopezr.trivia.shared.features.login.data.repository.AuthRepositoryImpl
import cl.jlopezr.trivia.shared.features.login.domain.AuthRepository

// Koin
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

// Ktor
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// Importamos el platformModule que es diferente para cada plataforma
expect val platformModule: org.koin.core.module.Module

val appModule = module {
    // Incluir el módulo de plataforma (donde está AdsManager)
    includes(platformModule)

    // 1. Cliente HTTP (Configurado para ignorar claves desconocidas del JSON)
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

    // 2. Repositorio
    // 'get()' inyectará automáticamente el HttpClient de arriba
    single<AuthRepository> { AuthRepositoryImpl(get()) }

    // 3. ViewModels
    // 'get()' inyectará automáticamente el AuthRepository en ambos ViewModels
    viewModel { LoginViewModel(get()) }

    // ✅ REGISTRAMOS EL VIEWMODEL DE REGISTRO
    viewModel { RegisterViewModel(get()) }
}
