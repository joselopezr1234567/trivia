package cl.jlopezr.trivia.shared.features.login.data.repository

import cl.jlopezr.trivia.core.network.model.UserProfileResponse
import cl.jlopezr.trivia.core.network.model.UserLoginRequest // ✅ Asegúrate de tener este import
import cl.jlopezr.trivia.core.network.model.UserRegisterRequest
import cl.jlopezr.trivia.shared.features.login.domain.AuthRepository
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.* // ✅ Necesario para usar HttpResponse
import io.ktor.http.*
import io.ktor.client.HttpClient

class AuthRepositoryImpl(private val httpClient: HttpClient) : AuthRepository {

    private val baseUrl = "http://10.0.2.2:8080"



    // 🎯 CORREGIDO: Cambiamos la firma a Result<UserProfileResponse> para que coincida con tu ViewModel
    override suspend fun login(email: String, password: String): Result<UserProfileResponse> {
        return try {
            // 1. Obtenemos la respuesta genérica del servidor como HttpResponse
            val response: HttpResponse = httpClient.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(UserLoginRequest(email, password))
            }

            // 2. Evaluamos el código de estado HTTP antes de deserializar
            if (response.status == HttpStatusCode.OK) {
                val profile = response.body<UserProfileResponse>()

                // 🚫 Aquí podrás descomentar tu guardado de token cuando lo reactives:
                // settings.putString("auth_token", profile.token)

                Result.success(profile)
            } else {
                // El servidor respondió con un error controlado (ej: 401 Unauthorized)
                val errorMap = response.body<Map<String, String>>()
                val backendMessage = errorMap["error"] ?: "Credenciales incorrectas"

                Result.failure(Exception(backendMessage))
            }
        } catch (e: Exception) {
            println("❌ ERROR EN PETICIÓN HTTP: ${e.message}")
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }
    override suspend fun register(user: UserRegisterRequest): Result<UserProfileResponse> {
        return try {
            val response: HttpResponse = httpClient.post("$baseUrl/auth/register")  { // 10.0.2.2 es localhost en Android
                contentType(ContentType.Application.Json)
                setBody(user) // Ktor usa @Serializable de tu modelo RegisterUser
            }

            if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                Result.success(response.body())
            } else {
                // Captura errores como "Email ya existe" (409 Conflict)
                Result.failure(Exception("Error en registro: ${response.status.description}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
