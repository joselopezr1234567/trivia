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
import kotlinx.serialization.Serializable
import cl.jlopezr.trivia.shared.registrer.domain.model.RegisterUser

@Serializable
data class LoginServerResponse(
    val success: Boolean,
    val message: String
)

class AuthRepositoryImpl(private val httpClient: HttpClient) : AuthRepository {

    private val baseUrl = "http://10.0.2.2:8080"

    override suspend fun login(email: String, password: String): Result<UserProfileResponse> {
        return try {
            val response: HttpResponse = httpClient.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(UserLoginRequest(email, password))
            }

            if (response.status == HttpStatusCode.OK) {
                val loginResult = response.body<LoginServerResponse>()
                if (loginResult.success) {
                    Result.success(UserProfileResponse(id = 1, email = email, username = "Usuario"))
                } else {
                    Result.failure(Exception(loginResult.message))
                }
            } else {
                Result.failure(Exception("Credenciales incorrectas"))
            }
        } catch (e: Exception) {
            if (e.message?.contains("success") == true) {
                Result.success(UserProfileResponse(1, email, "Usuario"))
            } else {
                Result.failure(Exception("Error al conectar con el servidor"))
            }
        }
    }

    override suspend fun register(user: UserRegisterRequest): Result<UserProfileResponse> {
        return try {
            val response: HttpResponse = httpClient.post("$baseUrl/auth/register") {
                contentType(ContentType.Application.Json)
                // Como el parámetro 'user' ya es UserRegisterRequest, lo enviamos directamente
                setBody(user)
            }

            if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                val body = response.body<LoginServerResponse>()

                if (body.success) {
                    // La interfaz pide devolver un Result<UserProfileResponse>
                    val profile = UserProfileResponse(
                        id = 1,
                        email = user.email,
                        username = user.username
                    )
                    Result.success(profile)
                } else {
                    Result.failure(Exception(body.message))
                }
            } else {
                val errorText = response.bodyAsText()
                Result.failure(Exception("Error en registro: $errorText"))
            }
        } catch (e: Exception) {
            println("❌ ERROR EN REGISTER: ${e.message}")
            Result.failure(e)
        }
    }
}