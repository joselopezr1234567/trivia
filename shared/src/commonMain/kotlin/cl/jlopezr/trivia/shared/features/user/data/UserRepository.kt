package cl.jlopezr.trivia.shared.features.user.data

import cl.jlopezr.trivia.shared.core.data.ProgressStorage
import cl.jlopezr.trivia.shared.core.network.model.InviteInfoResponse
import cl.jlopezr.trivia.shared.core.network.model.LoginServerResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Definimos el modelo de datos aquí mismo para asegurar visibilidad
@Serializable
data class UserProgressRequest(
    val email: String,
    val points: Int,
    val level: Int
)

// Modelo para la respuesta del progreso
data class UserProgress(
    val email: String,
    val totalPoints: Int,
    val level: Int
)

class UserRepository {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }

    // Cambiado de 10.0.2.2 para conexión con celular físico.
    private val baseUrl = "http://192.168.1.200:8080"

    suspend fun updateRemoteProgress(email: String, points: Int, level: Int): Result<Unit> {
        return try {
            client.post("$baseUrl/user/progress/update") {
                contentType(ContentType.Application.Json)
                setBody(UserProgressRequest(email, points, level))
            }
            println("DB SQL: Sincronización exitosa para $email")
            Result.success(Unit)
        } catch (e: Exception) {
            println("DB SQL ERROR: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getRemoteProgress(email: String): Result<UserProgress> {
        return try {
            val response = client.get("$baseUrl/user/progress/$email") {
                accept(ContentType.Application.Json)
            }.body<UserProgressRequest>()

            ProgressStorage.totalScore = response.points
            ProgressStorage.currentLevel = response.level

            Result.success(UserProgress(email, response.points, response.level))
        } catch (e: Exception) {
            println("ERROR DESERIALIZANDO: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getInviteInfo(email: String): Result<InviteInfoResponse> {
        return try {
            val response = client.get("$baseUrl/user/invite-info/$email").body<InviteInfoResponse>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun claimInviteReward(email: String): Result<String> {
        return try {
            val response = client.post("$baseUrl/user/claim-invite-reward/$email").body<LoginServerResponse>()
            if (response.success) {
                Result.success(response.message)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
