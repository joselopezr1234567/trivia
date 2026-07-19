package cl.jlopezr.trivia.forgotpassword.data

import cl.jlopezr.trivia.shared.forgotpassword.domain.ForgotPasswordRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ForgotPasswordRequest(val phone: String)

@Serializable
data class ResetPasswordRequest(
    val phone: String,
    val code: String,
    val newPassword: String
)

@Serializable
data class ForgotPasswordResponse(val success: Boolean, val message: String)

class ForgotPasswordRepositoryImpl : ForgotPasswordRepository {
    
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }
    
    private val baseUrl = "http://192.168.1.200:8080"

    override suspend fun validatePhoneAndSendCode(phone: String): Result<Unit> {
        return try {
            val response = client.post("$baseUrl/auth/forgot-password") {
                contentType(ContentType.Application.Json)
                setBody(ForgotPasswordRequest(phone))
            }
            
            if (response.status == HttpStatusCode.OK) {
                Result.success(Unit)
            } else {
                val error = response.body<ForgotPasswordResponse>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(phone: String, code: String, newPassword: String): Result<Unit> {
        return try {
            val response = client.post("$baseUrl/auth/reset-password") {
                contentType(ContentType.Application.Json)
                setBody(ResetPasswordRequest(phone, code, newPassword))
            }
            
            if (response.status == HttpStatusCode.OK) {
                Result.success(Unit)
            } else {
                val error = response.body<ForgotPasswordResponse>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}