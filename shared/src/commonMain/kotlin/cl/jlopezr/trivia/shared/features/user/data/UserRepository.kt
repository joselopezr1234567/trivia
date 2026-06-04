package cl.jlopezr.trivia.shared.features.user.data

import cl.jlopezr.trivia.shared.core.data.ProgressStorage
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

    // 10.0.2.2 es para el emulador de Android.
    // Si usas un celular físico, pon la IP de tu Macbook (ej. 192.168.1.XX)
    private val baseUrl = "http://10.0.2.2:8080"

    suspend fun updateRemoteProgress(email: String, points: Int, level: Int): Result<Unit> {
        return try {
            // Especificamos el tipo UserProgressRequest explícitamente en el post
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
            // Forzamos la recepción del tipo específico
            val response = client.get("$baseUrl/user/progress/$email") {
                accept(ContentType.Application.Json) // Le decimos al server que queremos JSON
            }.body<UserProgressRequest>() // <--- Asegúrate de usar .body<UserProgressRequest>()

            ProgressStorage.totalScore = response.points
            ProgressStorage.currentLevel = response.level

            Result.success(UserProgress(email, response.points, response.level))
        } catch (e: Exception) {
            println("ERROR DESERIALIZANDO: ${e.message}")
            Result.failure(e)
        }
    }
}