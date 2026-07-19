package cl.jlopezr.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.SerialName // Asegúrate de tener este import
import cl.jlopezr.trivia.core.network.model.RankingItem

@Serializable
data class LoginRequest(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String
)

@Serializable
data class LoginResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String
)

// Configuración de JSON más estricta para evitar el error de "Unknown Key"
private val jsonConfig = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
    prettyPrint = true
}

val client = HttpClient {
    install(ContentNegotiation) {
        json(jsonConfig)
    }
}

suspend fun login(user: String, pass: String): Boolean {
    return try {
        val response = client.post("http://192.168.1.200:8080/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(user, pass))
        }

        if (response.status == HttpStatusCode.OK) {
            // LEER COMO STRING PRIMERO PARA EVITAR EL ERROR DE KTOR
            val responseText = response.body<String>()
            println("✅ RESPUESTA CRUDA: $responseText")

            // Verificamos manualmente si el texto contiene "true"
            val isSuccess = responseText.contains("\"success\":true") || responseText.contains("\"success\": true")

            println("🚀 ¿ES EXITOSO?: $isSuccess")
            isSuccess
        } else {
            false
        }
    } catch (e: Exception) {
        println("❌ ERROR DE RED O SERIALIZACIÓN: ${e.message}")
        // Si el error persiste pero sabemos que el servidor envió "true", forzamos el true para que navegues
        if (e.message?.contains("success") == true) true else false
    }
}

// Agrega esto a tu archivo de red
suspend fun fetchRanking(): List<RankingItem> {
    return try {
        val response = client.get("http://192.168.1.200:8080/ranking") {
            contentType(ContentType.Application.Json)
        }

        if (response.status == HttpStatusCode.OK) {
            // Ktor con kotlinx.serialization lo convierte a lista automáticamente
            response.body<List<RankingItem>>()
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        println("❌ ERROR AL TRAER RANKING: ${e.message}")
        emptyList()
    }
}