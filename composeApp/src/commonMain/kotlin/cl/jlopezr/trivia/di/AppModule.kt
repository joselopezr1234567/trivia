package cl.jlopezr.trivia.di

// Asegúrate de que estos imports coincidan con la ubicación real de tus archivos
import cl.jlopezr.trivia.login.domain.AuthRepository
import cl.jlopezr.trivia.shared.features.login.domain.AuthRepositoryImpl
import cl.jlopezr.trivia.login.presentation.LoginViewModel

// Koin
import org.koin.dsl.module

// Ktor
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

import org.koin.core.module.dsl.viewModel

val appModule = module {

    // 1. Definición del HttpClient con configuración básica
    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }
    }

    // 2. Vinculación de la interfaz con su implementación concreta
    // El 'get()' busca automáticamente la instancia de HttpClient definida arriba
    single<AuthRepository> { AuthRepositoryImpl(get()) }

    // 3. Definición del ViewModel (factory crea una nueva instancia cada vez que se requiere)
    viewModel { LoginViewModel(get()) }
}