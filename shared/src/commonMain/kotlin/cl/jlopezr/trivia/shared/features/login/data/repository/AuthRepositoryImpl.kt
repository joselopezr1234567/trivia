package cl.jlopezr.trivia.shared.features.login.data.repository

import cl.jlopezr.trivia.core.network.model.UserProfileResponse
import cl.jlopezr.trivia.core.network.model.UserLoginRequest
import cl.jlopezr.trivia.core.network.model.UserRegisterRequest
import cl.jlopezr.trivia.shared.features.login.domain.AuthRepository
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.HttpClient

class AuthRepositoryImpl(private val httpClient: HttpClient) : AuthRepository {

    private val baseUrl = "http://10.0.2.2:8080"

    override suspend fun login(email: String, password: String): Result<UserProfileResponse> {
        return try {
            val response: HttpResponse = httpClient.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(UserLoginRequest(email, password))
            }

            if (response.status == HttpStatusCode.OK) {
                val profile = response.body<UserProfileResponse>()
                Result.success(profile)
            } else {
                // 🛡️ CAMBIO CLAVE AQUÍ:
                // Primero leemos la respuesta como TEXTO puro para evitar que Ktor explote
                val errorBody = response.bodyAsText()

                println("⚠️ ERROR 400/401 DEL SERVIDOR: $errorBody")

                // Intentamos extraer un mensaje útil del texto recibido
                val message = if (errorBody.contains("message")) {
                    // Si parece JSON, podrías intentar parsearlo, pero por ahora
                    // lo mostramos tal cual para debuguear
                    errorBody
                } else if (errorBody.isEmpty()) {
                    "El servidor no envió detalles del error (400)"
                } else {
                    errorBody
                }

                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            // Esto captura errores de red (ej: servidor apagado)
            println("❌ ERROR DE RED: ${e.message}")
            Result.failure(Exception("No se pudo conectar con el servidor"))
        }
    }

    override suspend fun register(user: UserRegisterRequest): Result<UserProfileResponse> {
        return try {
            val response: HttpResponse = httpClient.post("$baseUrl/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(user)
            }

            if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                Result.success(response.body())
            } else {
                val errorBody = response.bodyAsText()
                Result.failure(Exception("Error en registro (${response.status.value}): $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}